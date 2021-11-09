package com.can.data.structure.sds.enums;

/**
 * <pre>
 * sds 的类型 (sds.h)
 *
 * </pre>
 *
 * @author
 * @date 2021-11-08  14:46
 */
public enum SdsTypeEnum {

	/**
	 * sds 类型定义
	 */
	SDS_TYPE_5((byte) 0),
	SDS_TYPE_8((byte) 1),
	SDS_TYPE_16((byte) 2),
	SDS_TYPE_32((byte) 3),
	SDS_TYPE_64((byte) 4),
	;

	private final byte type;

	SdsTypeEnum(byte type) {
		this.type = type;
	}

	public byte getType() {
		return type;
	}

}
