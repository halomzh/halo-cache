package com.halo.cache.aspect;

import com.halo.cache.annotation.CachePut;
import com.halo.cache.handler.impl.RedisCacheHandler;
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

import java.util.concurrent.TimeUnit;

/**
 * @author shoufeng
 */

@Data
@Aspect
@Component
@Slf4j
public class CachePutAspect {

	@Autowired
	private SpringCacheUtils springCacheUtils;

	@Autowired
	private RedissonClient redissonClient;

	@Autowired
	private RedisCacheHandler redisCacheHandler;

	@Pointcut("@annotation(com.halo.cache.annotation.CachePut)")
	public void cachePutPointcut() {
	}

	@Around("cachePutPointcut()")
	public Object around(ProceedingJoinPoint point) throws Throwable {

		CachePut cachePut = springCacheUtils.getAnnotation(point, CachePut.class);
		if (!springCacheUtils.checkCondition(cachePut.condition(), point)) {
			return point.proceed();
		}
		String cacheKey = springCacheUtils.generateCacheKey(cachePut.nameSpace(), cachePut.name(), point);

		RLock rLock = redissonClient.getLock(cacheKey + ":lock");
		Object value;
		try {
			rLock.lock(5 * 60, TimeUnit.SECONDS);
			value = point.proceed();

			redisCacheHandler.putCache(cacheKey, value);
		} finally {
			rLock.unlock();
		}

		return value;
	}

}
