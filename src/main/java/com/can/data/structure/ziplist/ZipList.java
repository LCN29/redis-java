package com.can.data.structure.ziplist;

import lombok.Data;

/**
 * <pre>
 *
 * <zlbytes> <zltail> <zllen> <entry> <entry> ... <entry> <zlend>
 *
 * zlbytes: 压缩列表的字节长度，占 4 个字节，因此压缩列表最多有 23 ^ 2 - 1 个字节
 * zltail:  压缩列表尾元素和压缩列表起始地址的偏移量，占 4 个字节, 压缩链表的起始位置 + zltail = 当前压缩列表最后一个 entry 的起始位置
 * zllen:   压缩列表的元素个数，占 2 个字节, zllen 无法存储元素个数超过 65535（216 - 1）的压缩列表, 必须遍历整个压缩列表才能获取到元素个数
 * entryX:  压缩列表存储的元素，可以是字节数组或者整数，长度不限
 * zlend:   压缩列表的结尾，占 1 个字节，恒为 0xFF
 *
 *
 * entry
 * <previous_entry_length> <encoding> <content>
 *
 * previous_entry_length: 表示前一个元素的字节长度，占 1 个或者 5 个字节, 当前一个元素的长度小于 254 字节时，用 1 个字节表示;
 * 当前一 个元素的长度大于或等于 254 字节时，用 5 个字节来表示, 这 5 个字节中 第一个固定为 0xFE(1111 1110, 254)
 * 不用 255 表示的话，是 zlend 已经使用了, 遇到 255 表示达到了压缩列表的尾部了
 *
 * encoding: 当前元素的编码, 不同的编码，表示后面的 content 是不同的内容
 * 1 个字节 00 bbbbbb (6 个比特),  长度最大为 63 的字节数组
 * 1 个字节 11 00 0000, int 16 的整数
 * 1 个字节 11 01 0000, int 32 的整数
 * 1 个字节 11 10 0000, int 64 的整数
 * 1 个字节 11 11 0000, 24 位的整数
 * 1 个字节 11 11 1110, 8 位的整数
 * 1 个字节 11 11 xxxx, 没有 content 字段, 内容一并存储在 encoding 中, xxxx 表示 0 ~ 12 的整数, xxxx 4位只能用于表示 0001 到 1101, 既 1 ~ 13 情况 (避免和上面的 24 位和 8 位整数的影响), 然后 - 1，就得到可以表示 0 ~ 12 的整数。
 * 2 个字节 01 bbbbbb xxxxxxxx, 14 个位表示数组的长度，最大长度为 2^14 - 1 的字节数组
 * 5 个字节 10__ aaaaaaaa bbbbbbbb cccccccc dddddddd 32 为表示数组的长度, 最大长度为 2^32 - 1 的字节数组
 *
 * 根据encoding字段第1个字节的前2位，可以判断content 字段存储的是整数或者字节数组（及其最大长度）。当content存储的是 字节数组时，后续字节标识字节数组的实际长度；当content存储的是整 数时，可根据第3、第4位判断整数的具体类型。而当encoding字段标识 当前元素存储的是0～12的立即数时，数据直接存储在encoding字段的最 后4位，此时没有content字段。参照encoding字段的编码表格，Redis预 定义了以下常量对应encoding字段的各编码类型
 *
 * </pre>
 *
 * @author
 * @date 2021-11-10  15:43
 */
public class ZipList {


	public ZipList() {

	}


	@Data
	private static class Entry {

		/**
		 * previous_entry_length 占用了多少个字节 1 或者 5
		 */
		int prevrawlensize;

		/**
		 * previous_entry_length 的值
		 */
		int prevrawlen;

		/**
		 * encoding 占用了多少个字节  1,2,5
		 */
		int lensize;

		/**
		 * 元素数据内容的长度
		 */
		int len;

		/**
		 * 数据类型
		 */
		char encoding;

		int headerSize;

		char content;

		private void zipDecodePrevlen() {

		}

		private void zipDecodeLength() {

			// 192, 1100 0000
			int a = 0xc0;


		}
	}
}
