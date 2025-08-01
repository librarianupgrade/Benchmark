/*
 * {{{ header & license
 * Copyright (c) 2005 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.openhtmltopdf.render;

/**
 * A bean containing the distances that floated content takes up as measured
 * from the left and right content edge of the containing block.  Line boxes
 * use this information to figure out where they need to position themselves
 * within their containing block box.
 */
public class FloatDistances {
	private int _leftFloatDistance;
	private int _rightFloatDistance;

	public FloatDistances() {
	}

	public int getLeftFloatDistance() {
		return _leftFloatDistance;
	}

	public void setLeftFloatDistance(int leftFloatDistance) {
		_leftFloatDistance = leftFloatDistance;
	}

	public int getRightFloatDistance() {
		return _rightFloatDistance;
	}

	public void setRightFloatDistance(int rightFloatDistance) {
		_rightFloatDistance = rightFloatDistance;
	}
}
