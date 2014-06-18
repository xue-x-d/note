package com.shomop.crm.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 可以导出的对象
 * 标注一些约束
 * 用于导出验证
 * @author spencer.xue
 * @date 2013-9-11
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExportObject {

	/**
	 * 要导出的属性的个数
	 * @return
	 */
	public int columns() default 1;
	/**
	 * 导出说明（广告）
	 * @return
	 */
	public String[] value() default {"该文档由夏猫会宝导出，感谢您的使用！"};
}
