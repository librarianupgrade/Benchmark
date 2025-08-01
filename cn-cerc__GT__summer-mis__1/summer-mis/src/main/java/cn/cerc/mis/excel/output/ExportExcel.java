package cn.cerc.mis.excel.output;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import cn.cerc.core.DataSet;
import cn.cerc.core.ISession;
import cn.cerc.db.core.IHandle;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExportExcel implements IHandle {
	private static ApplicationContext app;
	public static final String LocalPath = System.getProperty("user.home") + System.getProperty("file.separator");
	private final static String xmlFile = "classpath:export-excel.xml";
	private HttpServletResponse response;
	private String templateId;
	private ExcelTemplate template;
	// 导出是否保存到本地，用于发送邮件时获取，默认false
	private boolean saveLocal = false;
	private ISession session;

	public ExportExcel() {

	}

	public ExportExcel(IHandle handle, HttpServletResponse response) {
		this.setSession(handle.getSession());
		this.response = response;
	}

	public void export() throws IOException, WriteException, AccreditException {
		if (this.session == null) {
			throw new RuntimeException("session is null");
		}

		template = this.getTemplate();

		IAccreditManager manager = template.getAccreditManager();
		if (manager != null) {
			if (!manager.isPass(this)) {
				throw new AccreditException(String.format("您没有导出[%s]的权限", manager.getDescribe()));
			}
		}

		HistoryWriter writer = template.getHistoryWriter();
		if (writer != null) {
			writer.start(this, template);
			exportDataSet();
			writer.finish(this, template);
		} else {
			exportDataSet();
		}
	}

	private void exportDataSet() throws IOException, WriteException {
		template = this.getTemplate();

		// 创建工作薄
		WritableWorkbook workbook;
		OutputStream os = null;
		if (!saveLocal) {
			// 取得输出流
			os = response.getOutputStream();
			response.reset();// 清空输出流

			// 下面是对中文文件名的处理
			response.setCharacterEncoding("UTF-8");// 设置相应内容的编码格式
			String fname = URLEncoder.encode(template.getFileName(), "UTF-8");
			response.setHeader("Content-Disposition", "attachment;filename=" + fname + ".xls");
			response.setContentType("application/msexcel");// 定义输出类型
			workbook = Workbook.createWorkbook(os);
		} else {
			String path = LocalPath + "\\" + template.getFileName() + ".xls";
			workbook = Workbook.createWorkbook(new File(path));
		}

		// 创建新的一页
		WritableSheet sheet = workbook.createSheet("Sheet1", 0);
		template.output(sheet);

		// 把创建的内容写入到输出流中，并关闭输出流
		workbook.write();
		workbook.close();
		if (os != null) {
			os.close();
		}
	}

	public void export(String message) throws WriteException, IOException {
		this.setTemplateId("ExportMessage");
		DataSet ds = new DataSet();
		ds.append();
		ds.setField("message_", message);
		this.getTemplate().setDataSet(ds);
		exportDataSet();
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public ExcelTemplate getTemplate() {
		if (template == null) {
			if (getTemplateId() == null) {
				throw new RuntimeException("templateId is null");
			}
			if (app == null) {
				app = new FileSystemXmlApplicationContext(xmlFile);
			}
			template = app.getBean(getTemplateId(), ExcelTemplate.class);
		}
		return template;
	}

	public void setTemplate(ExcelTemplate template) {
		this.template = template;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public void setSaveLocal(boolean saveLocal) {
		this.saveLocal = saveLocal;
	}

	@Override
	public ISession getSession() {
		return session;
	}

	@Override
	public void setSession(ISession session) {
		this.session = session;
	}

}