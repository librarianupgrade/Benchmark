package com.alibaba.excel.converters.floatconverter;

import java.text.ParseException;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.excel.util.NumberUtils;

/**
 * Float and string converter
 *
 * @author Jiaju Zhuang
 */
public class FloatStringConverter implements Converter<Float> {

	@Override
	public Class supportJavaTypeKey() {
		return Float.class;
	}

	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return CellDataTypeEnum.STRING;
	}

	@Override
	public Float convertToJavaData(CellData cellData, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) throws ParseException {
		return NumberUtils.parseFloat(cellData.getStringValue(), contentProperty);
	}

	@Override
	public CellData convertToExcelData(Float value, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) {
		return NumberUtils.formatToCellData(value, contentProperty);
	}
}
