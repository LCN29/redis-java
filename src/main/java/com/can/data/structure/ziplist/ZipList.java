package com.can.data.structure.ziplist;

import com.can.util.Util;

import java.util.Objects;

/**
 * <pre>
 *
 * </pre>
 *
 * @author
 * @date 2021-11-12  18:24
 */
public class ZipList extends AbstractZipList {

	/**
	 * 向压缩列表的头部新增元素的标识
	 */
	private final static byte PUSH_HEAD_POS_MASK = 0;

	/**
	 * 向压缩列表的尾部新增元素的标识
	 */
	private final static byte PUSH_TAIL_POS_MASK = 1;

	/**
	 * 存储数据的字节数组
	 */
	private byte[] zipList;

	public ZipList() {
		this.zipList = initZipList();
	}

	/**
	 * 初始出一个压缩列表的字节数组
	 *
	 * @return 初始出来的字节数组
	 */
	private byte[] initZipList() {

		// 头部长度 + zLend 1 个字节
		int length = ZIPLIST_HEADER_SIZE + 1;

		// 初始的 zipList 只有 zlBytes + zlTail + zlLen + zLend, 也就是 4 + 4 + 2 + 1 个字节
		byte[] zipList = new byte[length];

		// 将当前的 zipList 的字节长度存入到 zipList zlBytes 位置中
		setVal2ZlBytes(zipList, length);

		// 当前没有元素, 所以 zipList 到第一个 entry 的长度就是昂起头部的长度
		setVal2ZlTail(zipList, ZIPLIST_HEADER_SIZE);

		// 当前没有元素, 所以已有的元素个数为 0, 存储 entry 个数的字节只有 2
		setVal2ZlLen(zipList, (short) 0);

		// 初始数组的最后一位为结束标识符
		zipList[length - 1] = ZIP_END;

		return zipList;
	}

	/**
	 * 向压缩列表的头部新增一个 entry
	 *
	 * @param content 新增的内容
	 * @return 新增结果
	 */
	public boolean lPush(byte[] content) {
		// 获取头部的位置
		int insertPos = getZipListEntryHeadPos();
		return insert(content, insertPos);
	}

	/**
	 * 向压缩列表的尾部新增一个 entry
	 *
	 * @param content 新增的内容
	 * @return 新增结果
	 */
	public boolean rPush(byte[] content) {
		// 获取尾部的位置
		int insertPos = getZipListEntryTailPos(zipList);
		return insert(content, insertPos);
	}

	/**
	 * 向后遍历
	 */
	public void zipListNext() {

		int findPos = ZIPLIST_HEADER_SIZE;

		ZlEntry zlEntry;
		while (zipList[findPos] != ZIP_END) {
			zlEntry = unZip2ZlEntry(zipList, findPos);

			// int memcmp(const void *str1, const void *str2, size_t n))
			// str1  指向内存块的指针
			// str2  指向内存块的指针
			// n	 比较内存的多少个字节


			// 下一个遍历的位置
			findPos = findPos + zlEntry.getHeaderSize() + zlEntry.getLen();
		}
	}

	/**
	 * 向前遍历
	 */
	public void zipListPre() {

		int findPos = getZlTailValFromZl(zipList);

		ZlEntry zlEntry;
		while (findPos == getZipListEntryHeadPos()) {

			zlEntry = unZip2ZlEntry(zipList, findPos);


			findPos = findPos - zlEntry.getPrevRawLen();
		}
	}


	public ZlEntry find(byte[] content) {

		int findPos = ZIPLIST_HEADER_SIZE;

		ZlEntry zlEntry;
		while (zipList[findPos] != ZIP_END) {
			zlEntry = unZip2ZlEntry(zipList, findPos);

			// c 语言中有一个函数 int memcmp(const void *str1, const void *str2, size_t n))
			// str1  指向内存块的指针  str2  指向内存块的指针 n	 比较内存的多少个字节
			// 返回值  0 相同, 不等于 0 不相同


			if (isZip2StrByEncodingValue(zlEntry.getEncoding())) {
				// 压缩为字符串
				byte[] compareArr = new byte[zlEntry.getLen()];
				System.arraycopy(zipList, zlEntry.getP(), compareArr, 0, zlEntry.getLen());
				if (zlEntry.getLen() == content.length && memcmp(compareArr, content, content.length) == 0) {
					return zlEntry;
				}
			} else {
				// 数字

				// 通过 zipTryEncoding 尝试将入参的 content 转为整数
				// 转换失败继续遍历
				// 通过 zipLoadInteger 将 entry 的内容转为 整数
				// 2 个进行比较
			}

			// 下一个遍历的位置
			findPos = zlEntry.getHeaderSize() + zlEntry.getLen();
		}

		return null;
	}


	/**
	 * 比较 2 个字节数组的前 n 个字节
	 *
	 * @param content    比较的字节数组 1
	 * @param content2   比较的字节数组 2
	 * @param compareLen 比较的长度
	 * @return 比较结果 0: 一样
	 */
	private int memcmp(byte[] content, byte[] content2, int compareLen) {

		return 0;
	}


	/**
	 * 向压缩列表的指定位置新增一个 entry
	 *
	 * @param content   新增的内容
	 * @param insertPos 新增的位置
	 * @return 新增结果
	 */
	private boolean insert(byte[] content, int insertPos) {

		// 向压缩列表插入元素有 4 种情况
		// 1. 列表没有数据的插入
		// 2. 列表有数据, 在尾部插入
		// 3. 列表有数据, 在中间插入
		// 4. 列表有数据, 在首部插入

		// entry 的 previous_entry_length 存储的时上一个 entry 的字节长度, 所以第一步是获取上一个元素的 previous_entry_length 的内容

		// 获取需要插入
		int prevLenSize = 0;
		int preLen = 0;

		// 需要插入的数据的上一个 entry 的位置, 默认为插入的位置
		int preEntryPos = insertPos;

		// 压缩列表中没有 entry
		boolean noEntry = false;

		// 需要插入的位置是压缩列表的尾部
		if (zipList[insertPos] == ZIP_END) {

			noEntry = true;
			// 情况 1, 2
			// 压缩列表 zlTail 的位置的第一个字节不是结束标识, 标识插入的压缩列表当前有数据
			// 如果插入的压缩列表没有数据, 那么 zlTail 指向的位置就是压缩列表的末尾, 此时的值为结束标识
			if (zipList[getZlTailValFromZl(zipList)] != ZIP_END) {
				preEntryPos = getLastEntryStartPos(zipList);
				noEntry = false;
			}
		}

		// 有 entry, 获取上一个 entry 的 previous_entry_length 所占的字节和 previous_entry_length 的值
		if (!noEntry) {
			prevLenSize = getPosEntryPrevRawLenSize(zipList, preEntryPos);
			preLen = getPosEntryPrevLenValue(zipList, preEntryPos, prevLenSize);
		}

		// 当前存储字符串对应的 encoding
		byte encoding = 0;
		// 分配当前的 entry content 需要的字节长度
		int reqLen = 0;

		// 尝试将入参的字符串转为 long 数值
		Long value = Util.string2Long(content);

		if (Objects.nonNull(value)) {
			// 转换成功, 计算 encoding
			encoding = getEncodingFromCurValue(value);
			reqLen = getByteSizeFromNumberEncoding(encoding);
		} else {
			// 无法转为整数存储, 那么数组有多长, 那么就需要多少个字节
			reqLen = content.length;
			// 这里不出计算 encoding 的值, 猜测是如果是字符串数组, encoding 是的多个字节的, 一个 byte 是存不下的
			// 那么 encoding 的处理直接留到后面 zipList 插入时操作
		}

		// reqLen 的长度 = previous_entry_length + content
		reqLen += prevLenSize;

		// previous_entry_length + encoding + content, 这时候 reqLen 就是当前存储内容需要的字节数
		reqLen += getStoreEncodingByteNumber(encoding, content);

		// 插入的位置不是尾部, 那么要确保插入位置的下一个 entry 的 previous_entry_length 足够存储这个 entry 的大小
		int forceLarge = 0;

		// 存储当前 entry 的 previous_entry_length 当前需要的字节长度 - 插入位置的 previous_entry_length 的字节长度,
		// 得到 下一个 entry 的 previous_entry_length 的变化值, 增大 4 个字节, 减小 4 个字节, 不变
		int nextDiff = zipList[insertPos] == ZIP_END ? 0 : getCurEntryPreviousEntryLengthNeedByteNum(reqLen) - prevLenSize;

		// 当前的 entry 只需要 1 个字节存储, 但是下一个 entry 的 previous_entry_length 有 5 个字节, 特殊处理
		if (reqLen < 4 && nextDiff == -4) {

			// reqLen < 4 正常情况下都是 false,
			// -4, 说明在插入当前 entryX 时， entryX - 1, entryX + 2,  entryX+2 的 previous_entry_length = 5
			// 那么 entryX 插入时 previous_entry_length 理所应当为 5, 那么 reqLen 必定是 > 5 的, 不会出现 < 4 的
			// 但是在 连锁更新 时的还是可能出现特殊情况
			// 连锁更新，假设现在有一个列表 A(p_e_l=5) B(p_e_l=5) C(p_e_l=5), 这时候如果插入一个只需要 1 个字节的 entry
			// I(p_e_l=5) A(p_e_l=1) A 需要的字节数变小了, 假设刚好变为只需要 1 个字节，那么后面的 B 的 previous_entry_length 也需要变小
			// 导致 B 也变为只需要 1 个字节了, 导致 C 也需要变小, 从而引起了后面的连续更新


			// 强制将 nextDiff 设置为 0, -4 说明需要的空间变小了, 重新分配内存时, 可能会减少空间
			// realloc 方法分配内存, 可能会将多余的空间回收，导致数据丢失。需要避免这种情况的发生，即重新赋值 nextDiff=0,
			// 同时用 forceLarge 表示出现了这种情况
			nextDiff = 0;
			forceLarge = 1;
		}

		// 当前数组的长度
		int curLen = zipList.length;
		// 新数组的个数
		int newLen = curLen + reqLen + nextDiff;
		// 重新分配数组, 里面会更新 zlBytes
		this.zipList = zipListResize(newLen);

		if (zipList[insertPos] != ZIP_END) {
			// 不是末尾插入

			// 把当前数组的 insertPos - nextDiff 后的 curLen - insertPos - 1 + nextDiff 个字节拷贝到 insertPos + reqLen 的位置的后面

			// nextDiff = -4 的话，表示当前的 entry 只需要 1 个字节就可以存储长度, 而原本下一个 entry 有 5 个字节, 重新分配后, 只需要给 1 个字节
			// 所以分配时，下一个 entry 前面的 4 个字节可以不要了, 即从 insertPos + |-nextDiff| 的位置, insertPos + 4 的位置分配即可
			// 同理 nextDiff = 4, 类似的原理
			// 分配的长度 = 原本数组的长度 - insertPos + 后面增大或减小的 nextDiff - 1 (末尾的结束标识可以不用移动)

			System.arraycopy(zipList, insertPos - nextDiff, zipList, insertPos + reqLen, curLen - insertPos - 1 + nextDiff);

			// 处理插入的位置的下一个 entry 的 previous_entry_length
			// 出现了强制扩大的情况, 也就是强制用 5 个字节进行存储, 那么默认第一个字节位 0xfe;
			setZlEntryPreviousEntryLength(zipList, insertPos + reqLen, reqLen, forceLarge == 1);

			// 更新 zlTail
			setVal2ZlTail(zipList, getZlTailValFromZl(zipList) + reqLen);

			ZlEntry entry = unZip2ZlEntry(zipList, insertPos + reqLen);
			// 插入的位置的下一个 entry 不是尾结点
			if (zipList[entry.getHeaderSize() + entry.getLen()] != ZIP_END) {
				setVal2ZlTail(zipList, getZlTailValFromZl(zipList) + nextDiff);
			}

		} else {
			// 末尾插入

			// 更新 zlTail
			setVal2ZlTail(zipList, getZlTailValFromZl(zipList) + reqLen);
		}

		if (nextDiff != 0) {
			// TODO 级联更新 连锁更新
			// 级联更新 insertPos + reqLen 后面每一个 entry 的 previous_entry_length 对应的前面的 entry 的字节长度一致
			zipListCascadeUpdate(insertPos + reqLen);

			// 得到 insertPos + reqLen 需要的 entry 字节长度
			// 然后从 insertPos + reqLen 的下一个 entry 开始遍历
			// 得到下一个 entry 的 previous_entry_length 等于 上一个 entry 的字节长度, 跳出循环, 或者遍历到列表的尾部
			// 循环其间, 将下一个 entry 的 previous_entry_length 更新为上一个 entry 的字节长度, 不同的情况下

			// 需要结束后, 可能出现压缩列表申请了更多的空间, 也可能出现多了空间, 这时候会触发列表重新分配
		}

		// 更新当前的 entry 的 previous_entry_length
		setZlEntryPreviousEntryLength(zipList, insertPos, preLen, false);

		int encodingNeedByteNum = 0;
		// 更新当前的 entry 的 encoding
		if (Objects.nonNull(value)) {
			encodingNeedByteNum = setZlEntryEncodingByNum(zipList, insertPos + prevLenSize, value);
		} else {
			encodingNeedByteNum = setZlEntryEncodingByStr(zipList, insertPos + prevLenSize, content);
		}

		// 更新当前的 entry 的 content
		int updatePos = insertPos + preEntryPos + encodingNeedByteNum;
		// 更新当前 entry 的 content
		setZlEntryContent(zipList, updatePos, content);

		// 更新 zlLen 长度 + 1
		setVal2ZlLen(zipList, (byte) (getZlLenValFromZl(zipList) + 1));
		return true;
	}

	/**
	 * 压缩列表, 级联更新
	 *
	 * @param updatePos 更新的位置
	 */
	private void zipListCascadeUpdate(int updatePos) {
		// TODO
	}


	/**
	 * 重新分配数组
	 *
	 * @return
	 */
	private byte[] zipListResize(int len) {

		byte[] newZipList = new byte[len];
		// 将当前 zipList 的数据全部拷贝到 newZipList 中
		System.arraycopy(zipList, 0, newZipList, 0, zipList.length);
		// 重新更新 zlBytes 的值
		setVal2ZlBytes(newZipList, len);
		newZipList[len - 1] = ZIP_END;
		return newZipList;
	}

}
