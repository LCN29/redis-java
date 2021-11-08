package com.can.data.structure.skiplist;

import com.can.data.structure.sds.Sds;

import java.util.Objects;


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
	private int length;

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


	public SkipList() {

		this.level = 1;
		this.length = 0;
		this.header = zslCreateNode(ZSKIPLIST_MAXLEVEL, 0, null);

		for (int j = 0; j < ZSKIPLIST_MAXLEVEL; j++) {
			this.header.levels[j].forward = null;
			this.header.levels[j].span = 0;
		}

		this.header.backward = null;
		this.tail = null;
	}


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
	 * 新增元素
	 * @param ele
	 * @param score
	 * @return
	 */
	public SkipListNode insert(double score, Sds ele) {

		// serverAssert(!isnan(score)); 数据校验?

		SkipListNode[] update = new SkipListNode[ZSKIPLIST_MAXLEVEL];
		int[] rank = new int[ZSKIPLIST_MAXLEVEL];
		SkipListNode x = header;

		for (int i = level - 1; i >= 0; i-- ) {

			rank[i] = i == level - 1 ? 0 : rank[i + 1];

			while (x.levels[i].forward != null
					&& (x.levels[i].forward.score < score || (x.levels[i].forward.score == score && x.levels[i].forward.ele.equals(ele)))) {

				rank[i] += x.levels[i].span;
				x = x.levels[i].forward;
			}
			update[i] = x;
		}

		// 随机分配层高
		int level = zslRandomLevel();

		// 当前的层高高于已有的层高
		if (level > this.level) {
			for (int i = this.level; i < level; i++) {
				rank[i] = 0;
				update[i] = this.header;
				update[i].levels[i].span = this.length;
			}
			this.level = level;
		}

		x = zslCreateNode(level, score, ele);

		for (int i = 0; i < level; i++) {
			x.levels[i].forward = update[i].levels[i].forward;
			update[i].levels[i].forward = x;
			x.levels[i].span = update[i].levels[i].span - (rank[0] - rank[i]);
			update[i].levels[i].span = (rank[0] - rank[i]) + 1;
		}

		for (int i = level; i < this.level; i++) {
			update[i].levels[i].span++;
		}

		x.backward = (update[0] == this.header) ? null : update[0];
		if (Objects.nonNull(x.levels[0].forward)) {
			x.levels[0].forward.backward = x;
		} else {
			this.tail = x;
			this.length ++;
		}
		return x;
	}

	/**
	 * 创建节点
	 * @param level 层高
	 * @param score 分数
	 * @param ele 内容
	 * @return 创建的节点
	 */
	private SkipListNode zslCreateNode(int level, double score, Sds ele) {

		SkipListNode node = new SkipListNode();
		node.score = score;
		node.ele = ele;
		node.levels = new SkipListLevel[level];
		return node;
	}


	/**
	 * 返回层数
	 * 规则: 越高的概率越低
	 * 设 ZSKIPLIST_P = p
	 * 第 n 层的概率为 = p ^ (n - 1) * (1 - p)
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
