package org.voovan.http.server.module.annontationRouter.swagger.entity;

import org.voovan.http.server.module.annontationRouter.swagger.SwaggerApi;
import org.voovan.tools.reflect.annotation.NotSerialization;
import org.voovan.tools.reflect.annotation.Serialization;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger property
 *
 * @author: helyho
 * Voovan Framework.
 * WebSite: https://github.com/helyho/Voovan
 * Licence: Apache v2 License
 */
public class Schema extends Properties {
	@NotSerialization
	private Class clazz;
	/**
	 * 必填。参数类型。”string”, “number”, “integer”, “boolean”, “array” or “file”.
	 * 由于参数不在请求体，所以都是简单类型。consumes必须为multipart/form-data或者application/x-www-form-urlencoded或者两者皆有。
	 * 参数的in值必须为formData。
	 */
	private String type;

	/**
	 * 前面提到的type的扩展格式。详情参照Data Type Formats。
	 */
	private String format;

	//item
	private Schema items;

	private List<String> required;

	@Serialization("default")
	private String defaultVal;

	private String description;

	private Object example;

	public Schema() {
		this.setParent(this);
	}

	public Schema(String type, String format) {
		this.setParent(this);
		this.type = type;

		if (type == null) {
			return;
		}

		//数组的特殊处理
		if (type.equals("array")) {
			items = new Schema(format, null);
			return;
		}
		this.format = format;
	}

	public Class getClazz() {
		return clazz;
	}

	public void setClazz(Class clazz) {
		this.clazz = clazz;
	}

	public String getType() {

		return type;
	}

	public void setType(String type) {
		if (type != null && type.equals("array")) {
			items = new Schema(format, null);
		}
		this.type = type;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Schema getItems() {
		if (items == null) {
			items = new Schema();
		}
		return items;
	}

	public void setItems(Schema items) {
		this.items = items;
	}

	public List<String> getRequired() {
		if (required == null) {
			required = new ArrayList<String>();
		}
		return required;
	}

	public void setRequired(List<String> required) {
		this.required = required;
	}

	public String getDefaultVal() {
		return defaultVal;
	}

	public void setDefaultVal(String defaultVal) {
		this.defaultVal = defaultVal;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setExample(String example) {
		this.example = example == null || example.isEmpty() ? null : SwaggerApi.convertExample(example);
	}

	public void setExample(Object example) {
		this.example = example;
	}
}
