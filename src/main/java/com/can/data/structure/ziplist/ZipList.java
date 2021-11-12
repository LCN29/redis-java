package com.can.data.structure.ziplist;

/**
 * <pre>
 *
 * </pre>
 *
 * @author
 * @date 2021-11-12  18:24
 */
public class ZipList {


	private static class zlEntry {


		/**
		 * 当前 zlEntry 在字节数组中的开始位置
		 * 当前 entry 在字节数组中的开始位置, 也就是指向这个 entry 的 previous_entry_length 属性的内存位置
		 */
		private int p;
	}
}
