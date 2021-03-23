package com.halo.cache.annotation;

import com.halo.cache.constant.CacheConstant;

import java.lang.annotation.*;

/**
 * @author shoufeng
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheEvict {

	/**
	 * 缓存名称空间
	 */
	String nameSpace() default "";

	/**
	 * 缓存名称
	 */
	String[] names() default {};

	/**
	 * 条件
	 */
	String condition() default CacheConstant.UNDEFINED_STRING;

}
