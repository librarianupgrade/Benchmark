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
package org.b3log.solo.event;

/**
 * Event types.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.8, Feb 10, 2019
 * @since 0.3.1
 */
public final class EventTypes {

	/**
	 * Indicates a add article event.
	 */
	public static final String ADD_ARTICLE = "Add Article";

	/**
	 * Indicates a update article event.
	 */
	public static final String UPDATE_ARTICLE = "Update Article";

	/**
	 * Indicates a before render article event.
	 */
	public static final String BEFORE_RENDER_ARTICLE = "Before Render Article";

	/**
	 * Indicates an add comment to article event.
	 */
	public static final String ADD_COMMENT_TO_ARTICLE = "Add Comment To Article";

	/**
	 * Indicates an add comment to page event.
	 */
	public static final String ADD_COMMENT_TO_PAGE = "Add Comment To Page";

	/**
	 * Private constructor.
	 */
	private EventTypes() {
	}
}
