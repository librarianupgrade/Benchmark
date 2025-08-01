/*
 * Solo - A small and beautiful blogging system written in Java.
 * Copyright (c) 2010-present, b3log.org
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

import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Locales;
import org.b3log.solo.model.Option;
import org.b3log.solo.repository.OptionRepository;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Locale;

/**
 * Preference management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.4.0.2, Aug 18, 2019
 * @since 0.4.0
 */
@Service
public class PreferenceMgmtService {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(PreferenceMgmtService.class);

	/**
	 * Option query service.
	 */
	@Inject
	private OptionQueryService optionQueryService;

	/**
	 * Option repository.
	 */
	@Inject
	private OptionRepository optionRepository;

	/**
	 * Language service.
	 */
	@Inject
	private LangPropsService langPropsService;

	/**
	 * Updates the preference with the specified preference.
	 *
	 * @param preference the specified preference
	 * @throws ServiceException service exception
	 */
	public void updatePreference(final JSONObject preference) throws ServiceException {
		final Iterator<String> keys = preference.keys();
		while (keys.hasNext()) {
			final String key = keys.next();
			if (preference.isNull(key)) {
				throw new ServiceException("A value is null of preference [key=" + key + "]");
			}
		}

		final Transaction transaction = optionRepository.beginTransaction();

		try {
			preference.put(Option.ID_C_SIGNS, preference.get(Option.ID_C_SIGNS).toString());

			final JSONObject oldPreference = optionQueryService.getPreference();

			final String version = oldPreference.optString(Option.ID_C_VERSION);
			preference.put(Option.ID_C_VERSION, version);

			final String localeString = preference.getString(Option.ID_C_LOCALE_STRING);
			Latkes.setLocale(new Locale(Locales.getLanguage(localeString), Locales.getCountry(localeString)));

			final JSONObject allowVisitDraftViaPermalinkOpt = optionRepository
					.get(Option.ID_C_ALLOW_VISIT_DRAFT_VIA_PERMALINK);
			allowVisitDraftViaPermalinkOpt.put(Option.OPTION_VALUE,
					preference.optString(Option.ID_C_ALLOW_VISIT_DRAFT_VIA_PERMALINK));
			optionRepository.update(Option.ID_C_ALLOW_VISIT_DRAFT_VIA_PERMALINK, allowVisitDraftViaPermalinkOpt);

			final JSONObject articleListDisplayCountOpt = optionRepository.get(Option.ID_C_ARTICLE_LIST_DISPLAY_COUNT);
			articleListDisplayCountOpt.put(Option.OPTION_VALUE,
					preference.optString(Option.ID_C_ARTICLE_LIST_DISPLAY_COUNT));
			optionRepository.update(Option.ID_C_ARTICLE_LIST_DISPLAY_COUNT, articleListDisplayCountOpt);

			final JSONObject articleListPaginationWindowSizeOpt = optionRepository
					.get(Option.ID_C_ARTICLE_LIST_PAGINATION_WINDOW_SIZE);
			articleListPaginationWindowSizeOpt.put(Option.OPTION_VALUE,
					preference.optString(Option.ID_C_ARTICLE_LIST_PAGINATION_WINDOW_SIZE));
			optionRepository.update(Option.ID_C_ARTICLE_LIST_PAGINATION_WINDOW_SIZE,
					articleListPaginationWindowSizeOpt);

			final JSONObject articleListStyleOpt = optionRepository.get(Option.ID_C_ARTICLE_LIST_STYLE);
			articleListStyleOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_ARTICLE_LIST_STYLE));
			optionRepository.update(Option.ID_C_ARTICLE_LIST_STYLE, articleListStyleOpt);

			final JSONObject blogSubtitleOpt = optionRepository.get(Option.ID_C_BLOG_SUBTITLE);
			blogSubtitleOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_BLOG_SUBTITLE));
			optionRepository.update(Option.ID_C_BLOG_SUBTITLE, blogSubtitleOpt);

			final JSONObject blogTitleOpt = optionRepository.get(Option.ID_C_BLOG_TITLE);
			blogTitleOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_BLOG_TITLE));
			optionRepository.update(Option.ID_C_BLOG_TITLE, blogTitleOpt);

			final JSONObject commentableOpt = optionRepository.get(Option.ID_C_COMMENTABLE);
			commentableOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_COMMENTABLE));
			optionRepository.update(Option.ID_C_COMMENTABLE, commentableOpt);

			final JSONObject enableArticleUpdateHintOpt = optionRepository.get(Option.ID_C_ENABLE_ARTICLE_UPDATE_HINT);
			enableArticleUpdateHintOpt.put(Option.OPTION_VALUE,
					preference.optString(Option.ID_C_ENABLE_ARTICLE_UPDATE_HINT));
			optionRepository.update(Option.ID_C_ENABLE_ARTICLE_UPDATE_HINT, enableArticleUpdateHintOpt);

			final JSONObject externalRelevantArticlesDisplayCountOpt = optionRepository
					.get(Option.ID_C_EXTERNAL_RELEVANT_ARTICLES_DISPLAY_CNT);
			externalRelevantArticlesDisplayCountOpt.put(Option.OPTION_VALUE,
					preference.optString(Option.ID_C_EXTERNAL_RELEVANT_ARTICLES_DISPLAY_CNT));
			optionRepository.update(Option.ID_C_EXTERNAL_RELEVANT_ARTICLES_DISPLAY_CNT,
					externalRelevantArticlesDisplayCountOpt);

			final JSONObject feedOutputCntOpt = optionRepository.get(Option.ID_C_FEED_OUTPUT_CNT);
			feedOutputCntOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_FEED_OUTPUT_CNT));
			optionRepository.update(Option.ID_C_FEED_OUTPUT_CNT, feedOutputCntOpt);

			final JSONObject feedOutputModeOpt = optionRepository.get(Option.ID_C_FEED_OUTPUT_MODE);
			feedOutputModeOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_FEED_OUTPUT_MODE));
			optionRepository.update(Option.ID_C_FEED_OUTPUT_MODE, feedOutputModeOpt);

			final JSONObject footerContentOpt = optionRepository.get(Option.ID_C_FOOTER_CONTENT);
			footerContentOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_FOOTER_CONTENT));
			optionRepository.update(Option.ID_C_FOOTER_CONTENT, footerContentOpt);

			final JSONObject htmlHeadOpt = optionRepository.get(Option.ID_C_HTML_HEAD);
			htmlHeadOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_HTML_HEAD));
			optionRepository.update(Option.ID_C_HTML_HEAD, htmlHeadOpt);

			final JSONObject localeStringOpt = optionRepository.get(Option.ID_C_LOCALE_STRING);
			localeStringOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_LOCALE_STRING));
			optionRepository.update(Option.ID_C_LOCALE_STRING, localeStringOpt);

			final JSONObject metaDescriptionOpt = optionRepository.get(Option.ID_C_META_DESCRIPTION);
			metaDescriptionOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_META_DESCRIPTION));
			optionRepository.update(Option.ID_C_META_DESCRIPTION, metaDescriptionOpt);

			final JSONObject metaKeywordsOpt = optionRepository.get(Option.ID_C_META_KEYWORDS);
			metaKeywordsOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_META_KEYWORDS));
			optionRepository.update(Option.ID_C_META_KEYWORDS, metaKeywordsOpt);

			final JSONObject mostCommentArticleDisplayCountOpt = optionRepository
					.get(Option.ID_C_MOST_COMMENT_ARTICLE_DISPLAY_CNT);
			mostCommentArticleDisplayCountOpt.put(Option.OPTION_VALUE,
					preference.optString(Option.ID_C_MOST_COMMENT_ARTICLE_DISPLAY_CNT));
			optionRepository.update(Option.ID_C_MOST_COMMENT_ARTICLE_DISPLAY_CNT, mostCommentArticleDisplayCountOpt);

			final JSONObject mostUsedTagDisplayCountOpt = optionRepository.get(Option.ID_C_MOST_USED_TAG_DISPLAY_CNT);
			mostUsedTagDisplayCountOpt.put(Option.OPTION_VALUE,
					preference.optString(Option.ID_C_MOST_USED_TAG_DISPLAY_CNT));
			optionRepository.update(Option.ID_C_MOST_USED_TAG_DISPLAY_CNT, mostUsedTagDisplayCountOpt);

			final JSONObject mostViewArticleDisplayCountOpt = optionRepository
					.get(Option.ID_C_MOST_VIEW_ARTICLE_DISPLAY_CNT);
			mostViewArticleDisplayCountOpt.put(Option.OPTION_VALUE,
					preference.optString(Option.ID_C_MOST_VIEW_ARTICLE_DISPLAY_CNT));
			optionRepository.update(Option.ID_C_MOST_VIEW_ARTICLE_DISPLAY_CNT, mostViewArticleDisplayCountOpt);

			final JSONObject noticeBoardOpt = optionRepository.get(Option.ID_C_NOTICE_BOARD);
			noticeBoardOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_NOTICE_BOARD));
			optionRepository.update(Option.ID_C_NOTICE_BOARD, noticeBoardOpt);

			final JSONObject randomArticlesDisplayCountOpt = optionRepository
					.get(Option.ID_C_RANDOM_ARTICLES_DISPLAY_CNT);
			randomArticlesDisplayCountOpt.put(Option.OPTION_VALUE,
					preference.optString(Option.ID_C_RANDOM_ARTICLES_DISPLAY_CNT));
			optionRepository.update(Option.ID_C_RANDOM_ARTICLES_DISPLAY_CNT, randomArticlesDisplayCountOpt);

			final JSONObject recentArticleDisplayCountOpt = optionRepository
					.get(Option.ID_C_RECENT_ARTICLE_DISPLAY_CNT);
			recentArticleDisplayCountOpt.put(Option.OPTION_VALUE,
					preference.optString(Option.ID_C_RECENT_ARTICLE_DISPLAY_CNT));
			optionRepository.update(Option.ID_C_RECENT_ARTICLE_DISPLAY_CNT, recentArticleDisplayCountOpt);

			final JSONObject recentCommentDisplayCountOpt = optionRepository
					.get(Option.ID_C_RECENT_COMMENT_DISPLAY_CNT);
			recentCommentDisplayCountOpt.put(Option.OPTION_VALUE,
					preference.optString(Option.ID_C_RECENT_COMMENT_DISPLAY_CNT));
			optionRepository.update(Option.ID_C_RECENT_COMMENT_DISPLAY_CNT, recentCommentDisplayCountOpt);

			final JSONObject relevantArticlesDisplayCountOpt = optionRepository
					.get(Option.ID_C_RELEVANT_ARTICLES_DISPLAY_CNT);
			relevantArticlesDisplayCountOpt.put(Option.OPTION_VALUE,
					preference.optString(Option.ID_C_RELEVANT_ARTICLES_DISPLAY_CNT));
			optionRepository.update(Option.ID_C_RELEVANT_ARTICLES_DISPLAY_CNT, relevantArticlesDisplayCountOpt);

			final JSONObject signsOpt = optionRepository.get(Option.ID_C_SIGNS);
			signsOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_SIGNS));
			optionRepository.update(Option.ID_C_SIGNS, signsOpt);

			final JSONObject timeZoneIdOpt = optionRepository.get(Option.ID_C_TIME_ZONE_ID);
			timeZoneIdOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_TIME_ZONE_ID));
			optionRepository.update(Option.ID_C_TIME_ZONE_ID, timeZoneIdOpt);

			final JSONObject versionOpt = optionRepository.get(Option.ID_C_VERSION);
			versionOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_VERSION));
			optionRepository.update(Option.ID_C_VERSION, versionOpt);

			final JSONObject faviconURLOpt = optionRepository.get(Option.ID_C_FAVICON_URL);
			faviconURLOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_FAVICON_URL));
			optionRepository.update(Option.ID_C_FAVICON_URL, faviconURLOpt);

			final JSONObject syncGitHubOpt = optionRepository.get(Option.ID_C_SYNC_GITHUB);
			syncGitHubOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_SYNC_GITHUB));
			optionRepository.update(Option.ID_C_SYNC_GITHUB, syncGitHubOpt);

			final JSONObject pullGitHubOpt = optionRepository.get(Option.ID_C_PULL_GITHUB);
			pullGitHubOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_PULL_GITHUB));
			optionRepository.update(Option.ID_C_PULL_GITHUB, pullGitHubOpt);

			final JSONObject hljsThemeOpt = optionRepository.get(Option.ID_C_HLJS_THEME);
			hljsThemeOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_HLJS_THEME));
			optionRepository.update(Option.ID_C_HLJS_THEME, hljsThemeOpt);

			final JSONObject customVarsOpt = optionRepository.get(Option.ID_C_CUSTOM_VARS);
			customVarsOpt.put(Option.OPTION_VALUE, preference.optString(Option.ID_C_CUSTOM_VARS));
			optionRepository.update(Option.ID_C_CUSTOM_VARS, customVarsOpt);

			transaction.commit();
		} catch (final Exception e) {
			if (transaction.isActive()) {
				transaction.rollback();
			}

			LOGGER.log(Level.ERROR, "Updates preference failed", e);
			throw new ServiceException(langPropsService.get("updateFailLabel"));
		}

		LOGGER.log(Level.DEBUG, "Updates preference successfully");
	}
}
