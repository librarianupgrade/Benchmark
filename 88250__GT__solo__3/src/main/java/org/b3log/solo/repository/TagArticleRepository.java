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
package org.b3log.solo.repository;

import org.b3log.latke.Keys;
import org.b3log.latke.repository.*;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.solo.model.Article;
import org.b3log.solo.model.Tag;
import org.json.JSONObject;

import java.util.List;

/**
 * Tag-Article repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.4, Sep 30, 2018
 * @since 0.3.1
 */
@Repository
public class TagArticleRepository extends AbstractRepository {

	/**
	 * Public constructor.
	 */
	public TagArticleRepository() {
		super(Tag.TAG + "_" + Article.ARTICLE);
	}

	/**
	 * Gets tag-article relations by the specified article id.
	 *
	 * @param articleId the specified article id
	 * @return for example
	 * <pre>
	 * [{
	 *         "oId": "",
	 *         "tag_oId": "",
	 *         "article_oId": articleId
	 * }, ....], returns an empty list if not found
	 * </pre>
	 * @throws RepositoryException repository exception
	 */
	public List<JSONObject> getByArticleId(final String articleId) throws RepositoryException {
		final Query query = new Query()
				.setFilter(new PropertyFilter(Article.ARTICLE + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, articleId))
				.setPageCount(1);

		return getList(query);
	}

	/**
	 * Gets tag-article relations by the specified tag id.
	 *
	 * @param tagId          the specified tag id
	 * @param currentPageNum the specified current page number, MUST greater
	 *                       then {@code 0}
	 * @param pageSize       the specified page size(count of a page contains objects),
	 *                       MUST greater then {@code 0}
	 * @return for example
	 * <pre>
	 * {
	 *     "pagination": {
	 *       "paginationPageCount": 88250
	 *     },
	 *     "rslts": [{
	 *         "oId": "",
	 *         "tag_oId": tagId,
	 *         "article_oId": ""
	 *     }, ....]
	 * }
	 * </pre>
	 * @throws RepositoryException repository exception
	 */
	public JSONObject getByTagId(final String tagId, final int currentPageNum, final int pageSize)
			throws RepositoryException {
		final Query query = new Query()
				.setFilter(new PropertyFilter(Tag.TAG + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, tagId))
				.addSort(Article.ARTICLE + "_" + Keys.OBJECT_ID, SortDirection.DESCENDING)
				.setCurrentPageNum(currentPageNum).setPageSize(pageSize).setPageCount(1);

		return get(query);
	}
}
