/*
   Copyright 2017 Immutables Authors and Contributors

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.immutables.fixture.parcelable;

import android.os.Parcelable;
import java.util.Optional;
import java.util.OptionalDouble;
import org.immutables.value.Value;

@Value.Style(jdkOnly = true)
@Value.Immutable
public interface ParcVal extends Parcelable {
	String a();

	byte b();

	char c();

	double d();

	boolean e();

	float f();

	long g();

	short h();

	int i();

	String[] aa();

	byte[] ab();

	char[] ac();

	double[] ad();

	boolean[] ae();

	float[] af();

	long[] ag();

	short[] ah();

	int[] ai();

	Object[] ao();

	Optional<Integer> oi();

	OptionalDouble od();

	// TODO objects, lists, maps, arrays, optionals
}
