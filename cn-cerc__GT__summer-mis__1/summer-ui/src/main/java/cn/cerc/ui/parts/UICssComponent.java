package cn.cerc.ui.parts;

import cn.cerc.ui.core.HtmlWriter;

public abstract class UICssComponent extends UIComponent {
	protected String cssClass;
	protected String cssStyle;

	public UICssComponent() {
		super();
	}

	public UICssComponent(UIComponent owner) {
		super(owner);
	}

	public String getCssClass() {
		return cssClass;
	}

	public UICssComponent setCssClass(String cssClass) {
		this.cssClass = cssClass;
		return this;
	}

	public String getCssStyle() {
		return cssStyle;
	}

	public void setCssStyle(String cssStyle) {
		this.cssStyle = cssStyle;
	}

	protected void outputCss(HtmlWriter html) {
		if (this.cssClass != null) {
			html.print(" class='%s'", cssClass);
		}
		if (this.cssStyle != null) {
			html.print(" style='%s'", cssStyle);
		}
	}
}
