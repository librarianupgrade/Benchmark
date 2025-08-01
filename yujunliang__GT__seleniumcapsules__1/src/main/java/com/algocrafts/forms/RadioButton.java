package com.algocrafts.forms;

import com.algocrafts.conditions.StringContains;
import com.algocrafts.converters.FirstMatch;
import com.algocrafts.selenium.Element;
import com.algocrafts.selenium.Locating;
import com.algocrafts.selenium.SearchScope;
import org.openqa.selenium.By;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.algocrafts.conditions.ElementPredicates.DISPLAYED;
import static com.algocrafts.conditions.StringEquals.TRUE;
import static com.algocrafts.converters.ElementFunctions.CLICK;
import static com.algocrafts.converters.GetText.CHECKED;
import static com.algocrafts.converters.GetText.VALUE;
import static com.algocrafts.converters.OptionalGetter.GET;
import static com.algocrafts.locators.Locators.elements;

public class RadioButton<T extends SearchScope<T>> extends Locating<T, Stream<Element>> {

	/**
	 * Constructor this radio button.
	 *
	 * @param where    where
	 * @param selector selector
	 */
	public RadioButton(T where, Supplier<By> selector) {
		super(where, elements(selector));
	}

	/**
	 * @param value value to set
	 */
	public void setValue(Object value) {
		locate(new FirstMatch<>(VALUE.and(new StringContains(value.toString()))).andThen(GET).andThen(CLICK));
	}

	/**
	 * @return the value of the select radio
	 */
	public String getValue() {
		return locate(new FirstMatch<>(DISPLAYED.and(CHECKED.and(TRUE))).andThen(GET).andThen(VALUE));
	}
}
