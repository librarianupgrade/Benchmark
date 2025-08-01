/*
 * Copyright 2010 Jan Ouwens
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
package nl.jqno.equalsverifier.testhelpers.types;

public class Point3D extends Point {
	public int z;

	public Point3D(int x, int y, int z) {
		super(x, y);
		this.z = z;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Point3D)) {
			return false;
		}
		return super.equals(obj) && ((Point3D) obj).z == z;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + (31 * z);
	}

	@Override
	public String toString() {
		return super.toString() + "," + z;
	}
}
