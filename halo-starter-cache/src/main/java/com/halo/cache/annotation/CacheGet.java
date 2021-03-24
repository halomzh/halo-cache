package com.halo.cache.annotation;

import java.lang.annotation.*;

/**
 * @author shoufeng
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheGet {

	/**
	 * 缓存名称空间
	 */
	String nameSpace() default "";

	/**
	 * 缓存名称
	 */
	String name() default "";

	/**
	 * 条件
	 */
	String condition() default "";
	
}
