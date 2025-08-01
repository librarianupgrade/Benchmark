package com.alibaba.excel.converters.floatconverter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

/**
 * Float and boolean converter
 *
 * @author Jiaju Zhuang
 */
public class FloatBooleanConverter implements Converter<Float> {
	private static final Float ONE = (float) 1.0;
	private static final Float ZERO = (float) 0.0;

	@Override
	public Class supportJavaTypeKey() {
		return Float.class;
	}

	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return CellDataTypeEnum.BOOLEAN;
	}

	@Override
	public Float convertToJavaData(CellData cellData, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) {
		if (cellData.getBooleanValue()) {
			return ONE;
		}
		return ZERO;
	}

	@Override
	public CellData convertToExcelData(Float value, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) {
		if (ONE.equals(value)) {
			return new CellData(Boolean.TRUE);
		}
		return new CellData(Boolean.FALSE);
	}

}
