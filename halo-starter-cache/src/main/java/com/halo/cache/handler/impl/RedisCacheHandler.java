package com.halo.cache.handler.impl;

import com.halo.cache.config.redis.properties.RedisCacheProperties;
import com.halo.cache.constant.CacheLevelEnum;
import com.halo.cache.handler.CacheHandler;
import com.halo.cache.holder.CacheHolder;
import com.halo.cache.holder.CacheHolderStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author shoufeng
 */

@Slf4j
@Component
public class RedisCacheHandler implements CacheHandler {

	@Autowired
	private RedissonClient redissonClient;

	@Autowired
	private RedisCacheProperties redisCacheProperties;

	@Override
	public CacheLevelEnum getCacheLevel() {

		return CacheLevelEnum.REMOTE;
	}

	@Override
	public CacheHolder getCache(String cacheKey) {
		RBucket<CacheHolder> cacheHolderRBucket = redissonClient.getBucket(cacheKey);
		if (!cacheHolderRBucket.isExists()) {
			CacheHolder cacheHolder = new CacheHolder();
			cacheHolder.setCacheKey(cacheKey);
			cacheHolder.setCacheHolderStatus(CacheHolderStatusEnum.INVALID.getValue());
			return cacheHolder;
		}
		CacheHolder cacheHolder = cacheHolderRBucket.get();
		if (cacheHolder.getNextUpdateDate().before(new Date())) {
			cacheHolder.setCacheHolderStatus(CacheHolderStatusEnum.REFRESH.getValue());
		}

		cacheHolder.setAccessDate(new Date());
		cacheHolderRBucket.set(cacheHolder, redisCacheProperties.getExpireAfterAccess(), TimeUnit.SECONDS);

		return cacheHolder;
	}

	@Override
	public void evictCache(String cacheKey, Boolean allEntries) {
		if (allEntries) {
			redissonClient.getKeys().deleteByPattern(cacheKey);
		}
		RBucket<CacheHolder> cacheHolderRBucket = redissonClient.getBucket(cacheKey);
		cacheHolderRBucket.delete();
	}

	@Override
	public CacheHolder putCache(String cacheKey, Object value) {
		RBucket<CacheHolder> cacheHolderRBucket = redissonClient.getBucket(cacheKey);
		CacheHolder cacheHolder = new CacheHolder();
		if (cacheHolderRBucket.isExists()) {
			cacheHolder = cacheHolderRBucket.get();
		}

		cacheHolder.setCacheKey(cacheKey);
		cacheHolder.setCacheValue(value);
		cacheHolder.setWriteDate(new Date());
		cacheHolder.setNextUpdateDate(new DateTime().plusSeconds(redisCacheProperties.getRefreshAfterWrite()).toDate());
		cacheHolder.setCacheHolderStatus(CacheHolderStatusEnum.EFFECTIVE.getValue());
		cacheHolderRBucket.set(cacheHolder, redisCacheProperties.getExpireAfterWrite(), TimeUnit.SECONDS);

		return cacheHolder;
	}

}
