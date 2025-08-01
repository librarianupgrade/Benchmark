/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.openhtmltopdf.css.style;

import com.openhtmltopdf.css.parser.PropertyValue;

public class BackgroundPosition {
	private final PropertyValue _horizontal;
	private final PropertyValue _vertical;

	public BackgroundPosition(PropertyValue horizontal, PropertyValue vertical) {
		_horizontal = horizontal;
		_vertical = vertical;
	}

	public PropertyValue getHorizontal() {
		return _horizontal;
	}

	public PropertyValue getVertical() {
		return _vertical;
	}

}
