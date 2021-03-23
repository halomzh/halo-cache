package com.halo.cache.holder;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author shoufeng
 */

@AllArgsConstructor
public enum CacheHolderStatusEnum {

	/**
	 * 状态
	 */
	EFFECTIVE(200), INVALID(500), REFRESH(201);

	/**
	 * 值
	 */
	@Getter
	private int value;


}
