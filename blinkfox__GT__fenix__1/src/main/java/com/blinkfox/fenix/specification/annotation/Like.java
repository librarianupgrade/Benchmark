package com.blinkfox.fenix.specification.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于“模糊匹配某个元素”({@code LIKE})场景的注解.
 *
 * @author YangWenpeng 2019-12-17
 * @author blinkfox on 2020-01-13
 * @since v2.2.0
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Like {

	/**
	 * 注解的实体字段属性名称，默认为空或空字符串时将使用属性名称.
	 *
	 * @return 值
	 */
	String value() default "";

}
