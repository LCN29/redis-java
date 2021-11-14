package com.can.util;

/**
 * <pre>
 *
 * </pre>
 *
 * @author lcn29
 * @date 2021-11-13 17:32
 */
public class Util {

    /**
     * long 最大的长度 20, -9223372036854775808
     */
    private final static int LONG_NUM_MAX_LENGTH = 20;

    /**
     * 0 字符
     */
    private final static char ZERO_CHAR = '0';

    /**
     * 1 字符
     */
    private final static char ONE_CHAR = '1';

    /**
     * 9 字符
     */
    private final static char NINE_CHAR = '9';

    /**
     * 负号 -
     */
    private final static char MINUS_SIGN = '-';

    public static Long string2Long(byte[] string) {

        int len = string.length;
        // 长度超了, 无法解析, 直接返回 null
        if (len > LONG_NUM_MAX_LENGTH) {
            return null;
        }

        // 0 直接返回
        if (len == 1 && string[0] == ZERO_CHAR) {
            return 0L;
        }

        // 负数标识, 0 不是负数, 1 是负数
        int negative = 0;

        // 在数组的哪个位置开始处理, 默认为第一位
        int numHandlerStarPos = 0;

        // 负数
        if (string[0] == MINUS_SIGN) {

            // 防止只有一个负号
            if (len == 1) {
                return null;
            }

            negative = 1;
            // 第一位为负数符号号, 所以数字开始的位置为第二位
            numHandlerStarPos = 1;
        }

        // 数字的开头不是 1 或者 9,
        if (!(string[numHandlerStarPos] >= ONE_CHAR && string[numHandlerStarPos] <= NINE_CHAR)) {
            return null;
        }

        long value = 0L;

        while (numHandlerStarPos < len && string[numHandlerStarPos] >= ZERO_CHAR && string[numHandlerStarPos] <= NINE_CHAR) {

            // 超了
            if (value > Long.MAX_VALUE / 10) {
                return null;
            }

            value *= 10;

            // 超了
            if (value > (Long.MAX_VALUE - (string[numHandlerStarPos] - ZERO_CHAR))) {
                return null;
            }

            value += (string[numHandlerStarPos] - ZERO_CHAR);
            numHandlerStarPos++;
        }

        // 检查一遍是否是走到尾部结束的，防止因字符串中间异常字符导致提前结束
        if (numHandlerStarPos < len) {
            return null;
        }

        // long 最大值为 9223372036854775807, 最小值为 -9223372036854775808
        // 经过上面的处理, 最小值，到了这一步 value 变为了 -9223372036854775808

        if (negative == 1) {

            // 所以当前 value 是 Long.MIN_VALUE, 直接返回最小值
            if (value == Long.MIN_VALUE) {
                return Long.MIN_VALUE;
            }
        }
        return negative == 1 ? value * -1 : value;
    }

}
