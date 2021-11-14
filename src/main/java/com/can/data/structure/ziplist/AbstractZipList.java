package com.can.data.structure.ziplist;

import com.can.util.Util;
import lombok.Data;

import java.util.Objects;

/**
 * <pre>
 * ZipList 工具类, 减少 ZipList 的代码量，保持整洁
 * </pre>
 *
 * @author
 * @date 2021-11-12  18:23
 */
public class AbstractZipList {

    /**
     * zlbytes 的存储长度
     */
    protected final static int ZL_BYTES_LEN = 4;

    /**
     * zltail 的存储长度 (entry 都是 byte 的倍数), 所以 zlTail 里面存储的数组的下标
     */
    protected final static int ZL_TAIL_LEN = 4;

    /**
     * zl len 的存储长度
     */
    protected final static int ZL_LEN_LEN = 2;

    /**
     * 压缩列表头部的大小
     * zlbytes 4 个字节
     * zltail 4 个字节
     * zllen 2 个字节
     */
    protected final static int ZIPLIST_HEADER_SIZE = ZL_BYTES_LEN + ZL_TAIL_LEN + ZL_LEN_LEN;

    /**
     * 整个压缩列表结束的标识, 255
     */
    protected final static byte ZIP_END = (byte) 0xFF;

    /**
     * entry 中 previous_entry_length 所占的字节长度的区分值,
     * 当 entry 的第一个字节的长度存储的值
     * 小于 254, 1 个字节
     * 等于 254, 5 个字节, 第一个字节固定为 第一个固定为 0xFE (1111 1110, 既 254)
     * 大于 254, 异常情况
     */
    protected final static byte ZIP_BIG_PREV_LEN = (byte) 0xFE;

    /**
     * 当 previous_entry_length 用 5 个字节存储时, 第一个字节的默认值
     */
    protected final static byte ZIP_BIG_PREVLEN = (byte) 0xFE;

    /**
     * entry encoding 存储的内容的解析
     */

    /**
     * 192, 二进制 11000000
     */
    protected final static byte ZIP_STR_MASK = (byte) 0xc0;
    /**
     * 48, 二进制 00110000
     */
    protected final static byte ZIP_INT_MASK = (byte) 0x30;
    /**
     * 0, 二进制 00000000
     */
    protected final static byte ZIP_STR_06B = (byte) (0 << 6);
    /**
     * 64, 二进制 01000000
     */
    protected final static byte ZIP_STR_14B = (byte) (1 << 6);
    /**
     * 128, 二进制 10000000
     */
    protected final static byte ZIP_STR_32B = (byte) (2 << 6);
    /**
     * 192 | 0, 也就是 11000000 | 0, 结果的二进制 11000000
     */
    protected final static byte ZIP_INT_16B = (byte) (0xc0 | 0 << 4);
    /**
     * 192 | 16, 也就是 11000000 | 00010000, 结果的二进制 11010000
     */
    protected final static byte ZIP_INT_32B = (byte) (0xc0 | 1 << 4);
    /**
     * 192 | 32, 也就是 11000000 | 00100000, 结果的二进制 11100000
     */
    protected final static byte ZIP_INT_64B = (byte) (0xc0 | 2 << 4);
    /**
     * 192 | 48, 也就是 11000000 | 00110000, 结果的二进制 11110000
     */
    protected final static byte ZIP_INT_24B = (byte) (0xc0 | 3 << 4);
    /**
     * 254, 二进制 11111110
     */
    protected final static byte ZIP_INT_8B = (byte) 0xfe;
    /**
     * 241, 二进制 11110001, 表示 encoding 为存储数值 0 ~ 12 的整数 的类型
     */
    protected final static byte ZIP_INT_IMM_MIN = (byte) 0xf1;
    /**
     * 253, 二进制 11111101
     */
    protected final static byte ZIP_INT_IMM_MAX = (byte) 0xfd;

    /**
     * 24 位二进制的最小值
     */
    protected final static int INT24_MIN = (1 << 25 - 1) * -1;

    /**
     * 24 位二进制的最大值
     */
    protected final static int INT24_MAX = 1 << 24 - 1;

    /**
     * zipList 中 entry 解码出来后的对象
     */
    @Data
    static class ZlEntry {

        /**
         * previous_entry_length 占用了多少个字节, 取值为 1 或者 5
         */
        int prevRawLenSize;

        /**
         * previous_entry_length 的值
         */
        int prevRawLen;

        /**
         * encoding 占用了多少个字节  1,2,5
         */
        int lenSize;

        /**
         * content 占用的字节数
         */
        int len;

        /**
         * 头部的长度占用的长度 prevRawLenSize + lenSize
         */
        int headerSize;

        /**
         * content 的数据类型, 用一些枚举替代, 不保存具体的值
         * 0000 0000 长度最大为 63 的字节数组
         * 0100 0000 最大长度为 2^14 - 1 的字节数组
         * 1000 0000 最大长度为 2^32 - 1 的字节数组
         * 1100 0000 整数
         */
        byte encoding;

        /**
         * 当前 zlEntry 在字节数组中的开始位置
         * 当前 entry 在字节数组中的开始位置, 也就是指向这个 entry 的 previous_entry_length 属性的内存位置
         */
        private int p;
    }

    /**
     * 从压缩列表的指定的位置中解析出一个 zlEntry
     *
     * @param zipList       压缩列表
     * @param unZipStartPos 指定的位置
     * @return 解析出来的 entry
     */
    ZlEntry unZip2ZlEntry(byte[] zipList, int unZipStartPos) {

        ZlEntry entry = new ZlEntry();
        entry.setPrevRawLenSize(getPosEntryPrevRawLenSize(zipList, unZipStartPos));
        entry.setPrevRawLen(getPosEntryPrevLenValue(zipList, unZipStartPos, entry.getPrevRawLenSize()));

        byte theFirstByteEncoding = zipList[unZipStartPos + entry.getPrevRawLenSize()];

        if (isZip2StrByEncodingValue(theFirstByteEncoding)) {
            // 字符串
            // 与上 1100 0000
            entry.setEncoding((byte) (theFirstByteEncoding & ZIP_INT_16B));

            switch (entry.getEncoding()) {
                case ZIP_STR_06B:
                    entry.setLenSize(1);
                    entry.setLen(theFirstByteEncoding);
                    break;
                case ZIP_STR_14B:
                    entry.setLenSize(2);
                    // 01 bbbbbb xxxxxxxx, 取第一位的后 6 个字节
                    int len = zipList[unZipStartPos + entry.getPrevRawLenSize()] & ((1 << 6) - 1);
                    len = (len << 8) & zipList[unZipStartPos + entry.getPrevRawLenSize() + 1];
                    entry.setLen(len);
                    break;
                case ZIP_INT_32B:
                    entry.setLenSize(5);
                    // 10__ aaaaaaaa bbbbbbbb cccccccc dddddddd
                    int newLen = zipList[unZipStartPos + entry.getPrevRawLenSize() + 1];
                    newLen = (newLen << 8) & zipList[unZipStartPos + entry.getPrevRawLenSize() + 2];
                    newLen = (newLen << 8) & zipList[unZipStartPos + entry.getPrevRawLenSize() + 3];
                    newLen = (newLen << 8) & zipList[unZipStartPos + entry.getPrevRawLenSize() + 4];
                    entry.setLen(newLen);
                    break;
                default:
                    break;
            }

        } else {
            // 整数, 直接设置
            entry.setEncoding(theFirstByteEncoding);
            entry.setLenSize(1);


            if (entry.getEncoding() == ZIP_INT_16B) {
                entry.setLen(2);
            } else if (entry.getEncoding() == ZIP_INT_32B) {
                entry.setLen(4);
            } else if (entry.getEncoding() == ZIP_INT_64B) {
                entry.setLen(8);
            } else if (entry.getEncoding() >= ZIP_INT_IMM_MIN && entry.getEncoding() <= ZIP_INT_IMM_MAX) {
                entry.setLen(2);
            } else {
                entry.setLen(2);
            }
        }

        entry.setHeaderSize(entry.getPrevRawLenSize() + entry.getLenSize());
        entry.setP(unZipStartPos);
        return entry;
    }


    /**
     * 将一个 zlBytes 的值设置到 zipList 的 0 - 3 个字节
     *
     * @param zipList    设置的压缩列表
     * @param zlBytesVal zlByes 的值
     */
    void setVal2ZlBytes(byte[] zipList, int zlBytesVal) {

        // 用来表示压缩列表的字节数组的前 4 位, 0, 1, 2, 3 表示的是当前压缩列表的所占字节数
        // 入参的 zlBytesVal 是 int 32 位, 每 8 位依次放到字节数组的 0，1，2，3 的位置
        // zlBytesVal 向右移动 24 位, 再强转为 byte, 就能获取 int 的前 8 位
        zipList[0] = (byte) (zlBytesVal >> 24);
        // 同理 zlBytesVale 向右移动 18 位, 再强转为 byte, 就能获取 int 的 9-16位
        zipList[1] = (byte) (zlBytesVal >> 16);
        zipList[2] = (byte) (zlBytesVal >> 8);
        zipList[3] = (byte) zlBytesVal;
    }

    /**
     * 从压缩列表中获取 zlBytes 的值, 也就是 0 - 3 个字节组成的 int 值
     *
     * @param zipList 压缩列表
     * @return zlBytes 的值
     */
    int getZlBytesValFromZl(byte[] zipList) {

        int zlBytes = 0;
        // setVal2ZlBytes 的反向操作
        zlBytes = zlBytes | zipList[0];
        zlBytes = (zlBytes << 8) | zipList[1];
        zlBytes = (zlBytes << 8) | zipList[2];
        zlBytes = (zlBytes << 8) | zipList[3];
        return zlBytes;
    }

    /**
     * 将一个 zlTail 的值设置到 zipList 的 4 - 7 个字节
     *
     * @param zipList   设置的压缩列表
     * @param zlTailVal zLTail 的值
     */
    void setVal2ZlTail(byte[] zipList, int zlTailVal) {

        // 用来表示压缩列表的字节数组的前 5-8 位, 4, 5, 6, 7 表示的是当前压缩列表的头元素到最后一个元素的起始位置的字节数
        zipList[4] = (byte) (zlTailVal >> 24);
        zipList[5] = (byte) (zlTailVal >> 16);
        zipList[6] = (byte) (zlTailVal >> 8);
        zipList[7] = (byte) zlTailVal;
    }

    /**
     * 从压缩列表中获取 zlTail 的值, 也就是 4 - 7 个字节组成的 int 值
     *
     * @param zipList 压缩列表
     * @return zlTail 的值
     */
    int getZlTailValFromZl(byte[] zipList) {

        int zlTail = 0;
        // setVal2ZlTail 的反向操作
        zlTail = zlTail | zipList[4];
        zlTail = (zlTail << 8) | zipList[5];
        zlTail = (zlTail << 8) | zipList[6];
        zlTail = (zlTail << 8) | zipList[7];
        return zlTail;
    }

    /**
     * 将一个 zlLen 的值设置到 zipList 的 8 - 9 个字节
     *
     * @param zipList  设置的压缩列表
     * @param zlLenVal zlLen 的值
     */
    void setVal2ZlLen(byte[] zipList, short zlLenVal) {
        // 用来表示压缩列表的字节数组的前 9-10 位, 8, 9 表示的是当前压缩列表的存储的 entry 个数
        zipList[8] = (byte) (zlLenVal >> 8);
        zipList[9] = (byte) zlLenVal;
    }

    /**
     * 从压缩列表中获取 zlLen 的值, 也就是 8 - 9 个字节组成的 int 值
     *
     * @param zipList 压缩列表
     * @return zlLen 的值
     */
    int getZlLenValFromZl(byte[] zipList) {

        int zlLen = 0;
        // setVal2ZlTail 的反向操作
        zlLen = zlLen | zipList[8];
        zlLen = (zlLen << 8) | zipList[9];
        return zlLen;
    }

    /**
     * 获取压缩列表中, 第一个 entry 的起始位置
     *
     * @return 第一个 entry 的起始位置
     */
    int getZipListEntryHeadPos() {
        // 第一个 entry 的起始位置, 基本就是 zipList_header_size 后面的位置, 也是 ZIPLIST_HEADER_SIZE
        return ZIPLIST_HEADER_SIZE;
    }

    /**
     * 获取压缩列表中, 最后一个 entry 的结束位置
     *
     * @param zipList 压缩列表
     * @return 最后一个 entry 的结束位置
     */
    int getZipListEntryTailPos(byte[] zipList) {
        // zlBytes 存储的整个字节数组的长度, 整个字节数组的长度减去尾部的结束标识, 那么得到的位置就是 entry 的结束位置
        return getZlBytesValFromZl(zipList) - 1;
    }

    /**
     * 获取压缩列表指定位置的 entry 的 previous_entry_length 的占用的字节数
     *
     * @param zipList 压缩列表
     * @param pos     指定的位置
     * @return 指定位置的 entry 的 previous_entry_length 的占用的字节数
     */
    int getPosEntryPrevRawLenSize(byte[] zipList, int pos) {
        // entry : <previous_entry_length> <encoding> <content>
        // previous_entry_length 占 1 个或者 5 个字节
        // 当前一个元素的长度小于 254 字节时，用 1 个字节表示
        // 当前一个元素的长度大于或等于 254 字节时，用 5 个字节来表示, 这 5 个字节中, 第一个固定为 0xFE (1111 1110, 既 254), 后 4 位是具体的值
        // 不用 255 表示的话，是 zLend 已经使用了, 在处理中遇到 255 表示达到了压缩列表的尾部了
        return zipList[pos] < ZIP_BIG_PREV_LEN ? 1 : 5;
    }

    /**
     * 获取压缩列表指定位置的 entry 的 previous_entry_length 的值
     *
     * @param zipList 压缩列表
     * @param pos     指定的位置
     * @return 指定位置的 entry 的 previous_entry_length 的值
     */
    int getPosEntryPrevLenValue(byte[] zipList, int pos, int previousEntryLengthSize) {

        // 1 个字节, 直接获取返回即可
        if (previousEntryLengthSize == 1) {
            return zipList[pos];
        }

        // 5 个字节, 需要获取后面的 4 位
        int preLen = 0;
        preLen = preLen | zipList[pos + 1];
        preLen = (preLen << 8) | zipList[pos + 2];
        preLen = (preLen << 8) | zipList[pos + 3];
        preLen = (preLen << 8) | zipList[pos + 4];
        return preLen;

    }

    /**
     * 获取压缩列表最后一个 entry 的开始位置
     *
     * @param zipList 压缩列表
     * @return 最后一个 entry 的开始位置
     */
    int getLastEntryStartPos(byte[] zipList) {
        // 获取获取最后一个 entry 的起始位置, 只需要获取到 zlTail 的值就可以了
        // zlTail 表示的就是压缩列表到最后一个 entry 的起始位置的字节差
        return getZlTailValFromZl(zipList);
    }

    /**
     * 根据当前的 value 值获取对应的 encoding 值
     *
     * @param curVal 当前的 long 值
     * @return 计算后的 encoding 值
     */
    byte getEncodingFromCurValue(long curVal) {

        byte curEncoding;
        if (curVal >= 0 && curVal <= 12) {
            // 把获取到的值添加到 encoding 中
            curEncoding = (byte) (ZIP_INT_IMM_MIN + (byte) curVal);
        } else if (curVal >= Byte.MIN_VALUE && curVal <= Byte.MAX_VALUE) {
            curEncoding = ZIP_INT_8B;
        } else if (curVal >= Short.MIN_VALUE && curVal <= Short.MAX_VALUE) {
            curEncoding = ZIP_INT_16B;
        } else if (curVal >= INT24_MIN && curVal <= INT24_MAX) {
            curEncoding = ZIP_INT_24B;
        } else if (curVal >= Integer.MIN_VALUE && curVal <= Integer.MAX_VALUE) {
            curEncoding = ZIP_INT_32B;
        } else {
            curEncoding = ZIP_INT_64B;
        }

        return curEncoding;
    }

    /**
     * 通过整数类型的 encoding 获取给 content 分配多长的字节数
     *
     * @param encoding 编码
     * @return content 需要的字节数
     */
    int getByteSizeFromNumberEncoding(byte encoding) {
        switch (encoding) {
            case ZIP_INT_8B:
                return 1;
            case ZIP_INT_16B:
                return 2;
            case ZIP_INT_24B:
                return 3;
            case ZIP_INT_32B:
                return 4;
            case ZIP_INT_64B:
                return 8;
            default:
                break;
        }
        // 0 - 12 的整数类型, 不需要 content, 也就是 0， 其他情况也默认返回 0
        return 0;
    }

    /**
     * 存储 encoding 字段需要多少个字节
     *
     * @param encoding encoding 的值
     * @param content  需要存储到 entry 的内容
     * @return 存储 encoding 需要的字节数
     */
    int getStoreEncodingByteNumber(byte encoding, byte[] content) {

        if (isZip2StrByEncodingValue(encoding)) {
            // 编码为字符串, encoding 的取值可以为 1，2，4, 需要根据存储的内容的长度判断
            int rawLen = content.length;
            // 63, 存储的字符串的长度小于等于 63, encoding 用 1 个字节存储即可
            if (rawLen <= 0x3f) {
                return 1;
                // 16383, 存储的字符串的长度小于小于 16383 (2 的 14 方 - 1), encoding 用 2 个字节存储
            } else if (rawLen <= 0x3fff) {
                return 2;
            }
            // 其他情况用 5 个字节存储
            return 5;
        }
        // 编码为整数的话，encoding 固定为 1 个字节
        return 1;
    }

    /**
     * 通过 encoding 判断当前的内存是压缩为字符串格式
     *
     * @param encoding encoding 值
     * @return true： 压缩为字符串， false: 压缩为整数
     */
    boolean isZip2StrByEncodingValue(byte encoding) {
        // 11000000 & encoding < 11000000 那么 encoding 的前二位只能 01, 10, 00, 都是 encoding 表示字符串的格式
        return (encoding & ZIP_STR_MASK) < ZIP_STR_MASK;
    }

    /**
     * 通过当前 entry 的字节长度得到当前的 entry 的字节长度需要多少个字节存储
     *
     * @param curEntryRequestLen 当前 entry 的字节长度
     * @return 需要多少个字节存储
     */
    int getCurEntryPreviousEntryLengthNeedByteNum(int curEntryRequestLen) {

        // 长度在 254 内, 只需要 1 个字节, 否则就是 5 个字节
        return curEntryRequestLen <= 0xFE - 1 ? 1 : 5;
    }

    /**
     * 更新对应 entry 的 previous_entry_length
     *
     * @param zipList             压缩列表
     * @param updatePos           更新的位置
     * @param previousEntryLength previous_entry_length 的值
     * @param forceLarge          是否强制有 5 个字节存储
     */
    void setZlEntryPreviousEntryLength(byte[] zipList, int updatePos, long previousEntryLength, boolean forceLarge) {

        // 小于 254, 同时不需要强制用 5 个字节设置
        if (previousEntryLength < ZIP_BIG_PREV_LEN && !forceLarge) {
            // 更新第一个字节位存入的 值即可
            zipList[updatePos] = (byte) previousEntryLength;
            return;
        }
        // 需要强制用 5 个字节或者长度大于 254 了
        // 第一个字节默认为 0xfe
        zipList[updatePos] = ZIP_BIG_PREVLEN;
        zipList[updatePos + 1] = (byte) previousEntryLength;
        zipList[updatePos + 2] = (byte) (previousEntryLength >> 8);
        zipList[updatePos + 3] = (byte) (previousEntryLength >> 16);
        zipList[updatePos + 4] = (byte) (previousEntryLength >> 24);
    }

    /**
     * 将整数类型的 encoding 更新到当前 entry 的 encoding 中
     *
     * @param zipList   压缩列表
     * @param updatePos 更新的位置
     * @param value     更新的值
     * @return encoding 需要的字节数
     */
    int setZlEntryEncodingByNum(byte[] zipList, int updatePos, long value) {
        // 对应的 encoding 值
        byte encoding = getEncodingFromCurValue(value);
        zipList[updatePos] = encoding;
        return 1;
    }

    /**
     * 将字符串类型的 encoding 更新到当前 entry 的 encoding 中
     *
     * @param zipList   压缩列表
     * @param updatePos 更新的位置
     * @param content   更新的内容
     * @return encoding 需要的字节数
     */
    int setZlEntryEncodingByStr(byte[] zipList, int updatePos, byte[] content) {

        int len = content.length;
        int needByteCount = 0;
        if (len <= 63) {
            zipList[updatePos] = (byte) len;
            needByteCount = 1;
        } else if (len <= Math.pow(2, 14) - 1) {

            zipList[updatePos] = (byte) (((byte) (len >> 8)) | ZIP_STR_14B);
            zipList[updatePos + 1] = (byte) len;
            needByteCount = 2;
        } else {
            zipList[updatePos] = ZIP_STR_32B;
            zipList[updatePos + 1] = (byte) (len >> 24);
            zipList[updatePos + 2] = (byte) (len >> 16);
            zipList[updatePos + 3] = (byte) (len >> 8);
            zipList[updatePos + 4] = (byte) len;
            needByteCount = 5;
        }

        return needByteCount;
    }

    /**
     * 将 content 填充到 entry 的 content 中
     *
     * @param zipList   压缩列表
     * @param updatePos 更新的位置
     * @param content   更新的内容
     */
    void setZlEntryContent(byte[] zipList, int updatePos, byte[] content) {

        Long value = Util.string2Long(content);

        // 不为空
        if (Objects.nonNull(value)) {
            // 数字
            // 需要的字节数
            int reqLen = getByteSizeFromNumberEncoding(getEncodingFromCurValue(value));
            if (reqLen == 0) {
                return;
            }
            long changeValue = value;

            // 1 个字节, 8 位
            if (reqLen == 1) {
                zipList[updatePos] = (byte) changeValue;
                return;
            }

            // 2 个字节, 16 位
            if (reqLen == 2) {
                zipList[updatePos] = (byte) (changeValue >> 8);
                zipList[updatePos + 1] = (byte) changeValue;
                return;
            }
            // 3 个字节， 24 位
            if (reqLen == 3) {
                zipList[updatePos] = (byte) (changeValue >> 16);
                zipList[updatePos + 1] = (byte) (changeValue >> 8);
                zipList[updatePos + 2] = (byte) changeValue;
                return;
            }
            // 4 个字节， 32 位
            if (reqLen == 4) {
                zipList[updatePos] = (byte) (changeValue >> 24);
                zipList[updatePos + 1] = (byte) (changeValue >> 16);
                zipList[updatePos + 2] = (byte) (changeValue >> 8);
                zipList[updatePos + 3] = (byte) changeValue;
                return;
            }
            // 8 个字节， 64 位

            zipList[updatePos] = (byte) (changeValue >> 56);
            zipList[updatePos + 1] = (byte) (changeValue >> 48);
            zipList[updatePos + 2] = (byte) (changeValue >> 40);
            zipList[updatePos + 3] = (byte) (changeValue >> 32);
            zipList[updatePos + 4] = (byte) (changeValue >> 24);
            zipList[updatePos + 5] = (byte) (changeValue >> 16);
            zipList[updatePos + 6] = (byte) (changeValue >> 8);
            zipList[updatePos + 7] = (byte) changeValue;
            return;
        }

        int arrLen = content.length;

        // 字符串, 字符串长度是以字节位为单位存储的, 也就是 8 的倍数, 不够 8 的倍数, 前面补 0
        int needZeroCount = arrLen % 8;

        for (int i = 0; i < needZeroCount; i++) {
            zipList[updatePos + i] = 0;
        }

        int index = 0;
        for (int i = arrLen - 1; i >= 0; i--) {
            zipList[updatePos + needZeroCount + index] = content[i];
        }

    }


}
