package com.can.module.ae.process;

import com.can.module.ae.AeEventLoop;

/**
 * <pre>
 *
 * </pre>
 *
 * @author
 * @date 2021-12-29  18:02
 */
public interface AeTimeProc {

	/**
	 * 事件时间执行函数
	 *
	 * @param eventLoop  事件轮询
	 * @param id         时间事件 Id
	 * @param clientData 客户数据
	 * @return 返回 -1, 表示这个时间事件不需要了, 可以从事件轮询中删除, 返回 > 0, 表示这个事件下次隔多少毫秒再执行一次
	 */
	int aeTimeProc(AeEventLoop eventLoop, long id, Object clientData);
}
