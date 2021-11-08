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
	SDS_TYPE_5(0),
	SDS_TYPE_8(1),
	SDS_TYPE_16(2),
	SDS_TYPE_32(3),
	SDS_TYPE_64(4),
	;

	private final int type;

	SdsTypeEnum(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

}
