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
package com.openhtmltopdf.layout;

import com.openhtmltopdf.render.BlockBox;

/**
 * A bean containing the result of laying out a floated block.  If the floated
 * block can't fit on the current line, it will be marked pending with the result
 * that it will be layed out again once the line has been saved.
 *   
 * FIXME: This class can go away
 */
public class FloatLayoutResult {
	private boolean _pending;
	private BlockBox _block;

	public boolean isPending() {
		return _pending;
	}

	public void setPending(boolean pending) {
		_pending = pending;
	}

	public BlockBox getBlock() {
		return _block;
	}

	public void setBlock(BlockBox block) {
		_block = block;
	}
}
