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

import android.os.Parcel;
import android.os.Parcelable;
import org.immutables.value.Value;

// Having CREATOR field we suppress parcelable generation
@Value.Immutable
public abstract class CustomParcelable implements Parcelable {
	public static final Parcelable.Creator<CustomParcelable> CREATOR = new Parcelable.Creator<CustomParcelable>() {
		public CustomParcelable createFromParcel(Parcel in) {
			throw new UnsupportedOperationException();
		}

		public CustomParcelable[] newArray(int size) {
			throw new UnsupportedOperationException();
		}
	};

	@Override
	public final int describeContents() {
		return 0;
	}

	@Override
	public final void writeToParcel(Parcel dest, int flags) {
	}
}
