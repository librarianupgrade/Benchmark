/*
 * Copyright (c) 2009, 2010, 2011, 2012, 2013, B3log Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.latke.cache;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Latkes;
import org.b3log.latke.RuntimeEnv;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Serializer;
import org.b3log.latke.util.Strings;
import org.b3log.latke.util.freemarker.Templates;
import org.json.JSONObject;

/**
 * Page cache.
 * 
 * <p>
 *   This cache contains some pages and their statistics as the following: 
 *   <pre>
 *     &lt;pageCacheKey1, JSONObject1{oId, title, title, type}&gt;
 *     &lt;pageCacheKey2, JSONObject2{oId, title, title, type}&gt;
 *     ....
 *   </pre>
 * </p>
 * 
 * <p>
 * <i>Page Cache Key</i> generated by method 
 * {@linkplain #getPageCacheKey(java.lang.String, java.lang.String)}.
 * </p>
 *
 * <p>
 *   <b>Note</b>: The method <a href="http://code.google.com/appengine/docs/java/javadoc/
 *  com/google/appengine/api/memcache/MemcacheService.html#clearAll%28%29">
 *   clearAll</a> of <a href="http://code.google.com/appengine/docs/java/javadoc/
 *  com/google/appengine/api/memcache/MemcacheService.html">MemcacheService</a>
 *   does not respect namespaces - this flushes the cache for every namespace.
 * </p>
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.2.3, Aug 27, 2012
 * @since 0.3.1
 */
@SuppressWarnings("unchecked")
public final class PageCaches {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(PageCaches.class.getName());

	/**
	 * Page cache.
	 * <p>
	 * &lt;requestURI?queryString, page info&gt;
	 * </p>
	 */
	private static final Cache<String, Serializable> CACHE;

	/**
	 * Cached page keys.
	 */
	private static final Set<String> KEYS = new HashSet<String>();

	/**
	 * Maximum count of cacheable pages.
	 */
	private static final int MAX_CACHEABLE_PAGE_CNT = 10240;

	/**
	 * Key of page cache name.
	 */
	public static final String PAGE_CACHE_NAME = "page";

	/**
	 * Key of cached time.
	 */
	public static final String CACHED_TIME = "cachedTime";

	/**
	 * Key of cached bytes length.
	 */
	public static final String CACHED_BYTES_LENGTH = "cachedBtypesLength";

	/**
	 * key of cached hit count.
	 */
	public static final String CACHED_HIT_COUNT = "cachedHitCount";

	/**
	 * Maximum count of the most recent used cache.
	 */
	private static final int MOST_RECENT_USED_MAX_COUNT = Integer.MAX_VALUE;

	/**
	 * Key of cached title.
	 */
	public static final String CACHED_TITLE = "cachedTitle";

	/**
	 * Key of cached object id.
	 */
	public static final String CACHED_OID = "cachedOid";

	/**
	 * Key of cached HTML content.
	 */
	public static final String CACHED_CONTENT = "cachedContent";

	/**
	 * Key of cached password.
	 */
	public static final String CACHED_PWD = "cachedPwd";

	/**
	 * Key of cached type.
	 */
	public static final String CACHED_TYPE = "cachedType";

	/**
	 * Key of cached link.
	 */
	public static final String CACHED_LINK = "cachedLink";

	/**
	 * Initializes the cache.
	 */
	static {
		CACHE = (Cache<String, Serializable>) CacheFactory.getCache(PAGE_CACHE_NAME);
		final RuntimeEnv runtimeEnv = Latkes.getRuntimeEnv();

		if (RuntimeEnv.LOCAL == runtimeEnv || RuntimeEnv.BAE == runtimeEnv) {
			CACHE.setMaxCount(MAX_CACHEABLE_PAGE_CNT);
			LOGGER.log(Level.INFO, "Initialized page cache[maxCount={0}]", MAX_CACHEABLE_PAGE_CNT);
		}
	}

	/**
	 * Gets a page cache key by the specified URI and query string.
	 *
	 * @param uri the specified URI
	 * @param queryString the specified query string
	 * @return cache key
	 */
	public static String getPageCacheKey(final String uri, final String queryString) {
		String ret = uri;

		try {
			if (!Strings.isEmptyOrNull(queryString)) {
				ret += "?" + queryString;
			}

			return ret;
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}

		return ret;
	}

	/**
	 * Gets all cached page keys.
	 * 
	 * <p>
	 * <b>Note</b>: Do NOT modify the returned keys set directly.
	 * </p>
	 * 
	 * @return cached page keys, returns an empty set if not found
	 */
	@SuppressWarnings("unchecked")
	public static Set<String> getKeys() {
		syncKeys();

		return KEYS;
	}

	/**
	 * Gets cache.
	 *
	 * @return cache
	 */
	public static Cache<String, ?> getCache() {
		return CACHE;
	}

	/**
	 * Gets a cached page with the specified page cache key.
	 * 
	 * <p>
	 * Invoking this method will NOT change statistic (cache view count), 
	 * see {@link #get(java.lang.String, javax.servlet.http.HttpServletRequest)} 
	 * if you want to update the statistic.
	 * </p>
	 * 
	 * <p>
	 *   <b>Note</b>: Do NOT modify properties of the returned json object,
	 * </p>
	 *
	 * @param pageCacheKey the specified page cache key
	 * @return for example,
	 * <pre>
	 * {
	 *     "cachedContent: "",
	 *     "cachedOid": "",
	 *     "cachedTitle": "",
	 *     "cachedType": "",
	 *     "cachedBytesLength": int,
	 *     "cachedHitCount": long,
	 *     "cachedTime": long
	 * }
	 * </pre>
	 * @see #get(java.lang.String, javax.servlet.http.HttpServletRequest) 
	 */
	public static JSONObject get(final String pageCacheKey) {
		return (JSONObject) CACHE.get(pageCacheKey);
	}

	/**
	 * Gets a cached page with the specified page cache key and update 
	 * stat. flag. 
	 * 
	 * <p>
	 * Invoking this method may change statistic, such as to update the 
	 * cache hit count. But if the specified request made from a search engine 
	 * bot, will NOT change statistic field.
	 * </p>
	 * 
	 * <p>
	 * The {@link #get(java.lang.String)} method will return a cached page 
	 * without update statistic.
	 * </p>
	 * 
	 * <p>
	 *   <b>Note</b>: Do NOT modify properties of the returned json object,
	 * </p>
	 *
	 * @param pageCacheKey the specified page cache key
	 * @param request the specified request
	 * @param response the specified response
	 * @return for example,
	 * <pre>
	 * {
	 *     "cachedContent: "",
	 *     "cachedOid": "",
	 *     "cachedTitle": "",
	 *     "cachedType": "",
	 *     "cachedBytesLength": int,
	 *     "cachedHitCount": long,
	 *     "cachedTime": long
	 * }
	 * </pre>
	 * @see Requests#searchEngineBotRequest(javax.servlet.http.HttpServletRequest) 
	 * @see Requests#hasBeenServed(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse) 
	 * @see #get(java.lang.String) 
	 */
	public static JSONObject get(final String pageCacheKey, final HttpServletRequest request,
			final HttpServletResponse response) {
		final JSONObject ret = (JSONObject) CACHE.get(pageCacheKey);

		if (null == ret) {
			return null;
		}

		try {
			if (!Requests.searchEngineBotRequest(request) && !Requests.hasBeenServed(request, response)) {
				final long hitCount = ret.getLong(CACHED_HIT_COUNT);

				ret.put(CACHED_HIT_COUNT, hitCount + 1);
			}

			CACHE.put(pageCacheKey, ret);
			KEYS.add(pageCacheKey);
		} catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Set stat. of cached page[pageCacheKey=" + pageCacheKey + "] failed", e);
		}

		return ret;
	}

	/**
	 * Puts a page into cache.
	 * 
	 * <p>
	 * Invoking this method may change statistic, such as to initialize the 
	 * cache hit count to 1. But if the specified request made from a search 
	 * engine bot, will initialize the cache hit count to 0.
	 * </p>
	 *
	 * @param pageKey key of the page to put
	 * @param cachedValue value to put, for example, 
	 * <pre>
	 * {
	 *     "cachedContent: "",
	 *     "cachedOid": "",
	 *     "cachedTitle": "",
	 *     "cachedType": "",
	 *     "cachedBytesLength": int,
	 *     "cachedHitCount": long,
	 *     "cachedTime": long
	 * }
	 * </pre>
	 * @param request the specified request 
	 */
	public static void put(final String pageKey, final JSONObject cachedValue, final HttpServletRequest request) {
		check(cachedValue);

		try {
			final String content = cachedValue.getString(CACHED_CONTENT);
			final byte[] bytes = Serializer.serialize(content);

			if (Requests.searchEngineBotRequest(request)) {
				cachedValue.put(CACHED_HIT_COUNT, 0L);
			} else {
				cachedValue.put(CACHED_HIT_COUNT, 1L);
			}
			cachedValue.put(CACHED_BYTES_LENGTH, bytes.length);
			cachedValue.put(CACHED_TIME, System.currentTimeMillis());
		} catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Cache stat. failed[pageKey=" + pageKey + "]", e);
		}

		CACHE.put(pageKey, cachedValue);
		KEYS.add(pageKey);

		LOGGER.log(Level.FINEST, "Put a page[key={0}] into page cache, cached keys[size={1}, {2}]",
				new Object[] { pageKey, KEYS.size(), KEYS });
	}

	/**
	 * Removes a cached pages specified by the given page key.
	 * 
	 * <p>
	 *   <b>Note</b>: In addition to remove cached page content, invoking this 
	 *   method will remove template of the cached page corresponds to.
	 * </p>
	 *
	 * @param pageKey the given page key
	 */
	public static void remove(final String pageKey) {
		CACHE.remove(pageKey);
		KEYS.remove(pageKey);
		Templates.CACHE.clear();
	}

	/**
	 * Removes all cached pages and cached templates.
	 *
	 * <p>
	 *   <b>Note</b>: This method will flush the cache for every namespace (clears all caches).
	 * </p>
	 */
	public static void removeAll() {
		CacheFactory.removeAll();
		Templates.CACHE.clear();

		KEYS.clear();
		LOGGER.info("Removed all cache....");
	}

	/**
	 * Synchronizes the {@linkplain #KEYS keys} collection and cached page
	 * objects.
	 */
	private static void syncKeys() {
		@SuppressWarnings("unchecked")
		final Iterator<String> iterator = KEYS.iterator();
		final Set<String> toRemove = new HashSet<String>();

		while (iterator.hasNext()) {
			final String key = iterator.next();

			if (!CACHE.contains(key)) {
				toRemove.add(key);
				// iterator.remove() will also throw ConcurrentModificationException on GAE
			}
		}

		if (!toRemove.isEmpty()) {
			KEYS.removeAll(toRemove);
			LOGGER.log(Level.FINER, "Removed page cache keys[{0}] for sync", toRemove);
		}
	}

	/**
	 * Checks if all keys of the specified cached page are ready.
	 * 
	 * @param cachedPage the specified cached page
	 */
	private static void check(final JSONObject cachedPage) {
		if (!cachedPage.has(CACHED_CONTENT) || !cachedPage.has(CACHED_OID) || !cachedPage.has(CACHED_TITLE)
				|| !cachedPage.has(CACHED_TYPE) || !cachedPage.has(CACHED_LINK)) {
			throw new IllegalArgumentException("Illegal arguments for caching page, resolve this bug first!");
		}
	}

	/**
	 * Private default constructor.
	 */
	private PageCaches() {
	}
}
