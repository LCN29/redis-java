package com.can.data.structure.sds;

/**
 * <pre>
 * 	Java 中数组的最大容量为 Integer.MAX_VALUE
 * 	所以 没法声明出 char[] buf = new char[long] 的数组
 * 	拆分为二维数组, char[][] buf = new char[long][Integer.MAX>VALUE]
 * 	数组的第一纬度的长度依旧是为 long 的类型 Long.MAX_VALUE / Integer.MAX_VALUE = 4294967298,
 * 	也是 2 个 Integer.MAX_VALUE 还多一点, 所以 数组长度为 long 的在 Java 中没有实现
 * </pre>
 *
 * @author
 * @date 2021-11-08  14:44
 */
public class SdsHdr64 {

	/**
	 * 已经使用的长度
	 */
	private long len;

	/**
	 * 申请的字符长度，不包含 Header 结构和最后的空终止字符
	 */
	private long alloc;


}
