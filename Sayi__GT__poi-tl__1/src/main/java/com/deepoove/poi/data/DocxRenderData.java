/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.deepoove.poi.data;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 待合并文档和数据集合
 * @author Sayi
 * @version 1.3.0
 */
public class DocxRenderData implements RenderData {

	/**
	 * 待合并文档
	 */
	private File docx;

	/**
	 * 渲染待合并文档模板的数据集合，若合并文档不是个模板，可为空
	 */
	private List<?> dataList;

	public DocxRenderData(File docx) {
		this.docx = docx;
	}

	/**
	 * 待合并文档可以先被数据集合渲染
	 * 
	 * @param docx
	 * @param dataList 数据列表，列表的大小表示循环的次数
	 */
	public DocxRenderData(File docx, List<?> dataList) {
		this.docx = docx;
		this.dataList = dataList;
	}

	/**
	 * 待合并文档可以先被数据渲染
	 * 
	 * @param docx
	 * @param data 数据，循环次数为1
	 */
	public DocxRenderData(File docx, Object data) {
		this.docx = docx;
		this.dataList = Arrays.asList(data);
	}

	public File getDocx() {
		return docx;
	}

	public void setDocx(File docx) {
		this.docx = docx;
	}

	public List<?> getDataList() {
		return dataList;
	}

	public void setDataList(List<?> dataList) {
		this.dataList = dataList;
	}

}
