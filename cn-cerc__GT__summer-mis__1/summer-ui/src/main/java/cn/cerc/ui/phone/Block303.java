package cn.cerc.ui.phone;

import cn.cerc.core.ClassResource;
import cn.cerc.ui.SummerUI;
import cn.cerc.ui.core.HtmlWriter;
import cn.cerc.ui.core.UrlRecord;
import cn.cerc.ui.parts.UIComponent;
import cn.cerc.ui.vcl.UIImage;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 可用菜单 图标 + 文字
 * <p>
 * 一行最多占显示4个菜单
 *
 * @author HuangRongjun
 */
public class Block303 extends UIComponent {
	private static final ClassResource res = new ClassResource(Block303.class, SummerUI.ID);

	private Map<UrlRecord, UIImage> items = new LinkedHashMap<>();

	public Block303(UIComponent owner) {
		super(owner);
	}

	@Override
	public void output(HtmlWriter html) {
		if (items.size() == 0) {
			for (int i = 0; i < 4; i++) {
				UrlRecord url = new UrlRecord();
				url.setName("(名称)");
				url.setSite("#");
				UIImage img = new UIImage();
				img.setSrc("jui/phone/block301-leftIcon.png");
				this.addItem(url, img);
			}
		}

		html.println("<!-- %s -->", this.getClass().getName());
		html.println("<div class='block303'>");
		for (UrlRecord url : items.keySet()) {
			html.println("<div role='item'>");
			html.println("<div role='image'>");
			html.println("<a href='%s'>", url.getUrl());
			items.get(url).output(html);
			html.println("</a>", url.getUrl());
			html.println("</div>");
			html.println("<div role='title'>");
			html.println("<a href='%s'>%s</a>", url.getUrl(), url.getName());
			html.println("</div>");
			html.println("</div>");
		}
		html.println("</div>");
	}

	public void addItem(UrlRecord url, UIImage image) {
		if (items.size() > 3) {
			throw new RuntimeException(res.getString(1, "一个菜单组件最多容纳4个对象"));
		}
		items.put(url, image);
	}

	public int size() {
		return items.size();
	}
}
