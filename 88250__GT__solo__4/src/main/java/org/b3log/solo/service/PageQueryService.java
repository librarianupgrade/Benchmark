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

import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Paginator;
import org.b3log.solo.model.Page;
import org.b3log.solo.repository.PageRepository;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Page query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Oct 27, 2011
 * @since 0.4.0
 */
@Service
public class PageQueryService {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(PageQueryService.class);

	/**
	 * Page repository.
	 */
	@Inject
	private PageRepository pageRepository;

	/**
	 * Gets a page by the specified page id.
	 *
	 * @param pageId the specified page id
	 * @return for example,
	 * <pre>
	 * {
	 *     "page": {
	 *         "oId": "",
	 *         "pageTitle": "",
	 *         "pageContent": ""
	 *         "pageOrder": int,
	 *         "pagePermalink": "",
	 *         "pageCommentCount": int,
	 *         "pageCommentable": boolean,
	 *         "pageType": "",
	 *         "pageOpenTarget": "",
	 *         "pageIcon": ""
	 *     }
	 * }
	 * </pre>, returns {@code null} if not found
	 * @throws ServiceException service exception
	 */
	public JSONObject getPage(final String pageId) throws ServiceException {
		final JSONObject ret = new JSONObject();

		try {
			final JSONObject page = pageRepository.get(pageId);
			if (null == page) {
				return null;
			}

			ret.put(Page.PAGE, page);

			return ret;
		} catch (final Exception e) {
			LOGGER.log(Level.ERROR, e.getMessage(), e);

			throw new ServiceException(e);
		}
	}

	/**
	 * Gets pages by the specified request json object.
	 * 
	 * @param requestJSONObject the specified request json object, for example,
	 * <pre>
	 * {
	 *     "paginationCurrentPageNum": 1,
	 *     "paginationPageSize": 20,
	 *     "paginationWindowSize": 10
	 * }, see {@link Pagination} for more details
	 * </pre>
	 * @return for example,
	 * <pre>
	 * {
	 *     "pagination": {
	 *         "paginationPageCount": 100,
	 *         "paginationPageNums": [1, 2, 3, 4, 5]
	 *     },
	 *     "pages": [{
	 *         "oId": "",
	 *         "pageTitle": "",
	 *         "pageCommentCount": int,
	 *         "pageOrder": int,
	 *         "pagePermalink": "",
	 *         "pageCommentable": boolean,
	 *         "pageType": "",
	 *         "pageOpenTarget": ""
	 *      }, ....]
	 * }
	 * </pre>
	 * @throws ServiceException service exception
	 * @see Pagination
	 */
	public JSONObject getPages(final JSONObject requestJSONObject) throws ServiceException {
		final JSONObject ret = new JSONObject();

		try {
			final int currentPageNum = requestJSONObject.getInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
			final int pageSize = requestJSONObject.getInt(Pagination.PAGINATION_PAGE_SIZE);
			final int windowSize = requestJSONObject.getInt(Pagination.PAGINATION_WINDOW_SIZE);

			final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize)
					.addSort(Page.PAGE_ORDER, SortDirection.ASCENDING).setPageCount(1);
			final JSONObject result = pageRepository.get(query);
			final int pageCount = result.getJSONObject(Pagination.PAGINATION).getInt(Pagination.PAGINATION_PAGE_COUNT);

			final JSONObject pagination = new JSONObject();
			final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);

			pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
			pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

			final JSONArray pages = result.getJSONArray(Keys.RESULTS);

			for (int i = 0; i < pages.length(); i++) { // remove unused properties
				final JSONObject page = pages.getJSONObject(i);

				page.remove(Page.PAGE_CONTENT);
			}

			ret.put(Pagination.PAGINATION, pagination);
			ret.put(Page.PAGES, pages);

			return ret;
		} catch (final Exception e) {
			LOGGER.log(Level.ERROR, "Gets pages failed", e);

			throw new ServiceException(e);
		}
	}

	/**
	 * Set the page repository with the specified page repository.
	 * 
	 * @param pageRepository the specified page repository
	 */
	public void setPageRepository(final PageRepository pageRepository) {
		this.pageRepository = pageRepository;
	}
}
