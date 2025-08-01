/*
 * Solo - A small and beautiful blogging system written in Java.
 * Copyright (c) 2010-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.solo.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Plugin;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.Repository;
import org.b3log.latke.service.annotation.Service;
import org.b3log.solo.model.*;
import org.b3log.solo.repository.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Export service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.1, Sep 16, 2018
 * @since 2.5.0
 */
@Service
public class ExportService {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(ExportService.class);

	/**
	 * Archive date repository.
	 */
	@Inject
	private ArchiveDateRepository archiveDateRepository;

	/**
	 * Archive date-Article repository.
	 */
	@Inject
	private ArchiveDateArticleRepository archiveDateArticleRepository;

	/**
	 * Article repository.
	 */
	@Inject
	private ArticleRepository articleRepository;

	/**
	 * Category repository.
	 */
	@Inject
	private CategoryRepository categoryRepository;

	/**
	 * Category-Tag relation repository.
	 */
	@Inject
	private CategoryTagRepository categoryTagRepository;

	/**
	 * Comment repository.
	 */
	@Inject
	private CommentRepository commentRepository;

	/**
	 * Link repository.
	 */
	@Inject
	private LinkRepository linkRepository;

	/**
	 * Option repository.
	 */
	@Inject
	private OptionRepository optionRepository;

	/**
	 * Page repository.
	 */
	@Inject
	private PageRepository pageRepository;

	/**
	 * Plugin repository.
	 */
	@Inject
	private PluginRepository pluginRepository;

	/**
	 * Tag repository.
	 */
	@Inject
	private TagRepository tagRepository;

	/**
	 * Tag-Article repository.
	 */
	@Inject
	private TagArticleRepository tagArticleRepository;

	/**
	 * User repository.
	 */
	@Inject
	private UserRepository userRepository;

	/**
	 * Exports as Hexo markdown format.
	 *
	 * @return posts, password posts and drafts, <pre>
	 * {
	 *     "posts": [
	 *         {
	 *             "front": "", // yaml front matter,
	 *             "title": "",
	 *             "content": ""
	 *         }, ....
	 *     ],
	 *     "passwords": [], // format is same as post
	 *     "drafts": [] // format is same as post
	 * }
	 * </pre>
	 */
	public JSONObject exportHexoMDs() {
		final JSONObject ret = new JSONObject();
		final List<JSONObject> posts = new ArrayList<>();
		ret.put("posts", (Object) posts);
		final List<JSONObject> passwords = new ArrayList<>();
		ret.put("passwords", (Object) passwords);
		final List<JSONObject> drafts = new ArrayList<>();
		ret.put("drafts", (Object) drafts);

		final JSONArray articles = getJSONs(articleRepository);
		for (int i = 0; i < articles.length(); i++) {
			final JSONObject article = articles.optJSONObject(i);
			final Map<String, Object> front = new LinkedHashMap<>();
			final String title = article.optString(Article.ARTICLE_TITLE);
			front.put("title", title);
			final String date = DateFormatUtils.format(article.optLong(Article.ARTICLE_CREATED), "yyyy-MM-dd HH:mm:ss");
			front.put("date", date);
			front.put("updated",
					DateFormatUtils.format(article.optLong(Article.ARTICLE_UPDATED), "yyyy-MM-dd HH:mm:ss"));
			final List<String> tags = Arrays.stream(article.optString(Article.ARTICLE_TAGS_REF).split(","))
					.filter(StringUtils::isNotBlank).map(String::trim).collect(Collectors.toList());
			if (tags.isEmpty()) {
				tags.add("Solo");
			}
			front.put("tags", tags);
			front.put("permalink", article.optString(Article.ARTICLE_PERMALINK));
			final JSONObject one = new JSONObject();
			one.put("front", new Yaml().dump(front));
			one.put("title", title);
			one.put("content", article.optString(Article.ARTICLE_CONTENT));

			if (StringUtils.isNotBlank(article.optString(Article.ARTICLE_VIEW_PWD))) {
				passwords.add(one);

				continue;
			} else if (article.optBoolean(Article.ARTICLE_IS_PUBLISHED)) {
				posts.add(one);

				continue;
			} else {
				drafts.add(one);
			}
		}

		return ret;
	}

	/**
	 * Gets all data as JSON format.
	 */
	public JSONObject getJSONs() {
		final JSONObject ret = new JSONObject();
		final JSONArray archiveDates = getJSONs(archiveDateRepository);
		ret.put(ArchiveDate.ARCHIVE_DATES, archiveDates);

		final JSONArray archiveDateArticles = getJSONs(archiveDateArticleRepository);
		ret.put(ArchiveDate.ARCHIVE_DATE + "_" + Article.ARTICLE, archiveDateArticles);

		final JSONArray articles = getJSONs(articleRepository);
		ret.put(Article.ARTICLES, articles);

		final JSONArray categories = getJSONs(categoryRepository);
		ret.put(Category.CATEGORIES, categories);

		final JSONArray categoryTags = getJSONs(categoryTagRepository);
		ret.put(Category.CATEGORY + "_" + Tag.TAG, categoryTags);

		final JSONArray comments = getJSONs(commentRepository);
		ret.put(Comment.COMMENTS, comments);

		final JSONArray links = getJSONs(linkRepository);
		ret.put(Link.LINKS, links);

		final JSONArray options = getJSONs(optionRepository);
		ret.put(Option.OPTIONS, options);

		final JSONArray pages = getJSONs(pageRepository);
		ret.put(Page.PAGES, pages);

		final JSONArray plugins = getJSONs(pluginRepository);
		ret.put(Plugin.PLUGINS, plugins);

		final JSONArray tags = getJSONs(tagRepository);
		ret.put(Tag.TAGS, tags);

		final JSONArray tagArticles = getJSONs(tagArticleRepository);
		ret.put(Tag.TAG + "_" + Article.ARTICLES, tagArticles);

		final JSONArray users = getJSONs(userRepository);
		ret.put(User.USERS, users);

		return ret;
	}

	private JSONArray getJSONs(final Repository repository) {
		try {
			return repository.get(new Query()).optJSONArray(Keys.RESULTS);
		} catch (final Exception e) {
			LOGGER.log(Level.ERROR, "Gets data from repository [" + repository.getName() + "] failed", e);

			return new JSONArray();
		}
	}
}
