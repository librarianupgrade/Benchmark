package com.alibaba.excel.converters.doubleconverter;

import java.text.ParseException;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.excel.util.NumberUtils;

/**
 * Double and string converter
 *
 * @author Jiaju Zhuang
 */
public class DoubleStringConverter implements Converter<Double> {

	@Override
	public Class supportJavaTypeKey() {
		return Double.class;
	}

	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return CellDataTypeEnum.STRING;
	}

	@Override
	public Double convertToJavaData(CellData cellData, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) throws ParseException {
		return NumberUtils.parseDouble(cellData.getStringValue(), contentProperty);
	}

	@Override
	public CellData convertToExcelData(Double value, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) {
		return NumberUtils.formatToCellData(value, contentProperty);
	}
}
