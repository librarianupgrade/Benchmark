package cn.cerc.ui.phone;

import cn.cerc.ui.core.HtmlWriter;
import cn.cerc.ui.parts.UIComponent;
import cn.cerc.ui.vcl.UIImage;
import cn.cerc.ui.vcl.UITextBox;
import cn.cerc.ui.vcl.ext.UISpan;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用户下拉选择框，选中立即提交参数
 */
public class Block129 extends UIComponent {
	private UITextBox input = new UITextBox();
	private UIImage image = new UIImage();
	private String content = "";
	private String placeholder = "";
	private Map<String, String> items = new LinkedHashMap<String, String>();
	private String formId;

	/**
	 * 用于select控件
	 *
	 * @param owner 内容显示区
	 */
	public Block129(UIComponent owner) {
		super(owner);
		input.setReadonly(true);
		input.setType("hidden");
		image.setSrc("jui/phone/block107-expand.png");
	}

	@Override
	public void output(HtmlWriter html) {
		String onclick = String.format("javascript:showChoice(\"%s\");", this.getId());
		html.println("<!-- %s -->", this.getClass().getName());
		html.print("<div class='block129'>", this.getId());
		html.print("<div onclick='%s'>", onclick);
		input.setId(this.getId() + "input");
		input.output(html);

		html.println("<div class='content'");
		if (!"".equals(placeholder)) {
			html.println("placeholder='%s'", placeholder);
		}
		html.println(">");

		if (content != null && !"".equals(content)) {
			html.println(content);
		} else if (items.size() > 0) {
			input.setValue(items.keySet().iterator().next());
			html.println(items.get(input.getValue()));
		} else {
			html.print("");
		}
		html.println("</div>");

		image.output(html);
		html.println("</div>");

		html.println("<div id='%schoice' class='choice2'>", this.getId());
		html.print("<div class='choice3'>");
		html.println("</div>");

		html.print("<div id='%slist' class='choice4'>", this.getId());
		html.print("<ul class=''>");

		for (String key : items.keySet()) {
			outputChoiceItem(html, key, items.get(key), this.getId());
		}

		html.println("</ul>");
		html.println("</div>");
		html.println("</div>");
		html.println("</div>");
	}

	public UITextBox getInput() {
		return input;
	}

	public UISpan getCaption() {
		return input.getCaption();
	}

	public Map<String, String> getItems() {
		return items;
	}

	private void outputChoiceItem(HtmlWriter html, String key, String value, String choiceId) {
		html.print("<li onclick='javascript:postItem(this, \"%s\", \"%s\",\"%s\")'>%s</li>", getId(), key, formId,
				value);
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

}