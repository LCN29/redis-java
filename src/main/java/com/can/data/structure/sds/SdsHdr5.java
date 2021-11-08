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

    /**
     * 字符串的类型,
     * 只使用低 3 位存储类型, 高 5 位存储数组的长度
     */
    private byte flags;

    /**
     * 数据存储空间
     */
    private char[] buf;

    private final static int LEN_MARK = 3;

    @Override
    protected int sdsLen() {
        return flags >> LEN_MARK;
    }

    @Override
    protected char pos(int index) {
        return buf[index];
    }
}
