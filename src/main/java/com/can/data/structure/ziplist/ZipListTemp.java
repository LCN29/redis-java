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
 * 当前一 个元素的长度大于或等于 254 字节时，用 5 个字节来表示, 这 5 个字节中 第一个固定为 0xFE(1111 1110, 既 254)
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
public class ZipListTemp {

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
	private final static int ZIP_BIG_PREVLEN = 0xFE;

	// 255 	11111111
	private final static int ZIP_ENCODING_SIZE_INVALID = 0xff;


	// 192 11000000
	private final static int ZIP_STR_MASK = 0xc0;
	// 48 00110000
	private final static int ZIP_INT_MASK = 0x30;
	// 0 00000000
	private final static int ZIP_STR_06B = (0 << 6);
	// 64 01000000
	private final static int ZIP_STR_14B = (1 << 6);
	// 128 10000000
	private final static int ZIP_STR_32B = (2 << 6);
	// 192 | 0,  11000000 | 0, 11000000
	private final static int ZIP_INT_16B = (0xc0 | 0 << 4);
	// 192 | 16, 11000000 | 00010000, 11010000
	private final static int ZIP_INT_32B = (0xc0 | 1 << 4);
	// 192 | 32, 11000000 | 00100000, 11100000
	private final static int ZIP_INT_64B = (0xc0 | 2 << 4);
	// 192 | 48, 11000000 | 00110000, 11110000
	private final static int ZIP_INT_24B = (0xc0 | 3 << 4);
	// 254 11111110
	private final static int ZIP_INT_8B = 0xfe;
	// 241 11110001
	private final static int ZIP_INT_IMM_MIN = 0xf1;
	// 253 11111101
	private final static int ZIP_INT_IMM_MAX = 0xfd;


	/**
	 * int 的位数是 byte 的 4 倍
	 */
	private final static int COUNT_FROM_INT_BIT_TO_BYTE = Integer.SIZE / Byte.SIZE;

	public ZipListTemp() {

		// 头部长度 + zlend 1 个字节
		int length = ZIPLIST_HEADER_SIZE + 1;
		byte[] list = new byte[length];
		updateZlBytes(length);
		updateZlTail(ZIPLIST_HEADER_SIZE);
		updateZlLen((short) 0);
		list[length - 1] = ZIP_END;
		this.zipList = list;
	}

	private int ZIP_DECODE_PREVLEN(int pos) {

		int prevlensize = ZIP_DECODE_PREVLENSIZE(pos);
		int prevlen = 0;
		if (prevlensize == 1) {
			prevlen = zipList[pos];
		} else {
			prevlen = ((int) zipList[pos + 1]) << 24) |((int) zipList[pos + 2]) << 16) |((int) zipList[pos + 3]) << 8) | zipList[pos + 4];
		}
		return prevlen;
	}

	private int ZIP_DECODE_PREVLENSIZE(int pos) {
		return zipList[pos] < (byte) ZIP_BIG_PREVLEN ? 1 : 5;
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


	private int ziplist_entry_head() {
		return ZIPLIST_HEADER_SIZE;
	}

	private int ziplist_entry_end() {
		return getZlBytes() - 1;
	}


	private void push(char[] content, int contentLen, int where) {

		int pos = where == 0 ? ziplist_entry_head() : ziplist_entry_end();
		insert(pos, content, contentLen);
	}


	/**
	 * 插入元素
	 *
	 * @param pos        插入的位置
	 * @param content    插入的内容
	 * @param contentLen 插入的内容长度
	 */
	public void insert(int pos, char[] content, int contentLen) {

		// curLen 当前 ziplist 占用了多少个字节
		int curLen = getZlBytes(), reqlen = 0, newlen = 0;

		// 存储位置的前一个元素的字节长度
		int prevlen = 0;

		// 存储位置的前一个元素的字节长度需要的字节长度
		int prevlensize = 0;

		int prevlen = 0;
		int offset = 0;
		int nextdiff = 0;
		char encoding = 0;

		long value = 123456789L;

		// 1. 列表没有数据的插入
		// 2. 列表有数据, 在尾部插入
		// 3. 列表有数据, 在中间插入
		// 4. 列表有数据, 在首部插入

		// 已经有元素了, ziplist[10] 不是 zipEnd 列表中有数据了

		// 需要插入的位置不是结束标识, 也就是插入的位置已经有内容了
		// 情况 3, 4
		if (zipList[pos] != ZIP_END) {

			prevlensize = ZIP_DECODE_PREVLENSIZE(pos);
			prevlen = ZIP_DECODE_PREVLEN(pos);

		} else {

			// 需要插入的位置是结算标识, 没有内容了
			// 情况 1, 2

			// 最后一个 entry 的起始位置
			int lastEntryStartPos = getZlTail();

			// 最后一个 entry 的起始位置不是 结束标识, 也就是情况 2
			// 情况 1, prevlensize prevlen 默认值为 0
			if (zipList[lastEntryStartPos] != ZIP_END) {
				prevlen = zipRawEntryLengthSafe(curLen, lastEntryStartPos);
			}
		}


		/* See if the entry can be encoded */
		if (zipTryEncoding(s, slen, &value, &encoding)) {
			/* 'encoding' is set to the appropriate integer encoding */
			reqlen = zipIntSize(encoding);
		} else {
			/* 'encoding' is untouched, however zipStoreEntryEncoding will use the
			 * string length to figure out how to encode it. */
			reqlen = slen;
		}
		/* We need space for both the length of the previous entry and
		 * the length of the payload. */
		reqlen += zipStorePrevEntryLength(NULL,prevlen);
		reqlen += zipStoreEntryEncoding(NULL,encoding,slen);

		/* When the insert position is not equal to the tail, we need to
		 * make sure that the next entry can hold this entry's length in
		 * its prevlen field. */
		int forcelarge = 0;
		nextdiff = (p[0] != ZIP_END) ? zipPrevLenByteDiff(p,reqlen) : 0;
		if (nextdiff == -4 && reqlen < 4) {
			nextdiff = 0;
			forcelarge = 1;
		}

		/* Store offset because a realloc may change the address of zl. */
		offset = p-zl;
		newlen = curlen+reqlen+nextdiff;
		zl = ziplistResize(zl,newlen);
		p = zl+offset;

		/* Apply memory move when necessary and update tail offset. */
		if (p[0] != ZIP_END) {
			/* Subtract one because of the ZIP_END bytes */
			memmove(p+reqlen,p-nextdiff,curlen-offset-1+nextdiff);

			/* Encode this entry's raw length in the next entry. */
			if (forcelarge)
				zipStorePrevEntryLengthLarge(p+reqlen,reqlen);
			else
				zipStorePrevEntryLength(p+reqlen,reqlen);

			/* Update offset for tail */
			ZIPLIST_TAIL_OFFSET(zl) =
					intrev32ifbe(intrev32ifbe(ZIPLIST_TAIL_OFFSET(zl))+reqlen);

			/* When the tail contains more than one entry, we need to take
			 * "nextdiff" in account as well. Otherwise, a change in the
			 * size of prevlen doesn't have an effect on the *tail* offset. */
			assert(zipEntrySafe(zl, newlen, p+reqlen, &tail, 1));
			if (p[reqlen+tail.headersize+tail.len] != ZIP_END) {
				ZIPLIST_TAIL_OFFSET(zl) =
						intrev32ifbe(intrev32ifbe(ZIPLIST_TAIL_OFFSET(zl))+nextdiff);
			}
		} else {
			/* This element will be the new tail. */
			ZIPLIST_TAIL_OFFSET(zl) = intrev32ifbe(p-zl);
		}

		/* When nextdiff != 0, the raw length of the next entry has changed, so
		 * we need to cascade the update throughout the ziplist */
		if (nextdiff != 0) {
			offset = p-zl;
			zl = __ziplistCascadeUpdate(zl,p+reqlen);
			p = zl+offset;
		}

		/* Write the entry */
		p += zipStorePrevEntryLength(p,prevlen);
		p += zipStoreEntryEncoding(p,encoding,slen);
		if (ZIP_IS_STR(encoding)) {
			memcpy(p,s,slen);
		} else {
			zipSaveInteger(p,value,encoding);
		}
		ZIPLIST_INCR_LENGTH(zl,1);
		return zl;

	}

	int zipTryEncoding(unsigned char *entry, unsigned int entrylen, long long *v, unsigned char *encoding) {
		long long value;

		if (entrylen >= 32 || entrylen == 0) return 0;
		if (string2ll((char*)entry,entrylen,&value)) {
			/* Great, the string can be encoded. Check what's the smallest
			 * of our encoding types that can hold this value. */
			if (value >= 0 && value <= 12) {
            *encoding = ZIP_INT_IMM_MIN+value;
			} else if (value >= INT8_MIN && value <= INT8_MAX) {
            *encoding = ZIP_INT_8B;
			} else if (value >= INT16_MIN && value <= INT16_MAX) {
            *encoding = ZIP_INT_16B;
			} else if (value >= INT24_MIN && value <= INT24_MAX) {
            *encoding = ZIP_INT_24B;
			} else if (value >= INT32_MIN && value <= INT32_MAX) {
            *encoding = ZIP_INT_32B;
			} else {
            *encoding = ZIP_INT_64B;
			}
        *v = value;
			return 1;
		}
		return 0;
	}


	private int zipRawEntryLengthSafe(int curLen, int entryStartPos) {

		ZlEntry e = zipEntrySafe(curLen, entryStartPos);
		return e.getHeaderSize() + e.getLen();;
	}

	private int ZIP_ENTRY_ENCODING(int encodingStartPos, ZlEntry entry) {

		entry.setEncoding(zipList[encodingStartPos]);

		// 小于 11000000，也就是字节数组, 重试设值
		if (zipList[encodingStartPos] < (byte) ZIP_STR_MASK) {
			// 字节数组 只取前 2 位即可，00 01 10 11 4 种编码的情况
			entry.setEncoding(zipList[encodingStartPos] & ZIP_STR_MASK);
		}
	}

	int zipEncodingLenSize(int encoding) {
		if (encoding == ZIP_INT_16B || encoding == ZIP_INT_32B ||
				encoding == ZIP_INT_24B || encoding == ZIP_INT_64B ||
				encoding == ZIP_INT_8B)
			return 1;
		if (encoding >= ZIP_INT_IMM_MIN && encoding <= ZIP_INT_IMM_MAX)
			return 1;
		if (encoding == ZIP_STR_06B)
			return 1;
		if (encoding == ZIP_STR_14B)
			return 2;
		if (encoding == ZIP_STR_32B)
			return 5;
		return ZIP_ENCODING_SIZE_INVALID;
	}

	private int ZIP_DECODE_LENGTH(int encodingStartPos, ZlEntry entry) {

		int lensize = 0;
		int len = 0;

		//   < 11000000
		if (zipList[encodingStartPos] < (byte)ZIP_STR_MASK) {
			// 字节数组

			if ((zipList[encodingStartPos] & ZIP_STR_MASK) == ZIP_STR_06B) {
				lensize = 1;
				// 00 111111
				len = zipList[encodingStartPos] & 0x3f;
			} else if ((zipList[encodingStartPos] & ZIP_STR_MASK) == ZIP_STR_14B) {
				lensize = 2;
				len = ((zipList[encodingStartPos] & 0x3f) << 8) | zipList[encodingStartPos + 1];
			} else if ((zipList[encodingStartPos] & ZIP_STR_MASK) == ZIP_STR_32B) {
				lensize= 5;
				len =  (zipList[encodingStartPos + 1] << 24) | (zipList[encodingStartPos + 2]<< 16) | ( zipList[encodingStartPos + 3]<<  8) | (zipList[encodingStartPos + 4]);
			} else {
				lensize = len = 0;
			}

		} else {
			// 整数
			lensize = 1;

			if (zipList[encodingStartPos] ==  ZIP_INT_8B) {
				// 11111110
				len = 1;
			} else if (zipList[encodingStartPos] ==  ZIP_INT_16B) {
				// 11000000
				len = 2;
			} else if (zipList[encodingStartPos] ==  ZIP_INT_24B) {
				// 11110000
				len = 3;
			} else if (zipList[encodingStartPos] ==  ZIP_INT_32B) {
				// 11010000
				len = 4
			} else if (zipList[encodingStartPos] ==  ZIP_INT_64B) {
				//11100000
				len = 8
			} else if (zipList[encodingStartPos]>= ZIP_INT_IMM_MIN && zipList[encodingStartPos]<= ZIP_INT_IMM_MAX) {
				//  11110001 <= x <= 11111101
				len = 0;
			} else {
				// 其他情况
				lensize = len = 0;
			}
		}
		entry.setLen(len);
		entry.setLensize(lensize);
	}

	private boolean OUT_OF_RANGE(int current, int start, int end) {

		if (current < start || current > end) {
			return true;
		}

		return false;
	}

	private ZlEntry zipEntrySafe(int curLen, int entryStartPos, int validatePrevlen) {

		// 第一个元素开始的位置
		int firstEntryPos = ZIPLIST_HEADER_SIZE;
		// 最后一个元素的结束位置 当前列表的长度 减去末尾的结束标识
		int lastEntryEndPos = curLen - 1;

		ZlEntry entry = new ZlEntry();

		// 如果没有头溢出压缩列表的可能，就走捷径（最大的 lensize 和 prevrawlensize 都是5字节）
		if (entryStartPos >= firstEntryPos && entryStartPos + 10 < lastEntryEndPos) {

			entry.setPrevrawlensize(ZIP_DECODE_PREVLENSIZE(entryStartPos));
			entry.setPrevrawlen(ZIP_DECODE_PREVLENSIZE(entryStartPos));
			ZIP_ENTRY_ENCODING(entryStartPos + entry.getPrevrawlen(), entry);
			ZIP_DECODE_LENGTH(entryStartPos + entry.getPrevrawlen(), entry);
			entry.setHeaderSize(entry.getPrevrawlensize() + entry.getLensize());
			// TODO
			//entry.content

			if (entry.getLensize() == 0) {
				throw new RuntimeException("异常")；
			}

			if (OUT_OF_RANGE(entry.getHeaderSize() + entry.getLen())) {
				throw new RuntimeException("异常");
			}

			if (validatePrevlen == 1 && OUT_OF_RANGE(entry.getPrevrawlen())) {
				throw new RuntimeException("异常");
			}

			// 数组索引为 0
			// 将 src 数组的 1 位开始，拷贝 2 个元素到 descArr 数组中，位置从数组的 2 开始
			//System.arraycopy(srcArr, 1, descArr, 3, 2);

			// TODO
			entry.setContent();

			// TODO
			return null;
		}

		if (OUT_OF_RANGE(entryStartPos))
			throw new RuntimeException("异常");

		entry.setPrevrawlensize(ZIP_DECODE_PREVLENSIZE(entryStartPos));

		if (OUT_OF_RANGE(entry.getPrevrawlensize()))
			throw new RuntimeException("异常");

		ZIP_ENTRY_ENCODING(entryStartPos + entry.getPrevrawlen(), entry);
		entry.setLensize((int)entry.getEncoding());
		if (entry.getLensize() == ZIP_ENCODING_SIZE_INVALID) {
			throw new RuntimeException("异常");
		}

		if (OUT_OF_RANGE(entry.getPrevrawlensize() + entry.getLensize())) {
			throw new RuntimeException("异常");
		}

		entry.setPrevrawlensize(ZIP_DECODE_PREVLENSIZE(entryStartPos));
		entry.setPrevrawlen(ZIP_DECODE_PREVLEN(entryStartPos));
		entry.setHeaderSize(entry.getLen() + entry.getHeaderSize());


		if (OUT_OF_RANGE(entry.getHeaderSize() + entry.getLen())) {
			throw new RuntimeException("异常")；
		}

		if (validatePrevlen == 1 && OUT_OF_RANGE(entry.getPrevrawlen())) {
			throw new RuntimeException("异常")；
		}

		// 数组索引为 0
		// 将 src 数组的 1 位开始，拷贝 2 个元素到 descArr 数组中，位置从数组的 2 开始
		//System.arraycopy(srcArr, 1, descArr, 3, 2);

		// TODO
		entry.setContent();

		return null;
	}


	@Data
	private static class ZlEntry {

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
		 * content 占用的字节数
		 */
		int len;

		/**
		 * 数据类型
		 * 0000 0000 长度最大为 63 的字节数组
		 * 0100 0000 最大长度为 2^14 - 1 的字节数组
		 * 1000 0000 最大长度为 2^32 - 1 的字节数组
		 * 1100 0000 整数
		 *
		 */
		byte encoding;

		/**
		 * 头部的长度占用的长度 prevrawlensize + lensize
		 */
		int headerSize;

		/**
		 * 存储的内容，包括 previous_entry_length + encoding + content
		 */
		char[] content;

		private void zipDecodePrevlen() {

		}

		private void zipDecodeLength() {

			// 192, 1100 0000
			int a = 0xc0;


		}
	}
}
