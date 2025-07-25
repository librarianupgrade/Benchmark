package cn.cerc.mis.core;

import java.util.HashMap;
import java.util.Map;

// 此對象應該換為 MenuItem
public class MenuItem {
	// 菜單代碼
	private String id;
	// 菜單標題
	public static final String TITLE = "title";
	// 菜單編號，一般為數字
	public static final String PAGENO = "formNo";
	// 軟件類別，如 1,2,，其中1及2各代表一種軟件
	public static final String SOFTWARE = "versions";
	// 菜單授權碼
	public static final String PERMISSION = "permission";
	// true: 需要登錄方可使用
	public static final String SECURITY = "security";
	// 上級菜單，若無，則為""
	public static final String PARENT = "parentId";
	// 菜單圖標，為URL值
	public static final String IMAGE = "image";
	// 所有參數值
	private Map<String, String> params = new HashMap<>();

	public MenuItem() {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		String result = this.getParam(TITLE);
		return result != null ? result : "";
	}

	public String getPageNo() {
		String result = this.getParam(PAGENO);
		return result != null ? result : "";
	}

	public boolean isSecurityEnabled() {
		String result = this.getParam(SECURITY);
		return result != null ? "true".equals(result) : true;
	}

	public String getPermissionCode() {
		String result = this.getParam(PERMISSION);
		return result != null ? result : "";
	}

	public String getSoftwareList() {
		String result = this.getParam(SOFTWARE);
		return result != null ? result : "";
	}

	public String getParentId() {
		String result = this.getParam(PARENT);
		return result != null ? result : "";
	}

	public String getImage() {
		String result = this.getParam(IMAGE);
		return result != null ? result : "";
	}

	public void setParam(String key, String value) {
		params.put(key, value);
	}

	private String getParam(String key) {
		return params.get(key);
	}

	@Deprecated
	public Map<String, String> getParams() {
		return params;
	}

	@Deprecated
	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public int getHrip() {
		return Integer.parseInt(getParam(this.getId()));
	}

}