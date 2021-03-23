package com.halo.cache.annotation;

import com.halo.cache.constant.CacheConstant;
import com.halo.cache.constant.CacheLevelEnum;

import java.lang.annotation.*;

/**
 * @author shoufeng
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CachePut {

	/**
	 * 缓存名称空间
	 */
	String nameSpace() default "";

	/**
	 * 缓存名称
	 */
	String name() default CacheConstant.UNDEFINED_STRING;

	/**
	 * 缓存级别
	 */
	CacheLevelEnum cacheLevel() default CacheLevelEnum.REMOTE;

	/**
	 * 条件
	 */
	String condition() default CacheConstant.UNDEFINED_STRING;

}
