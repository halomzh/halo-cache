package com.halo.cache.handler;

import com.halo.cache.constant.CacheLevelEnum;
import com.halo.cache.holder.CacheHolder;

/**
 * @author shoufeng
 */

public interface CacheHandler {

	/**
	 * 获取缓存级别
	 *
	 * @return 缓存级别
	 */
	CacheLevelEnum getCacheLevel();

	/**
	 * 获取缓存
	 *
	 * @param cacheKey key
	 * @return 缓存
	 */
	CacheHolder getCache(String cacheKey);

	/**
	 * 驱逐缓存
	 *
	 * @param cacheKey key
	 */
	void evictCache(String cacheKey);

	/**
	 * 新增缓存
	 *
	 * @param cacheKey key
	 * @param value    值
	 * @return 缓存
	 */
	CacheHolder putCache(String cacheKey, Object value);

}
