package com.halo.cache.aspect;

import com.halo.cache.annotation.CacheGet;
import com.halo.cache.config.redis.properties.RedisCacheProperties;
import com.halo.cache.handler.impl.RedisCacheHandler;
import com.halo.cache.holder.CacheHolder;
import com.halo.cache.holder.CacheHolderStatusEnum;
import com.halo.cache.utils.SpringCacheUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author shoufeng
 */

@Data
@Aspect
@Component
@Slf4j
public class CacheGetAspect {

	@Autowired
	private SpringCacheUtils springCacheUtils;

	@Autowired
	private RedissonClient redissonClient;

	@Autowired
	private RedisCacheHandler redisCacheHandler;

	@Autowired
	private RedisCacheProperties redisCacheProperties;

	private ExecutorService refreshExecutorService = Executors.newCachedThreadPool();

	@Pointcut("@annotation(com.halo.cache.annotation.CacheGet)")
	public void cacheGetPointcut() {
	}

	@Around("cacheGetPointcut()")
	public Object around(ProceedingJoinPoint point) throws Throwable {

		CacheGet cacheGet = springCacheUtils.getAnnotation(point, CacheGet.class);
		if (!springCacheUtils.checkCondition(cacheGet.condition(), point)) {
			return point.proceed();
		}
		String cacheKey = springCacheUtils.generateCacheKey(cacheGet.nameSpace(), cacheGet.name(), point);
		CacheHolder cacheHolder = redisCacheHandler.getCache(cacheKey);

		if (CacheHolderStatusEnum.EFFECTIVE.getValue() == cacheHolder.getCacheHolderStatus()) {
			return cacheHolder.getCacheValue();
		}
		if (CacheHolderStatusEnum.REFRESH.getValue() == cacheHolder.getCacheHolderStatus()) {
			refreshExecutorService.execute(() -> {
				RLock rLock = redissonClient.getLock(cacheKey + ":lock");
				try {
					rLock.lock(5 * 60, TimeUnit.SECONDS);
					CacheHolder cacheHolderTemp = redisCacheHandler.getCache(cacheKey);
					if (CacheHolderStatusEnum.EFFECTIVE.getValue() == cacheHolderTemp.getCacheHolderStatus()) {
						return;
					}
					Object value = point.proceed();
					redisCacheHandler.putCache(cacheKey, value);
				} catch (Throwable throwable) {
					log.error("刷新失败: cacheKey[{}], {}", cacheKey, throwable);
				} finally {
					rLock.unlock();
				}
			});
		}

		if (CacheHolderStatusEnum.INVALID.getValue() == cacheHolder.getCacheHolderStatus()) {
			RLock rLock = redissonClient.getLock(cacheKey + ":lock");
			try {
				rLock.lock(5 * 60, TimeUnit.SECONDS);
				cacheHolder = redisCacheHandler.getCache(cacheKey);
				if (CacheHolderStatusEnum.EFFECTIVE.getValue() == cacheHolder.getCacheHolderStatus()) {
					return cacheHolder.getCacheValue();
				}
				Object value = point.proceed();
				cacheHolder = redisCacheHandler.putCache(cacheKey, value);
			} finally {
				rLock.unlock();
			}
		}

		return cacheHolder.getCacheValue();
	}

}
