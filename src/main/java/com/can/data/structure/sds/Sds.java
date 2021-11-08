package com.can.data.structure.sds;

import java.util.Objects;

/**
 * <pre>
 *
 * 	动态字符串 Simple Dynamic String
 *
 * </pre>
 *
 * @author
 * @date 2021-11-08  16:41
 */
public abstract class Sds {

    /**
     * 比较 2 个 sds 是否相同
     * 比较规则,
     * @param anotherSds
     * @return
     */
    public boolean equals(Sds anotherSds) {

        if (Objects.isNull(anotherSds)) {
            return false;
        }

        if (this == anotherSds) {
            return true;
        }

        int minLen = Math.min(sdsLen(), anotherSds.sdsLen());

        int lenDifference = sdsLen() - anotherSds.sdsLen();

        if (lenDifference == 0) {
            // 2 个字符串一样长
            for (int i = 0; i < minLen; i++) {
                if (pos(i) != anotherSds.pos(i)) {
                    return pos(i) > anotherSds.pos(i);
                }
            }
        } else if (lenDifference > 0) {

            // 当前字符串长
            for (int i = 0; i < minLen; i++) {
                if (pos(i + lenDifference) != anotherSds.pos(i)) {
                    return pos(i + lenDifference) > anotherSds.pos(i);
                }
            }

        } else {
            // 比较的字符串长
            for (int i = 0; i < minLen; i++) {
                if (pos(i) != anotherSds.pos(i + lenDifference)) {
                    return pos(i) > anotherSds.pos(i + lenDifference);
                }
            }
        }

        return sdsLen() > anotherSds.sdsLen();
    }

    /**
     * 获取字符串的长度
     * @return
     */
    protected abstract int sdsLen();

    /**
     * 获取指定位置的字符
     * @param index
     * @return
     */
    protected abstract char pos(int index);

}
