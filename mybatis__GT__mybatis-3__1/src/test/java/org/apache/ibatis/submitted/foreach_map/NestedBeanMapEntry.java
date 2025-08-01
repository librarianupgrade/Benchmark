/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.foreach_map;

public class NestedBeanMapEntry {
	public NestedBeanMapEntry() {
	}

	public NestedBeanMapEntry(Integer keya, Boolean keyb, Integer valuea, Boolean valueb) {
		this.keya = keya;
		this.keyb = keyb;
		this.valuea = valuea;
		this.valueb = valueb;
	}

	public Integer getKeya() {
		return keya;
	}

	public void setKeya(Integer keya) {
		this.keya = keya;
	}

	public Boolean getKeyb() {
		return keyb;
	}

	public void setKeyb(Boolean keyb) {
		this.keyb = keyb;
	}

	public Integer getValuea() {
		return valuea;
	}

	public void setValuea(Integer valuea) {
		this.valuea = valuea;
	}

	public Boolean getValueb() {
		return valueb;
	}

	public void setValueb(Boolean valueb) {
		this.valueb = valueb;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		NestedBeanMapEntry map3Entry = (NestedBeanMapEntry) o;

		if (keya != null ? !keya.equals(map3Entry.keya) : map3Entry.keya != null)
			return false;
		if (keyb != null ? !keyb.equals(map3Entry.keyb) : map3Entry.keyb != null)
			return false;
		if (valuea != null ? !valuea.equals(map3Entry.valuea) : map3Entry.valuea != null)
			return false;
		if (valueb != null ? !valueb.equals(map3Entry.valueb) : map3Entry.valueb != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = keya != null ? keya.hashCode() : 0;
		result = 31 * result + (valuea != null ? valuea.hashCode() : 0);
		result = 31 * result + (keyb != null ? keyb.hashCode() : 0);
		result = 31 * result + (valueb != null ? valueb.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "NestedBeanMapEntry{" + "keya=" + keya + ", valuea=" + valuea + ", keyb=" + keyb + ", valueb=" + valueb
				+ '}';
	}

	private Integer keya;
	private Boolean keyb;
	private Integer valuea;
	private Boolean valueb;
}
