package com.can.data.structure.skiplist;

import com.can.data.structure.sds.Sds;


/**
 * <pre>
 * 跳表
 * </pre>
 *
 * @author
 * @date 2021-11-08  17:41
 */
public class SkipList {

	/**
	 * 跳表的头尾节点
	 */
	private SkipListNode header, tail;

	/**
	 * 跳表中元素的个数
	 */
	private long length;

	/**
	 * 跳表的高度
	 */
	private int level;

	/**
	 * 跳表最大的层高
	 */
	private final static int ZSKIPLIST_MAXLEVEL = 32;

	/**
	 * 用于产生层高的随机因子
	 */
	private final static float ZSKIPLIST_P = 0.25f;

	/**
	 * 后 16 位全部为 1
	 */
	private final static int RANDOM_NUM = 0xFFFF;


	/**
	 * 跳表的层
	 */
	static class SkipListLevel {

		/**
		 * 后置节点
		 */
		private SkipListNode forward;

		/**
		 * 下一个节点和当前节点跳过的节点数
		 */
		private int span;
	}

	/**
	 * 跳表的节点
	 */
	static class SkipListNode {

		/**
		 * 存储的内容
		 */
		private Sds ele;

		/**
		 * 节点的分数
		 */
		private double score;

		/**
		 * 前置节点
		 */
		private SkipListNode backward;

		/**
		 * 节点的层数
		 */
		private SkipListLevel[] levels;
	}

	/**
	 * 返回层数
	 * 规则: 越高的概率越低
	 * 设 ZSKIPLIST_P = p
	 * 第 n 层的概率为 = p ^ (n-1) * (1 - p)
	 *
	 * @return 层数
	 */
	private int zslRandomLevel() {
		int level = 1;

		/** Redis 内部的实现
		 while ((random() & RANDOM_NUM) < RANDOM_NUM * ZSKIPLIST_P)) { level += 1;}
		 */
		while ((Math.random() < ZSKIPLIST_P)) {
			level += 1;
		}
		return Math.min(level, ZSKIPLIST_MAXLEVEL);
	}

}
