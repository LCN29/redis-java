package com.can.module.ae.process;

import com.can.module.ae.AeEventLoop;

/**
 * <pre>
 *
 * </pre>
 *
 * @author
 * @date 2021-12-29  18:01
 */
@FunctionalInterface
public interface AeFileProc {

	/**
	 * 文件事件执行函数
	 *
	 * @param eventLoop  时间轮询
	 * @param fd         文件描述符
	 * @param clientData 客户数据
	 * @param mask       文件标识
	 */
	void aeFileProc(AeEventLoop eventLoop, int fd, Object clientData, int mask);
}
