package com.can.module.ae.event;

import com.can.module.ae.process.AeEventFinalizerProc;
import com.can.module.ae.process.AeTimeProc;
import lombok.Data;

/**
 * <pre>
 * 时间时间链表
 * </pre>
 *
 * @author
 * @date 2021-12-29  18:06
 */
@Data
public class AeTimeEvent {

	// 事件 id, 不断递增的
	private long id;

	// 事件执行的时间 (秒数)
	private long whenSec;

	// 事件执行的时间 (毫秒数)
	private long whenMs;

	// 执行的函数
	private AeTimeProc timeProc;

	// 时间事件从链表中删除时执行的函数, 非必须
	private AeEventFinalizerProc finalizerProc;

	// 客户端数据
	private Object clientData;

	// 上一个时间事件
	private AeTimeEvent prev;

	// 下一个时间事件
	private AeTimeEvent next;
}
