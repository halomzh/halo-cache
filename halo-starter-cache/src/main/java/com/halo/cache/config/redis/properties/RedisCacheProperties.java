package com.halo.cache.config.redis.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author shoufeng
 */

@Data
@ConfigurationProperties(prefix = RedisCacheProperties.PREFIX)
@NoArgsConstructor
@AllArgsConstructor
public class RedisCacheProperties {

	public static final String PREFIX = "halo.cache.redis";

	/**
	 * 是否开启
	 */
	private boolean enable = false;

	/**
	 * 最后一次写入或访问后经过固定时间过期
	 */
	private int expireAfterAccess = 60;

	/**
	 * 最后一次写入后经过固定时间过期，默认3分钟
	 * expireAfterWrite和expireAfterAccess同时存在时，以expireAfterWrite为准。
	 */
	private int expireAfterWrite = 3 * 60;

	/**
	 * 创建缓存或者最近一次更新缓存后经过固定的时间间隔，刷新缓存，默认1分钟
	 */
	private int refreshAfterWrite = 60;
}
