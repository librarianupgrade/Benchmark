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
package com.openhtmltopdf.newtable;

/**
 * Instances of this class are effective columns in the table grid. Table
 * columns are managed in terms of effective columns which ensures that only the
 * minimum number of columns necessary to manage the grid are created. For
 * example, a table cell with colspan="1000" will only create a single effective
 * column unless there are other table cells in other rows which force the
 * column to be split.
 */
public class ColumnData {
	private int _span = 1;

	public int getSpan() {
		return _span;
	}

	public void setSpan(int span) {
		_span = span;
	}
}