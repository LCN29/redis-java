package com.can.sds;

/**
 * <pre>
 *
 * </pre>
 *
 * @author
 * @date 2021-11-08  14:39
 */
public class SdsHdr5 extends Sds {

	/**
	 * 字符串的类型,
	 * 低 3 位存储类型, 高 5 位存储长度
	 */
	private byte flags;

	/**
	 * 数据存储空间
	 */
	private char[] buf;

}
