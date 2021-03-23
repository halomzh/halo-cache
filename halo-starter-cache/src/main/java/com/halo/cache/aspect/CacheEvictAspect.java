package com.halo.cache.aspect;

import com.halo.cache.annotation.CacheEvict;
import com.halo.cache.handler.impl.RedisCacheHandler;
import com.halo.cache.utils.SpringCacheUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author shoufeng
 */

@Data
@Aspect
@Component
@Slf4j
public class CacheEvictAspect {

	@Autowired
	private RedissonClient redissonClient;

	@Autowired
	private SpringCacheUtils springCacheUtils;

	@Autowired
	private RedisCacheHandler redisCacheHandler;

	@Pointcut("@annotation(com.halo.cache.annotation.CacheEvict)")
	public void cacheEvictPointcut() {
	}

	@Around("cacheEvictPointcut()")
	public Object around(ProceedingJoinPoint point) throws Throwable {

		Object value = point.proceed();

		CacheEvict cacheEvict = springCacheUtils.getAnnotation(point, CacheEvict.class);
		List<String> cacheKeyList = Arrays.stream(cacheEvict.names()).map(name -> {
			try {
				return springCacheUtils.generateCacheKey(cacheEvict.nameSpace(), name, point);
			} catch (IOException | NoSuchMethodException e) {
				log.error("转换cacheKey失败: nameSpace[{}], name[{}]", cacheEvict.nameSpace(), name);
			}
			return null;
		}).filter(StringUtils::isNoneBlank).collect(Collectors.toList());

		for (String cacheKey : cacheKeyList) {
			RLock rLock = redissonClient.getLock(cacheKey + ":lock");
			try {
				rLock.lock(5 * 60, TimeUnit.SECONDS);
				redisCacheHandler.evictCache(cacheKey);
			} finally {
				rLock.unlock();
			}

		}

		return value;
	}

}
