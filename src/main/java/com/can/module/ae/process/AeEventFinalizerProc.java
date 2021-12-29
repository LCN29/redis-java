package com.can.module.ae.process;

import com.can.module.ae.AeEventLoop;

/**
 * <pre>
 *
 * </pre>
 *
 * @author
 * @date 2021-12-29  18:08
 */
public interface AeEventFinalizerProc {

	void aeEventFinalizerProc(AeEventLoop eventLoop, Object clientData);
}
