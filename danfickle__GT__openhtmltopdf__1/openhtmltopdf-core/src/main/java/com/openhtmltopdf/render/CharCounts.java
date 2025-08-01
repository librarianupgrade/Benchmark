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
package com.openhtmltopdf.render;

public class CharCounts {
	private int _spaceCount;
	private int _nonSpaceCount;

	public int getSpaceCount() {
		return _spaceCount;
	}

	public void setSpaceCount(int spaceCount) {
		_spaceCount = spaceCount;
	}

	public int getNonSpaceCount() {
		return _nonSpaceCount;
	}

	public void setNonSpaceCount(int nonSpaceCount) {
		_nonSpaceCount = nonSpaceCount;
	}
}
