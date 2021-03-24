package com.halo.cache.annotation;

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
	 *
	 * @return 条件表达式
	 */
	String condition() default "";

	/**
	 * 是否全部删除
	 *
	 * @return 默认否
	 */
	boolean allEntries() default false;

	/**
	 * 是否调用前删除
	 *
	 * @return 默认否
	 */
	boolean beforeInvocation() default false;

}
