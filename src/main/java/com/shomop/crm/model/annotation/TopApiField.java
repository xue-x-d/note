package com.shomop.crm.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记从 TOP API 对象中拷贝的属性
 * @author twang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD })
public @interface TopApiField {

    /**
     * TOP API中对应的属性名称，如果为空字符串，表示和TOP API中的属性名称一样。
     */
    public String value() default "";
}
