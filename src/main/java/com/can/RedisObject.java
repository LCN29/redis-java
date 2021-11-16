package com.can;

/**
 * <pre>
 *
 * </pre>
 *
 * @author
 * @date 2021-11-16  15:00
 */
public class RedisObject {
	
	/**
	 * 信息 4 (type) + 4 (encoding) + 24 (lru)
	 */
	int info;

	int refcount;

	Object value;
}
