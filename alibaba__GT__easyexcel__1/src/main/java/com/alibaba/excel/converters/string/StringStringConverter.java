package com.alibaba.excel.converters.string;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

/**
 * String and string converter
 *
 * @author Jiaju Zhuang
 */
public class StringStringConverter implements Converter<String> {
	@Override
	public Class supportJavaTypeKey() {
		return String.class;
	}

	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return CellDataTypeEnum.STRING;
	}

	@Override
	public String convertToJavaData(CellData cellData, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) {
		return cellData.getStringValue();
	}

	@Override
	public CellData convertToExcelData(String value, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) {
		return new CellData(value);
	}

}
