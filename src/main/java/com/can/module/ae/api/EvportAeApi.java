package com.can.module.ae.api;

import com.can.module.ae.AeEventLoop;
import com.can.module.time.TimeVal;

/**
 * <pre>
 *
 * </pre>
 *
 * @author
 * @date 2021-12-30  15:57
 */
public class EvportAeApi implements AeApi {

	@Override
	public int aeApiCreate(AeEventLoop aeEventLoop) {
		return 0;
	}

	@Override
	public int aeApiResize(AeEventLoop aeEventLoop, int setSize) {
		return 0;
	}

	@Override
	public void aeApiFree(AeEventLoop aeEventLoop) {

	}

	@Override
	public int aeApiAddEvent(AeEventLoop aeEventLoop, int fd, int mask) {
		return 0;
	}

	@Override
	public int aeApiDelEvent(AeEventLoop aeEventLoop, int fd, int mask) {
		return 0;
	}

	@Override
	public int aeApiPoll(AeEventLoop aeEventLoop, TimeVal tv) {
		return 0;
	}

	@Override
	public String aeApiName() {
		return null;
	}
}
