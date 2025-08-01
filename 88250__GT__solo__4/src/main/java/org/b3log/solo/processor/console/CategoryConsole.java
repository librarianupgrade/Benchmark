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
package org.b3log.solo.processor.console;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.JSONRenderer;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.URLs;
import org.b3log.solo.model.Category;
import org.b3log.solo.model.Common;
import org.b3log.solo.model.Tag;
import org.b3log.solo.service.CategoryMgmtService;
import org.b3log.solo.service.CategoryQueryService;
import org.b3log.solo.service.TagQueryService;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Category console request processing.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.3.1, Sep 25, 2018
 * @since 2.0.0
 */
@RequestProcessor
@Before(adviceClass = ConsoleAdminAuthAdvice.class)
public class CategoryConsole {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(CategoryConsole.class);

	/**
	 * Category management service.
	 */
	@Inject
	private CategoryMgmtService categoryMgmtService;

	/**
	 * Category query service.
	 */
	@Inject
	private CategoryQueryService categoryQueryService;

	/**
	 * Tag query service.
	 */
	@Inject
	private TagQueryService tagQueryService;

	/**
	 * Language service.
	 */
	@Inject
	private LangPropsService langPropsService;

	/**
	 * Changes a category order by the specified category id and direction.
	 * <p>
	 * Renders the response with a json object, for example,
	 * <pre>
	 * {
	 *     "sc": boolean,
	 *     "msg": ""
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param request           the specified http servlet request
	 * @param response          the specified http servlet response
	 * @param context           the specified http request context
	 * @param requestJSONObject the specified request json object, for example,
	 *                          "oId": "",
	 *                          "direction": "" // "up"/"down"
	 * @throws Exception exception
	 */
	@RequestProcessing(value = "/console/category/order/", method = HTTPRequestMethod.PUT)
	public void changeOrder(final HttpServletRequest request, final HttpServletResponse response,
			final HTTPRequestContext context, final JSONObject requestJSONObject) throws Exception {
		final JSONRenderer renderer = new JSONRenderer();
		context.setRenderer(renderer);

		final JSONObject ret = new JSONObject();
		try {
			final String categoryId = requestJSONObject.getString(Keys.OBJECT_ID);
			final String direction = requestJSONObject.getString(Common.DIRECTION);

			categoryMgmtService.changeOrder(categoryId, direction);

			ret.put(Keys.STATUS_CODE, true);
			ret.put(Keys.MSG, langPropsService.get("updateSuccLabel"));

			renderer.setJSONObject(ret);
		} catch (final Exception e) {
			LOGGER.log(Level.ERROR, e.getMessage(), e);

			final JSONObject jsonObject = new JSONObject().put(Keys.STATUS_CODE, false);
			renderer.setJSONObject(jsonObject);
			jsonObject.put(Keys.MSG, langPropsService.get("updateFailLabel"));
		}
	}

	/**
	 * Gets a category by the specified request.
	 * <p>
	 * Renders the response with a json object, for example,
	 * <pre>
	 * {
	 *     "sc": boolean,
	 *     "category": {
	 *         "oId": "",
	 *         "categoryTitle": "",
	 *         "categoryURI": "",
	 *         ....
	 *     }
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param request  the specified http servlet request
	 * @param response the specified http servlet response
	 * @param context  the specified http request context
	 * @throws Exception exception
	 */
	@RequestProcessing(value = "/console/category/*", method = HTTPRequestMethod.GET)
	public void getCategory(final HttpServletRequest request, final HttpServletResponse response,
			final HTTPRequestContext context) throws Exception {
		final JSONRenderer renderer = new JSONRenderer();
		context.setRenderer(renderer);
		try {
			final String requestURI = request.getRequestURI();
			final String categoryId = requestURI.substring((Latkes.getContextPath() + "/console/category/").length());

			final JSONObject result = categoryQueryService.getCategory(categoryId);
			if (null == result) {
				renderer.setJSONObject(new JSONObject().put(Keys.STATUS_CODE, false));

				return;
			}

			final StringBuilder tagBuilder = new StringBuilder();
			final List<JSONObject> tags = (List<JSONObject>) result.opt(Category.CATEGORY_T_TAGS);
			for (final JSONObject tag : tags) {
				tagBuilder.append(tag.optString(Tag.TAG_TITLE)).append(",");
			}
			tagBuilder.deleteCharAt(tagBuilder.length() - 1);
			result.put(Category.CATEGORY_T_TAGS, tagBuilder.toString());

			renderer.setJSONObject(result);
			result.put(Keys.STATUS_CODE, true);
		} catch (final ServiceException e) {
			LOGGER.log(Level.ERROR, e.getMessage(), e);

			final JSONObject jsonObject = new JSONObject().put(Keys.STATUS_CODE, false);
			renderer.setJSONObject(jsonObject);
			jsonObject.put(Keys.MSG, langPropsService.get("getFailLabel"));
		}
	}

	/**
	 * Removes a category by the specified request.
	 * <p>
	 * Renders the response with a json object, for example,
	 * <pre>
	 * {
	 *     "sc": boolean,
	 *     "msg": ""
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param request  the specified http servlet request
	 * @param response the specified http servlet response
	 * @param context  the specified http request context
	 * @throws Exception exception
	 */
	@RequestProcessing(value = "/console/category/*", method = HTTPRequestMethod.DELETE)
	public void removeCategory(final HttpServletRequest request, final HttpServletResponse response,
			final HTTPRequestContext context) throws Exception {
		final JSONRenderer renderer = new JSONRenderer();
		context.setRenderer(renderer);
		final JSONObject jsonObject = new JSONObject();
		renderer.setJSONObject(jsonObject);
		try {
			final String categoryId = request.getRequestURI()
					.substring((Latkes.getContextPath() + "/console/category/").length());
			categoryMgmtService.removeCategory(categoryId);

			jsonObject.put(Keys.STATUS_CODE, true);
			jsonObject.put(Keys.MSG, langPropsService.get("removeSuccLabel"));
		} catch (final ServiceException e) {
			LOGGER.log(Level.ERROR, e.getMessage(), e);

			jsonObject.put(Keys.STATUS_CODE, false);
			jsonObject.put(Keys.MSG, langPropsService.get("removeFailLabel"));
		}
	}

	/**
	 * Updates a category by the specified request.
	 * <p>
	 * Renders the response with a json object, for example,
	 * <pre>
	 * {
	 *     "sc": boolean,
	 *     "msg": ""
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param request           the specified http servlet request
	 * @param context           the specified http request context
	 * @param response          the specified http servlet response
	 * @param requestJSONObject the specified request json object, for example,
	 *                          "oId": "",
	 *                          "categoryTitle": "",
	 *                          "categoryURI": "", // optional
	 *                          "categoryDescription": "", // optional
	 *                          "categoryTags": "tag1, tag2" // optional
	 * @throws Exception exception
	 */
	@RequestProcessing(value = "/console/category/", method = HTTPRequestMethod.PUT)
	public void updateCategory(final HttpServletRequest request, final HttpServletResponse response,
			final HTTPRequestContext context, final JSONObject requestJSONObject) throws Exception {
		final JSONRenderer renderer = new JSONRenderer();
		context.setRenderer(renderer);
		final JSONObject ret = new JSONObject();
		renderer.setJSONObject(ret);

		try {
			String tagsStr = requestJSONObject.optString(Category.CATEGORY_T_TAGS);
			tagsStr = Tag.formatTags(tagsStr);
			if (StringUtils.isBlank(tagsStr)) {
				throw new ServiceException(langPropsService.get("tagsEmptyLabel"));
			}
			final String[] tagTitles = tagsStr.split(",");

			String addArticleWithTagFirstLabel = langPropsService.get("addArticleWithTagFirstLabel");

			final List<JSONObject> tags = new ArrayList<>();
			final Set<String> deduplicate = new HashSet<>();
			for (int i = 0; i < tagTitles.length; i++) {
				String tagTitle = StringUtils.trim(tagTitles[i]);
				if (StringUtils.isBlank(tagTitle)) {
					continue;
				}

				final JSONObject tagResult = tagQueryService.getTagByTitle(tagTitle);
				if (null == tagResult) {
					addArticleWithTagFirstLabel = addArticleWithTagFirstLabel.replace("{tag}", tagTitle);

					final JSONObject jsonObject = new JSONObject().put(Keys.STATUS_CODE, false);
					renderer.setJSONObject(jsonObject);
					jsonObject.put(Keys.MSG, addArticleWithTagFirstLabel);

					return;
				}

				if (deduplicate.contains(tagTitle)) {
					continue;
				}

				tags.add(tagResult.optJSONObject(Tag.TAG));
				deduplicate.add(tagTitle);
			}

			final String categoryId = requestJSONObject.optString(Keys.OBJECT_ID);

			final String title = requestJSONObject.optString(Category.CATEGORY_TITLE, "Category");
			JSONObject mayExist = categoryQueryService.getByTitle(title);
			if (null != mayExist && !mayExist.optString(Keys.OBJECT_ID).equals(categoryId)) {
				final JSONObject jsonObject = new JSONObject().put(Keys.STATUS_CODE, false);
				renderer.setJSONObject(jsonObject);
				jsonObject.put(Keys.MSG, langPropsService.get("duplicatedCategoryLabel"));

				return;
			}

			String uri = requestJSONObject.optString(Category.CATEGORY_URI, title);
			if (StringUtils.isBlank(uri)) {
				uri = title;
			}
			uri = URLs.encode(uri);
			mayExist = categoryQueryService.getByURI(uri);
			if (null != mayExist && !mayExist.optString(Keys.OBJECT_ID).equals(categoryId)) {
				final JSONObject jsonObject = new JSONObject().put(Keys.STATUS_CODE, false);
				renderer.setJSONObject(jsonObject);
				jsonObject.put(Keys.MSG, langPropsService.get("duplicatedCategoryURILabel"));

				return;
			}
			if (255 <= StringUtils.length(uri)) {
				final JSONObject jsonObject = new JSONObject().put(Keys.STATUS_CODE, false);
				renderer.setJSONObject(jsonObject);
				jsonObject.put(Keys.MSG, langPropsService.get("categoryURITooLongLabel"));

				return;
			}

			final String desc = requestJSONObject.optString(Category.CATEGORY_DESCRIPTION);

			final JSONObject category = new JSONObject();
			category.put(Category.CATEGORY_TITLE, title);
			category.put(Category.CATEGORY_URI, uri);
			category.put(Category.CATEGORY_DESCRIPTION, desc);

			categoryMgmtService.updateCategory(categoryId, category);
			categoryMgmtService.removeCategoryTags(categoryId); // remove old relations

			// add new relations
			for (final JSONObject tag : tags) {
				final JSONObject categoryTag = new JSONObject();
				categoryTag.put(Category.CATEGORY + "_" + Keys.OBJECT_ID, categoryId);
				categoryTag.put(Tag.TAG + "_" + Keys.OBJECT_ID, tag.optString(Keys.OBJECT_ID));

				categoryMgmtService.addCategoryTag(categoryTag);
			}

			ret.put(Keys.OBJECT_ID, categoryId);
			ret.put(Keys.MSG, langPropsService.get("updateSuccLabel"));
			ret.put(Keys.STATUS_CODE, true);
		} catch (final ServiceException e) {
			LOGGER.log(Level.ERROR, e.getMessage(), e);

			final JSONObject jsonObject = new JSONObject().put(Keys.STATUS_CODE, false);
			renderer.setJSONObject(jsonObject);
			jsonObject.put(Keys.MSG, e.getMessage());
		}
	}

	/**
	 * Adds a category with the specified request.
	 * <p>
	 * Renders the response with a json object, for example,
	 * <pre>
	 * {
	 *     "sc": boolean,
	 *     "oId": "", // Generated category id
	 *     "msg": ""
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param request           the specified http servlet request
	 * @param response          the specified http servlet response
	 * @param context           the specified http request context
	 * @param requestJSONObject the specified request json object, for example,
	 *                          "categoryTitle": "",
	 *                          "categoryURI": "", // optional
	 *                          "categoryDescription": "", // optional
	 *                          "categoryTags": "tag1, tag2" // optional
	 * @throws Exception exception
	 */
	@RequestProcessing(value = "/console/category/", method = HTTPRequestMethod.POST)
	public void addCategory(final HttpServletRequest request, final HttpServletResponse response,
			final HTTPRequestContext context, final JSONObject requestJSONObject) throws Exception {
		final JSONRenderer renderer = new JSONRenderer();
		context.setRenderer(renderer);
		final JSONObject ret = new JSONObject();
		renderer.setJSONObject(ret);

		try {
			String tagsStr = requestJSONObject.optString(Category.CATEGORY_T_TAGS);
			tagsStr = Tag.formatTags(tagsStr);
			if (StringUtils.isBlank(tagsStr)) {
				throw new ServiceException(langPropsService.get("tagsEmptyLabel"));
			}
			final String[] tagTitles = tagsStr.split(",");

			String addArticleWithTagFirstLabel = langPropsService.get("addArticleWithTagFirstLabel");

			final List<JSONObject> tags = new ArrayList<>();
			final Set<String> deduplicate = new HashSet<>();
			for (int i = 0; i < tagTitles.length; i++) {
				String tagTitle = StringUtils.trim(tagTitles[i]);
				if (StringUtils.isBlank(tagTitle)) {
					continue;
				}

				final JSONObject tagResult = tagQueryService.getTagByTitle(tagTitle);
				if (null == tagResult) {
					addArticleWithTagFirstLabel = addArticleWithTagFirstLabel.replace("{tag}", tagTitle);

					final JSONObject jsonObject = new JSONObject().put(Keys.STATUS_CODE, false);
					renderer.setJSONObject(jsonObject);
					jsonObject.put(Keys.MSG, addArticleWithTagFirstLabel);

					return;
				}

				if (deduplicate.contains(tagTitle)) {
					continue;
				}

				tags.add(tagResult.optJSONObject(Tag.TAG));
				deduplicate.add(tagTitle);
			}

			final String title = requestJSONObject.optString(Category.CATEGORY_TITLE, "Category");
			JSONObject mayExist = categoryQueryService.getByTitle(title);
			if (null != mayExist) {
				final JSONObject jsonObject = new JSONObject().put(Keys.STATUS_CODE, false);
				renderer.setJSONObject(jsonObject);
				jsonObject.put(Keys.MSG, langPropsService.get("duplicatedCategoryLabel"));

				return;
			}

			String uri = requestJSONObject.optString(Category.CATEGORY_URI, title);
			if (StringUtils.isBlank(uri)) {
				uri = title;
			}
			uri = URLs.encode(uri);
			mayExist = categoryQueryService.getByURI(uri);
			if (null != mayExist) {
				final JSONObject jsonObject = new JSONObject().put(Keys.STATUS_CODE, false);
				renderer.setJSONObject(jsonObject);
				jsonObject.put(Keys.MSG, langPropsService.get("duplicatedCategoryURILabel"));

				return;
			}
			if (255 <= StringUtils.length(uri)) {
				final JSONObject jsonObject = new JSONObject().put(Keys.STATUS_CODE, false);
				renderer.setJSONObject(jsonObject);
				jsonObject.put(Keys.MSG, langPropsService.get("categoryURITooLongLabel"));

				return;
			}

			final String desc = requestJSONObject.optString(Category.CATEGORY_DESCRIPTION);

			final JSONObject category = new JSONObject();
			category.put(Category.CATEGORY_TITLE, title);
			category.put(Category.CATEGORY_URI, uri);
			category.put(Category.CATEGORY_DESCRIPTION, desc);

			final String categoryId = categoryMgmtService.addCategory(category);

			for (final JSONObject tag : tags) {
				final JSONObject categoryTag = new JSONObject();
				categoryTag.put(Category.CATEGORY + "_" + Keys.OBJECT_ID, categoryId);
				categoryTag.put(Tag.TAG + "_" + Keys.OBJECT_ID, tag.optString(Keys.OBJECT_ID));

				categoryMgmtService.addCategoryTag(categoryTag);
			}

			ret.put(Keys.OBJECT_ID, categoryId);
			ret.put(Keys.MSG, langPropsService.get("addSuccLabel"));
			ret.put(Keys.STATUS_CODE, true);
		} catch (final ServiceException e) {
			LOGGER.log(Level.ERROR, e.getMessage(), e);

			final JSONObject jsonObject = new JSONObject().put(Keys.STATUS_CODE, false);
			renderer.setJSONObject(jsonObject);
			jsonObject.put(Keys.MSG, e.getMessage());
		}
	}

	/**
	 * Gets categories by the specified request json object.
	 * <p>
	 * The request URI contains the pagination arguments. For example, the request URI is /console/categories/1/10/20, means
	 * the current page is 1, the page size is 10, and the window size is 20.
	 * </p>
	 * <p>
	 * Renders the response with a json object, for example,
	 * <pre>
	 * {
	 *     "pagination": {
	 *         "paginationPageCount": 100,
	 *         "paginationPageNums": [1, 2, 3, 4, 5]
	 *     },
	 *     "categories": [{
	 *         "oId": "",
	 *         "categoryTitle": "",
	 *         "categoryURI": "",
	 *         ....
	 *      }, ....]
	 *     "sc": true
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param request  the specified http servlet request
	 * @param response the specified http servlet response
	 * @param context  the specified http request context
	 * @throws Exception exception
	 */
	@RequestProcessing(value = "/console/categories/*/*/*"/* Requests.PAGINATION_PATH_PATTERN */, method = HTTPRequestMethod.GET)
	public void getCategories(final HttpServletRequest request, final HttpServletResponse response,
			final HTTPRequestContext context) throws Exception {
		final JSONRenderer renderer = new JSONRenderer();
		context.setRenderer(renderer);

		try {
			final String requestURI = request.getRequestURI();
			final String path = requestURI.substring((Latkes.getContextPath() + "/console/categories/").length());
			final JSONObject requestJSONObject = Requests.buildPaginationRequest(path);
			final JSONObject result = categoryQueryService.getCategoris(requestJSONObject);
			result.put(Keys.STATUS_CODE, true);
			renderer.setJSONObject(result);

			final JSONArray categories = result.optJSONArray(Category.CATEGORIES);
			for (int i = 0; i < categories.length(); i++) {
				final JSONObject category = categories.optJSONObject(i);
				String title = category.optString(Category.CATEGORY_TITLE);
				title = StringEscapeUtils.escapeXml(title);
				category.put(Category.CATEGORY_TITLE, title);
			}
		} catch (final ServiceException e) {
			LOGGER.log(Level.ERROR, e.getMessage(), e);

			final JSONObject jsonObject = new JSONObject().put(Keys.STATUS_CODE, false);
			renderer.setJSONObject(jsonObject);
			jsonObject.put(Keys.MSG, langPropsService.get("getFailLabel"));
		}
	}
}
