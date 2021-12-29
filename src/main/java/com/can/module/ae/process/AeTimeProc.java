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

	int aeTimeProc(AeEventLoop eventLoop, long id, Object clientData);
}
