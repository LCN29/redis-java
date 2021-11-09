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
	 * 字符串的类型,
	 * 一般只使用低 3 位存储类型, 高 5 位不使用
	 * sds5 使用了低  3 位存储类型, 高 5 位表示字符串的长度
	 */
	protected byte flags;

	/**
	 * 数据存储空间
	 */
	protected char[] buf;


	/**
	 * 比较 2 个
	 *
	 * @param anotherSds 比较的字符串
	 * @return > 0: 比入参的字符串大, = 0: 和入参的字符串一样大 < 0: 比入参的字符串小
	 */
	public int sdscmp(Sds anotherSds) {

		if (Objects.isNull(anotherSds)) {
			return 1;
		}

		if (this == anotherSds) {
			return 0;
		}

		int minLen = Math.min(sdsLen(), anotherSds.sdsLen());

		for (int i = 0; i < minLen; i++) {
			if (pos(i) != anotherSds.pos(i)) {
				return pos(i) - anotherSds.pos(i);
			}
		}

		// 比较那个当前的字符串的长度是否大于入参的字符串的长度
		return Integer.compare(sdsLen(), anotherSds.sdsLen());
	}

	/**
	 * 获取字符串的长度
	 *
	 * @return 字符串的长度
	 */
	protected abstract int sdsLen();

	/**
	 * 获取指定位置的字符
	 *
	 * @param index 查询的位置
	 * @return 返回指定位置的字符
	 */
	public char pos(int index) {
		return buf[index];
	}

}
