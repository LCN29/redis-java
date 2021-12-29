package com.can.module.ae.enums;

/**
 * <pre>
 * ae 事件标识
 * </pre>
 *
 * @author
 * @date 2021-12-29  18:13
 */
public enum AeMaskEnum {

	// 未注册任何事件
	AE_NONE(0),
	// 当文件描述符为可读时触发
	AE_READABLE(1),
	// 当文件描述符为可写时触发
	AE_WRITABLE(2),
	// 如果同时设置了一个事件为可读和可写, 如果可读事件已在同一事件循环迭代中触发，则永远不要再触发该事件
	// 但是配置为这个，可以以先写后读的方式执行
	AE_BARRIER(4);

	// 标识
	private int mask;

	AeMaskEnum(int mask) {
		this.mask = mask;
	}

	public int getMask() {
		return mask;
	}

}
