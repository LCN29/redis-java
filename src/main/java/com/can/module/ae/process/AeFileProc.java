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

	void aeFileProc(AeEventLoop eventLoop, int fd, int mask);
}
