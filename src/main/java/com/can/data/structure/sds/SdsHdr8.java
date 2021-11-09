package com.can.data.structure.sds;

import com.can.data.structure.sds.enums.SdsTypeEnum;

/**
 * <pre>
 *
 * </pre>
 *
 * @author
 * @date 2021-11-08  14:39
 */
public class SdsHdr8 extends Sds {

	/**
	 * 已经使用的长度
	 */
	private byte len;

	/**
	 * 申请的字符长度，不包含 Header 结构和最后的空终止字符
	 */
	private byte alloc;

	public SdsHdr8(char[] content) {

		this.flags = SdsTypeEnum.SDS_TYPE_8.getType();
		this.len = (byte) content.length;
		// TODO 提前分配
		this.alloc = this.len;
		this.buf = new char[this.alloc];
		System.arraycopy(content, 0, buf, 0, content.length);
	}

	@Override
	protected int sdsLen() {
		return len;
	}

}
