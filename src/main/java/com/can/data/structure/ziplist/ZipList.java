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

	private final byte[] zipList;

	/**
	 * zlbytes 的存储长度
	 */
	private final static int ZL_BYTES_LEN = 4;

	/**
	 * zltail 的存储长度 (entry 都是 byte 的倍数), 所以 zlTail 里面存储的数组的下标
	 */
	private final static int ZL_TAIL_LEN = 4;

	/**
	 * zl len 的存储长度
	 */
	private final static int ZL_LEN_LEN = 2;

	/**
	 * 压缩列表头部的大小
	 * zlbytes 4 个字节
	 * zltail 4 个字节
	 * zllen 2 个字节
	 */
	private final static int ZIPLIST_HEADER_SIZE = ZL_BYTES_LEN + ZL_TAIL_LEN + ZL_LEN_LEN;

	/**
	 * 压缩列表结束的标识 255
	 */
	private final static byte ZIP_END = (byte) 0xFF;

	/**
	 * 8 位二进制最大值
	 */
	private final static int MAX_8_BIT_VALUE = 0xFF;

	/**
	 * 长度的分隔值
	 */
	private final static int ZIP_BIG_PREVLEN = 0xFE

	/**
	 * int 的位数是 byte 的 4 倍
	 */
	private final static int COUNT_FROM_INT_BIT_TO_BYTE = Integer.SIZE / Byte.SIZE;

	public ZipList() {

		// 头部长度 + zlend 1 个字节
		int length = ZIPLIST_HEADER_SIZE + 1;
		byte[] list = new byte[length];
		updateZlBytes(length);
		updateZlTail(ZIPLIST_HEADER_SIZE);
		updateZlLen((short) 0);
		list[length - 1] = ZIP_END;
		this.zipList = list;
	}

	public void insert() {

		int curLen = getZlBytes();

		// 已经有元素了
		if (zipList[ZIPLIST_HEADER_SIZE] != ZIP_END) {

		} else {

		}

	}

	private int ZIP_DECODE_PREVLEN(byte[] a) {

		int prevlensize = ZIP_DECODE_PREVLENSIZE(a);
		int prevlen = 0;
		if (prevlensize == 1) {
			prevlen = a[0];
		} else {
			prevlen = ((int) a[1]) << 24) |((int) a[2]) << 16) |((int) a[3]) << 8) |a[4];
		}
		return prevlen;
	}

	private int ZIP_DECODE_PREVLENSIZE(byte[] a) {
		return a[0] < (byte) ZIP_BIG_PREVLEN ? 1 : 5;
	}

	/**
	 * 更新 压缩列表的 zlBytes 的值
	 *
	 * @param zlBytesLen
	 */
	private void updateZlBytes(int zlBytesLen) {

		// zipList 的前 10 个字节 [][][][]  [][][][]  [][]
		// 将 zlBytesLen 的 4 个字节 a b c d 放到 zipList 的前 4 个字节的位置 [a][b][c][d]  [][][][]  [][]
		for (int i = 0; i < COUNT_FROM_INT_BIT_TO_BYTE - 1; i++) {
			// 右移位数
			int rightMoveBitCount = (COUNT_FROM_INT_BIT_TO_BYTE - 1 - i) * Byte.SIZE;
			zipList[i] = (byte) ((zlBytesLen >> rightMoveBitCount) & MAX_8_BIT_VALUE);
		}
	}

	/**
	 * 更新 压缩列表的 zlTail 的值
	 *
	 * @param zlTailLen
	 */
	private void updateZlTail(int zlTailLen) {

		// zipList 的前 10 个字节 [][][][]  [][][][]  [][]
		// 将 zlTailLen 的 4 个字节 a b c d 放到 zipList 的前 5 到 8 位置 [][][][]  [a][b][c][d]  [][]

		for (int i = 0; i < COUNT_FROM_INT_BIT_TO_BYTE - 1; i++) {
			// 右移位数
			int rightMoveBitCount = (COUNT_FROM_INT_BIT_TO_BYTE - 1 - i) * Byte.SIZE;
			zipList[ZL_BYTES_LEN + i] = (byte) ((zlTailLen >> rightMoveBitCount) & MAX_8_BIT_VALUE);
		}
	}

	/**
	 * 更新 压缩列表的 zlLen 的值
	 *
	 * @param zlLen
	 */
	private void updateZlLen(short zlLen) {

		// zipList 的前 10 个字节 [][][][]  [][][][]  [][]
		// 将 zlLen 的 2 个字节 a b 放到 zipList 的前 9 到 10 位置 [][][][]  [][][][]  [a][b]

		// [][][][]  [][][][]  [*][*] 将 contentLen 存到数组索引 8,9 位置
		zipList[ZIPLIST_HEADER_SIZE - 1] = (byte) (zlLen & MAX_8_BIT_VALUE);
		zipList[ZIPLIST_HEADER_SIZE - 2] = (byte) ((zlLen >> Byte.SIZE) & MAX_8_BIT_VALUE);
	}

	/**
	 * 获取 zlBytes 的值
	 *
	 * @return
	 */
	private int getZlBytes() {

		int zlBytes = 0;
		for (int i = 0; i < COUNT_FROM_INT_BIT_TO_BYTE - 1; i++) {
			// 左移的位数
			int leftMoveBitCount = Byte.SIZE * i;
			zlBytes = (zlBytes << leftMoveBitCount) | zipList[i];
		}
		return zlBytes;
	}

	/**
	 * 获取 zlBytes 的值
	 *
	 * @return
	 */
	private int getZlTail() {

		int zlTail = 0;
		for (int i = 0; i < COUNT_FROM_INT_BIT_TO_BYTE - 1; i++) {
			// 左移的位数
			int leftMoveBitCount = Byte.SIZE * i;
			zlTail = (zlTail << leftMoveBitCount) | zipList[ZL_BYTES_LEN + i];
		}
		return zlTail;
	}

	/**
	 * 获取 zllen 的值
	 *
	 * @return
	 */
	private short getZlLen() {

		short zlLen = 0;
		zlLen = (short) (zlLen | zipList[ZIPLIST_HEADER_SIZE - 2]);
		zlLen = (short) ((zlLen << Byte.SIZE) | zipList[ZIPLIST_HEADER_SIZE - 1]);
		return zlLen;
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
