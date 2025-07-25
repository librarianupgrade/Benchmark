package com.algocrafts.converters;

import com.algocrafts.selenium.Locator;

import java.util.List;

public class ElementAtIndex<T> implements Locator<List<T>, T> {

	private final int index;

	public ElementAtIndex(int index) {
		this.index = index;
	}

	@Override
	public T locate(List<T> list) {
		return list.get(index);
	}

}
