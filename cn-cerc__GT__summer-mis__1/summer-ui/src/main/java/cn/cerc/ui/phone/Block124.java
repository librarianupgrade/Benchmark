package cn.cerc.ui.phone;

import cn.cerc.core.ClassResource;
import cn.cerc.ui.SummerUI;
import cn.cerc.ui.core.HtmlWriter;
import cn.cerc.ui.parts.UIComponent;
import cn.cerc.ui.vcl.ext.UISpan;

/**
 * 提示块
 *
 * @author 郭向军
 */
public class Block124 extends UIComponent {
	private static final ClassResource res = new ClassResource(Block124.class, SummerUI.ID);

	private UISpan title = new UISpan();

	public Block124(UIComponent owner) {
		super(owner);
		title.setText(res.getString(1, "提示:"));
	}

	@Override
	public void output(HtmlWriter html) {
		html.println("<!-- %s -->", this.getClass().getName());
		html.print("<div class='block124'>");
		this.title.output(html);
		html.print("</div>");
	}

	public UISpan getTitle() {
		return title;
	}

}
