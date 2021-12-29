package com.can.module.ae.event;

import com.can.module.ae.process.AeEventFinalizerProc;

/**
 * <pre>
 * 时间时间链表
 * </pre>
 *
 * @author
 * @date 2021-12-29  18:06
 */
public class AeTimeEvent {

	long id;

	long whenSec;

	long whenMs;

	AeTimeEvent aeTimeEvent;

	AeEventFinalizerProc aeEventFinalizerProc;

	Object clientData;

	AeTimeEvent prev;

	AeTimeEvent next;
}
