package com.jquery.menu;

import com.algocrafts.pages.Page;
import com.algocrafts.selenium.Element;
import com.algocrafts.selenium.Locator;

import static org.openqa.selenium.By.linkText;

public class MenuGroupLocator implements Locator<Page, Element> {

	private final String menuGroup;

	public MenuGroupLocator(String menuGroup) {
		this.menuGroup = menuGroup;
	}

	public Element locate(Page page) {
		return page.untilFound(() -> linkText(menuGroup));
	}

	@Override
	public String toString() {
		return "[" + menuGroup + "]";
	}
}
