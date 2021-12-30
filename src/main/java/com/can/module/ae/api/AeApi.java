package com.can.module.ae.api;

import com.can.module.ae.AeEventLoop;
import com.can.module.time.TimeVal;

import java.io.IOException;

/**
 * <pre>
 *
 * </pre>
 *
 * @author
 * @date 2021-12-30  15:51
 */
public interface AeApi {

	/**
	 * aeApi 的创建
	 *
	 * @param aeEventLoop
	 * @return
	 */
	int aeApiCreate(AeEventLoop aeEventLoop) throws IOException;

	int aeApiResize(AeEventLoop aeEventLoop, int setSize);

	void aeApiFree(AeEventLoop aeEventLoop);

	int aeApiAddEvent(AeEventLoop aeEventLoop, int fd, int mask);

	int aeApiDelEvent(AeEventLoop aeEventLoop, int fd, int mask);

	int aeApiPoll(AeEventLoop aeEventLoop, TimeVal tv) throws IOException;

	String aeApiName();

	static AeApi getAeApiInstance(String type) {

		if ("select".equals(type)) {
			return new SelectAeApi();
		}

		if ("epoll".equals(type)) {
			return new EpollAeApi();
		}

		if ("kqueue".equals(type)) {
			return new KqueueAeApi();
		}

		if ("evport".equals(type)) {
			return new EvportAeApi();
		}

		// 默认
		return new SelectAeApi();
	}
}
