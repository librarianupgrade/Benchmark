package cn.cerc.ui.page;

import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import cn.cerc.core.ISession;
import cn.cerc.core.Utils;
import cn.cerc.mis.core.AbstractForm;
import cn.cerc.mis.core.AppClient;
import cn.cerc.mis.core.Application;
import cn.cerc.mis.core.IForm;
import cn.cerc.mis.language.R;
import cn.cerc.ui.core.Component;
import cn.cerc.ui.core.IRightMenuLoad;
import cn.cerc.ui.core.UICustomComponent;
import cn.cerc.ui.core.UrlRecord;
import cn.cerc.ui.menu.MenuList;
import cn.cerc.ui.mvc.StartForms;
import cn.cerc.ui.parts.UIComponent;
import cn.cerc.ui.parts.UIFormVertical;
import cn.cerc.ui.parts.UIHeader;

/**
 * 主体子页面(公用)
 *
 * @author 张弓
 */
public class UIPageModify extends UIPage {
	private String searchWaitingId = "";
	private UIComponent body;

	public UIPageModify(IForm form) {
		super();
		setForm(form);
		initCssFile();
		initJsFile();
	}

	public void addExportFile(String service, String key) {
		if (AppClient.ee.equals(this.getForm().getClient().getDevice())) {
			ExportFile item = new ExportFile(service, key);
			this.put("export", item);
		}
	}

	@Override
	protected void writeHtml(PrintWriter out) {
		HttpServletRequest request = getRequest();

		IForm form = this.getForm();
		ISession session = form.getSession();
		UIHeader header = getHeader();
		if (header != null) {
			if (session.logon()) {
				List<UrlRecord> rightMenus = header.getRightMenus();
				IRightMenuLoad menus = Application.getBean(IRightMenuLoad.class);
				if (menus != null)
					menus.loadMenu(form, rightMenus);
			} else {
				header.getHomePage().setSite(Application.getConfig().getWelcomePage());
			}
		}

		// 系统通知消息
		Component content = this.getContent();
		if (form instanceof AbstractForm) {
			if (header != null) {
				header.initHeader();
			}
			if (content.getId() != null) {
				request.setAttribute(content.getId(), content);
			}
			for (Component component : content.getComponents()) {
				if (component.getId() != null) {
					request.setAttribute(component.getId(), component);
				}
			}
		}

		// 开始输出
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");

		String menuCode = StartForms.getRequestCode(this.getForm().getRequest());
		String[] params = menuCode.split("\\.");
		String formId = params[0];
		if (Utils.isNotEmpty(this.getForm().getName())) {
			out.printf("<title>%s</title>\n", R.asString(form, this.getForm().getName()));
		} else {
			out.printf("<title>%s</title>\n", R.asString(form, MenuList.create(this.getForm()).getName(formId)));
		}

		// 所有的请求都不发送 referrer
		out.println("<meta name=\"referrer\" content=\"no-referrer\" />");
		out.println("<meta name=\"format-detection\" content=\"telephone=no\" />");
		out.println("<meta name=\"format-detection\" content=\"email=no\" />");
		out.printf("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\n");
		out.println("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=9; IE=8; IE=7;\"/>");
		out.printf(
				"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0\"/>\n");
		out.print(this.getCssHtml());
		out.print(getScriptHtml());
		out.println("<script>");
		out.println("var Application = new TApplication();");
		out.printf("Application.device = '%s';\n", form.getClient().getDevice());
		out.printf("Application.bottom = '%s';\n", this.getFooter().getId());
		String msg = form.getParam("message", "");
		msg = msg == null ? "" : msg.replaceAll("\r\n", "<br/>");
		out.printf("Application.message = '%s';\n", msg);
		out.printf("Application.searchFormId = '%s';\n", this.searchWaitingId);
		out.println("$(document).ready(function() {");
		out.println("Application.init();");
		out.println("});");
		out.println("</script>");
		out.println("</head>");
		out.println("<body>");
		writeBody(out);
		out.println("</body>");
		out.println("</html>");
	}

	public UIFormVertical createForm() {
		UIFormVertical form = new UIFormVertical(this.getDocument().getContent());
		form.setId("search");
		put("search", form);
		return form;
	}

	public UIComponent getBody() {
		if (body == null) {
			body = new UICustomComponent();
			body.setOwner(this.getDocument().getContent());
			body.setId("search");
		}
		return body;
	}

	public String getSearchWaitingId() {
		return searchWaitingId;
	}

	public void setSearchWaitingId(String searchWaitingId) {
		this.searchWaitingId = searchWaitingId;
	}

}
