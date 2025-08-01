
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

import ij.plugin.PlugIn;
import uk.ac.sussex.gdsc.ij.About_PlugIn;

/**
 * Default ImageJ plugin (no Java package) to run the {@link About_PlugIn} plugin.
 *
 * <p><strong>This class is not included in the packaged uk.ac.sussex.gdsc.ij.ext jar.</strong>
 *
 * <p>This class allows the project to be run in debug mode from an IDE (e.g. Eclipse). The Maven
 * output directory will be target/classes. Create a symbolic link to that directory from the
 * project root and name it plugins. Optionally create a link to the macros directory to allow the
 * toolset to be loaded:
 *
 * <pre>
 * {@code
 * ${root}/plugins -> ${root}/target/classes
 * ${root}/macros -> ${root}/target/classes/macros
 * }
 * </pre>
 *
 * <p>Set the project to run ij.ImageJ as the main class and use the root directory as the ImageJ
 * path:
 *
 * <pre>
 * ij.ImageJ -ijpath ${root}
 * </pre>
 *
 * <p>ImageJ will load this class from the plugins directory. This class can call all other plugins.
 */
public class Gdsc_PlugIn implements PlugIn {
	/** {@inheritDoc} */
	@Override
	public void run(String arg) {
		// Show the About plugin
		About_PlugIn.showAbout();
	}
}
