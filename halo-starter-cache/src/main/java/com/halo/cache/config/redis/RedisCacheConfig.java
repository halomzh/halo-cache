package com.halo.cache.config.redis;

import com.halo.cache.aspect.CacheEvictAspect;
import com.halo.cache.aspect.CacheGetAspect;
import com.halo.cache.aspect.CachePutAspect;
import com.halo.cache.config.redis.properties.RedisCacheProperties;
import com.halo.cache.handler.impl.RedisCacheHandler;
import com.halo.cache.utils.SpringCacheUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author shoufeng
 */

@Configuration
@ComponentScan(basePackages = {"com.halo.cache.config.redis"})
@EnableConfigurationProperties(value = {
		RedisCacheProperties.class
})
@ConditionalOnProperty(prefix = RedisCacheProperties.PREFIX, value = "enable")
@Slf4j
@Data
public class RedisCacheConfig {

	@Bean
	public CacheEvictAspect cacheEvictAspect() {

		return new CacheEvictAspect();
	}

	@Bean
	public CacheGetAspect cacheGetAspect() {

		return new CacheGetAspect();
	}

	@Bean
	public CachePutAspect cachePutAspect() {

		return new CachePutAspect();
	}

	@Bean
	public RedisCacheHandler redisCacheHandler() {

		return new RedisCacheHandler();
	}

	@Bean
	@ConditionalOnMissingBean(value = SpringCacheUtils.class)
	public SpringCacheUtils springCacheUtils() {

		return new SpringCacheUtils();
	}

}
