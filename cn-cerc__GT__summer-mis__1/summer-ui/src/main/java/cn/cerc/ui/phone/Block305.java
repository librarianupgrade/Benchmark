package cn.cerc.ui.phone;

import cn.cerc.ui.core.HtmlWriter;
import cn.cerc.ui.parts.UIComponent;
import cn.cerc.ui.vcl.UIImage;

/**
 * 用于显示简介，说明等
 * <p>
 * icon图标 + 标题
 * <p>
 * 描述信息
 *
 * @author HuangRongjun
 */
public class Block305 extends UIComponent {
	private String title = "(title)";
	private UIImage icon;
	private String describe = "(describe)";

	public Block305(UIComponent owner) {
		super(owner);
		icon = new UIImage();
		icon.setSrc("jui/phone/block305-icon.png");
	}

	@Override
	public void output(HtmlWriter html) {
		html.println("<!-- %s -->", this.getClass().getName());
		html.print("<div class='block305'>");

		html.print("<div role='title'>");

		html.print("<span role='icon'>");
		icon.output(html);
		html.print("</span>");

		html.print("<span role='title'>%s</span>", this.title);
		html.print("</div>");

		html.print("<div role='describe'>%s</div>", this.describe);
		html.print("</div>");
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public UIImage getIcon() {
		return icon;
	}

	public void setIcon(UIImage icon) {
		this.icon = icon;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

}
