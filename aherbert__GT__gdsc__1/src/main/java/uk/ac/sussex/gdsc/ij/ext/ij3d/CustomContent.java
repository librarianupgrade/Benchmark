/*-
 * #%L
 * Genome Damage and Stability Centre ImageJ Plugins
 *
 * Software for microscopy image analysis
 * %%
 * Copyright (C) 2011 - 2022 Alex Herbert
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package uk.ac.sussex.gdsc.ij.ext.ij3d;

import ij3d.Content;

/**
 * Extend the Content class to use CustomContentInstant.
 */
public class CustomContent extends Content {
	/**
	 * Instantiates a new custom content.
	 *
	 * @param name the name
	 * @param isOrdered the is ordered
	 */
	public CustomContent(String name, boolean isOrdered) {
		super(name);
		// Replace the default from the super constructor
		final CustomContentInstant ci = new CustomContentInstant(name + "_#0", isOrdered);
		addInstant(ci);
	}
}
