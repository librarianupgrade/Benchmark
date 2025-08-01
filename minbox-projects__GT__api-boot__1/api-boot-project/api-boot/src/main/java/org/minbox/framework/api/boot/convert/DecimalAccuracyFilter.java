/*
 * Copyright [2019] [恒宇少年 - 于起宇]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 *
 */

package org.minbox.framework.api.boot.convert;

import com.alibaba.fastjson.serializer.ValueFilter;
import org.minbox.framework.api.boot.convert.annotation.ApiBootDecimalAccuracy;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;

/**
 * ApiBoot Decimal Accuracy Value Filter
 * Perform formatting if {@link ApiBootDecimalAccuracy} is added to the field and the type is {@link BigDecimal}
 *
 * @author 恒宇少年
 */
public class DecimalAccuracyFilter implements ValueFilter {

	@Override
	public Object process(Object object, String name, Object value) {
		try {
			// find field
			Field field = ReflectionUtils.findField(object.getClass(), name);
			// Have ApiBootDecimalAccuracy Annotation
			// Value is BigDecimal Instance
			if (field.isAnnotationPresent(ApiBootDecimalAccuracy.class) && value instanceof BigDecimal) {
				ApiBootDecimalAccuracy decimalAccuracy = field.getDeclaredAnnotation(ApiBootDecimalAccuracy.class);
				BigDecimal decimalValue = (BigDecimal) value;
				return decimalValue.setScale(decimalAccuracy.scale(), decimalAccuracy.roundingMode());
			}
		} catch (Exception e) {
			//ignore
			return value;
		}
		return value;
	}
}
