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
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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

		Object value;

		CacheEvict cacheEvict = springCacheUtils.getAnnotation(point, CacheEvict.class);
		if (!springCacheUtils.checkCondition(cacheEvict.condition(), point)) {
			return point.proceed();
		}
		if (cacheEvict.beforeInvocation()) {
			delete(cacheEvict, point);
			value = point.proceed();
		} else {
			value = point.proceed();
			delete(cacheEvict, point);
		}

		return value;
	}

	private void delete(CacheEvict cacheEvict, ProceedingJoinPoint point) {
		if (cacheEvict.allEntries()) {
			redisCacheHandler.evictCache("nameSpace:" + cacheEvict.nameSpace() + ":*", true);
		}
		List<String> cacheKeyList = Arrays.stream(cacheEvict.names()).map(name -> {
			try {
				return springCacheUtils.generateCacheKey(cacheEvict.nameSpace(), name, point);
			} catch (IOException | NoSuchMethodException e) {
				log.error("转换cacheKey失败: nameSpace[{}], name[{}]", cacheEvict.nameSpace(), name);
			}
			return null;
		}).filter(StringUtils::isNoneBlank).collect(Collectors.toList());

		cacheKeyList.forEach(cacheKey -> redisCacheHandler.evictCache(cacheKey, false));

	}

}
