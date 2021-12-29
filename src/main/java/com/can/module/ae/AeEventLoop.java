package com.can.module.ae;

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

	private long timeEventNextId;

	private LocalDateTime lastTime;

	private AeFileEvent[] events;

	private AeFiredEvent[] fired;

	private AeTimeEvent timeEventHead;

	private int stop;

	private Object apiData;

	private AeBeforeSleepProc beforeSleep;

	private AeBeforeSleepProc afterSleep;
}
