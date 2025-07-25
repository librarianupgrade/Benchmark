package cn.cerc.ui.phone;

import cn.cerc.core.ClassResource;
import cn.cerc.ui.SummerUI;
import cn.cerc.ui.core.HtmlWriter;
import cn.cerc.ui.core.UrlRecord;
import cn.cerc.ui.parts.UIComponent;
import cn.cerc.ui.vcl.UIImage;

import java.util.ArrayList;
import java.util.List;

/**
 * 两行文字列表描述，右侧带导航栏箭头
 *
 * @author HuangRongjun
 */
public class Block201 extends UIComponent {
	private static final ClassResource res = new ClassResource(Block201.class, SummerUI.ID);

	private List<String> items = new ArrayList<>();
	private UIImage icon = new UIImage();
	private UrlRecord url;

	public Block201(UIComponent owner) {
		super(owner);
		url = new UrlRecord();
		icon.setSrc("jui/phone/block301-rightIcon.png");
		icon.setRole("right");
	}

	@Override
	public void output(HtmlWriter html) {
		if (items.size() == 0) {
			for (int i = 0; i < 2; i++) {
				items.add("line" + i);
			}
		}

		html.println("<!-- %s -->", this.getClass().getName());
		html.print("<div class='block201'>");
		html.print("<a href='%s'>", url.getUrl());

		for (String line : items) {
			html.print("<div role='line'>%s</div>", line);
		}

		icon.output(html);

		html.print("</a>");
		html.print("</div>");
	}

	public UrlRecord getUrl() {
		return url;
	}

	public void setUrl(UrlRecord url) {
		this.url = url;
	}

	public UIImage getIcon() {
		return icon;
	}

	public int size() {
		return items.size();
	}

	public void addItems(String line) {
		if (items.size() > 1) {
			throw new RuntimeException(res.getString(1, "最多只能放2行信息"));
		}
		items.add(line);
	}
}
