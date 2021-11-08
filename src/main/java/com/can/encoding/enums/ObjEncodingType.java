package com.can.encoding.enums;

/**
 * <pre>
 * 编码类型 (server.h)
 * </pre>
 *
 * @author lcn29
 * @date 2021-11-08  17:14
 */
public enum ObjEncodingType {

	/**
	 * 编码类型
	 */
	OBJ_ENCODING_RAW(0),
	OBJ_ENCODING_INT(1),
	OBJ_ENCODING_HT(2),
	OBJ_ENCODING_ZIPMAP(3),
	OBJ_ENCODING_LINKEDLIST(4),
	OBJ_ENCODING_ZIPLIST(5),
	OBJ_ENCODING_INTSET(6),
	OBJ_ENCODING_SKIPLIST(7),
	OBJ_ENCODING_EMBSTR(8),
	OBJ_ENCODING_QUICKLIST(9),
	OBJ_ENCODING_STREAM(10),
	OBJ_ENCODING_LISTPACK(11);

	private final int type;

	ObjEncodingType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
}
