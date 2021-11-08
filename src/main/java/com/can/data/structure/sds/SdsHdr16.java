package com.can.data.structure.sds;

/**
 * <pre>
 *
 * </pre>
 *
 * @author
 * @date 2021-11-08  14:44
 */
public class SdsHdr16 {

	/**
	 * 已经使用的长度
	 */
	private short len;

	/**
	 * 申请的字符长度，不包含 Header 结构和最后的空终止字符
	 */
	private short alloc;

	/**
	 * 字符串的类型,
	 * 只使用低 3 位存储类型, 高 5 位不使用
	 */
	private byte flags;

	/**
	 * 数据存储空间
	 */
	private char[] buf;


}
