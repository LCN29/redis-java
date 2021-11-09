package com.can.data.structure.sds;

/**
 * <pre>
 *
 * </pre>
 *
 * @author lcn29
 * @date 2021-11-08 22:12
 */
public class SdsHdr5 extends Sds {

	private final static int LEN_MARK = 3;

	@Override
	protected int sdsLen() {
		return flags >> LEN_MARK;
	}

}
