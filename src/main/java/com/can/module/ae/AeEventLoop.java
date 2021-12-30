package com.can.module.ae;

import com.can.module.ae.api.state.AeApiState;
import com.can.module.ae.event.AeFileEvent;
import com.can.module.ae.event.AeFiredEvent;
import com.can.module.ae.event.AeTimeEvent;
import com.can.module.ae.process.AeBeforeSleepProc;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <pre>
 *
 * </pre>
 *
 * @author
 * @date 2021-12-29  17:55
 */

@Data
public class AeEventLoop {

	private int maxFd;

	private int setSize;

	// 时间事件的下一个 id
	private long timeEventNextId;

	private LocalDateTime lastTime;

	// 事件数组
	private AeFileEvent[] events;

	// 触发的事件数组, 在轮询中, 会检查上面 events 符合条件的事件, 拷贝对应的事件放到这里, 表示这里面的事件是可以触发
	private AeFiredEvent[] fired;

	private AeTimeEvent timeEventHead;

	private int stop;

	// 事件轮询底层实现 Api 对象
	private AeApiState apiData;

	private AeBeforeSleepProc beforeSleep;

	private AeBeforeSleepProc afterSleep;
}
