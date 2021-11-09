package com.can.data.structure.skiplist;

import com.can.data.structure.sds.Sds;
import lombok.Data;

import java.util.Objects;


/**
 * <pre>
 * 跳表
 * </pre>
 *
 * @author lcn29
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

		SkipListNode node = zslCreateNode(ZSKIPLIST_MAXLEVEL, 0, null);
		node.setBackward(null);
		for (int j = 0; j < ZSKIPLIST_MAXLEVEL; j++) {
			node.setSpecifiedLevelForwardNode(j, null);
			node.setSpecifiedLevelSpan(j, 0);
		}

		this.level = 1;
		this.length = 0;
		this.header = node;
		this.tail = null;
	}

	/**
	 * 新增元素
	 *
	 * @param ele   新增元素的内容
	 * @param score 新增元素的分数
	 * @return 新增的节点
	 */
	public SkipListNode insert(double score, Sds ele) {

		// serverAssert(!isnan(score)); 数据校验?

		// 存储当前节点新增到跳表时, 每一层的前置节点
		SkipListNode[] update = new SkipListNode[ZSKIPLIST_MAXLEVEL];

		// 存储从头节点到 update[i] 经过的节点数
		int[] rank = new int[ZSKIPLIST_MAXLEVEL];


		SkipListNode tempNode = this.header;

		for (int i = this.level - 1; i >= 0; i--) {

			rank[i] = i == this.level - 1 ? 0 : rank[i + 1];

			while (Objects.nonNull(tempNode.getSpecifiedLevelForwardNode(i))
					&& (tempNode.getSpecifiedLevelForwardNodeScore(i) < score
					|| (tempNode.getSpecifiedLevelForwardNodeScore(i) == score && tempNode.getSpecifiedLevelForwardNodeEle(i).sdscmp(ele) <= 0))) {

				rank[i] += tempNode.getSpecifiedLevelSpan(i);
				tempNode = tempNode.getSpecifiedLevelForwardNode(i);
			}

			update[i] = tempNode;
		}


		// 随机分配层高
		int curNodeLevel = zslRandomLevel();

		// 当前的层高高于已有的层高
		if (curNodeLevel > this.level) {
			for (int i = this.level; i < curNodeLevel; i++) {
				rank[i] = 0;
				update[i] = this.header;
				update[i].setSpecifiedLevelSpan(i, this.length);
			}
			this.level = curNodeLevel;
		}

		// 创建新的节点
		tempNode = zslCreateNode(level, score, ele);

		for (int i = 0; i < curNodeLevel; i++) {

			tempNode.setSpecifiedLevelForwardNode(i, update[i].getSpecifiedLevelForwardNode(i));
			update[i].setSpecifiedLevelForwardNode(i, tempNode);
			tempNode.setSpecifiedLevelSpan(i, update[i].getSpecifiedLevelSpan(i) - (rank[0] - rank[i]));
			update[i].setSpecifiedLevelSpan(i, rank[0] - rank[i] + 1);
		}

		for (int i = curNodeLevel; i < this.level; i++) {
			update[i].setSpecifiedLevelSpan(i, update[i].getSpecifiedLevelSpan(i) + 1);
		}

		if (update[0] == this.header) {
			System.out.println("0000");
		}

		// TODO bug
		SkipListNode skipListNode = update[0] == this.header ? null : update[0];

		tempNode.setBackward(skipListNode);

		if (Objects.nonNull(tempNode.getSpecifiedLevelForwardNode(0))) {
			tempNode.getSpecifiedLevelForwardNode(0).setBackward(tempNode);
		} else {
			this.tail = tempNode.getBackward();
		}

		this.length++;
		return tempNode;
	}

	/**
	 * 创建节点
	 *
	 * @param level 层高
	 * @param score 分数
	 * @param ele   内容
	 * @return 创建的节点
	 */
	private SkipListNode zslCreateNode(int level, double score, Sds ele) {

		SkipListNode node = new SkipListNode();
		node.setScore(score);
		node.setEle(ele);
		node.setLevels(new SkipListLevel[level]);

		for (int i = 0; i < level; i++) {
			node.getLevels()[i] = new SkipListLevel();
		}
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

	/**
	 * 跳表的层
	 */
	@Data
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
	@Data
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

		/**
		 * 获取指定层数的后置节点
		 *
		 * @param level 指定的层数
		 * @return 指定的层数的后置节点
		 */
		public SkipListNode getSpecifiedLevelForwardNode(int level) {
			return levels[level].getForward();
		}

		/**
		 * 设置指定层数的后置节点
		 *
		 * @param level       指定的层数
		 * @param forwardNode 更新的节点
		 */
		public void setSpecifiedLevelForwardNode(int level, SkipListNode forwardNode) {
			levels[level].setForward(forwardNode);
		}

		/**
		 * 获取指定层数的后置节点的分数
		 *
		 * @param level 指定的层数
		 * @return 指定的层数的后置节点的分数
		 */
		public double getSpecifiedLevelForwardNodeScore(int level) {
			return getSpecifiedLevelForwardNode(level).getScore();
		}


		/**
		 * 获取指定层数的后置节点的分数
		 *
		 * @param level 指定的层数
		 * @return 指定的层数的后置节点的分数
		 */
		public Sds getSpecifiedLevelForwardNodeEle(int level) {
			return getSpecifiedLevelForwardNode(level).getEle();
		}


		/**
		 * 获取当前节点指定层数的跳过节点个数
		 *
		 * @param level 指定的层数
		 * @return 跳过节点个数
		 */
		public int getSpecifiedLevelSpan(int level) {
			return levels[level].getSpan();
		}

		/**
		 * 更新当前节点指定层数的跳过节点个数
		 *
		 * @param level 指定的层数
		 * @param span  新的跳过个数
		 */
		public void setSpecifiedLevelSpan(int level, int span) {
			levels[level].setSpan(span);
		}

	}

}
