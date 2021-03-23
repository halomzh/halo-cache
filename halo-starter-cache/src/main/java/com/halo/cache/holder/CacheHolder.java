package com.halo.cache.holder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author shoufeng
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheHolder implements Serializable {

	/**
	 * 缓存键
	 */
	private String cacheKey;

	/**
	 * 缓存值
	 */
	private Object cacheValue;

	/**
	 * 写入时间
	 */
	private Date writeDate;

	/**
	 * 访问时间
	 */
	private Date accessDate;

	/**
	 * 下次更新时间
	 */
	private Date nextUpdateDate;

	/**
	 * 缓存状态
	 */
	private int cacheHolderStatus;
}
