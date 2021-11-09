package com.can.data.structure.sds;

/**
 * <pre>
 *
 * </pre>
 *
 * @author
 * @date 2021-11-08  14:44
 */
public class SdsHdr16 extends Sds {

	/**
	 * 已经使用的长度
	 */
	private short len;

	/**
	 * 申请的字符长度，不包含 Header 结构和最后的空终止字符
	 */
	private short alloc;

	@Override
	protected int sdsLen() {
		return len;
	}
}
