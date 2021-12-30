package com.can.module.ae.api;

import com.can.module.ae.AeConstants;
import com.can.module.ae.AeEventLoop;
import com.can.module.ae.api.state.AeApiState;
import com.can.module.ae.event.AeFileEvent;
import com.can.module.time.TimeVal;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Objects;
import java.util.Set;

/**
 * <pre>
 *
 * </pre>
 *
 * @author
 * @date 2021-12-30  15:57
 */
public class EpollAeApi implements AeApi {

	private final static int FD_SETSIZE = 1024;

	@Override
	public int aeApiCreate(AeEventLoop aeEventLoop) throws IOException {

		AeApiState state = new AeApiState();

		// 开启 selector
		Selector selector = Selector.open();
		state.setSelector(selector);

		aeEventLoop.setApiData(state);
		return 0;
	}

	@Override
	public int aeApiResize(AeEventLoop aeEventLoop, int setSize) {

		if (setSize > FD_SETSIZE) {
			return -1;
		}
		return 0;
	}

	@Override
	public void aeApiFree(AeEventLoop aeEventLoop) {
		aeEventLoop.setApiData(null);
	}

	@Override
	public int aeApiAddEvent(AeEventLoop aeEventLoop, int fd, int mask) {

		AeApiState apiData = aeEventLoop.getApiData();

		AeFileEvent event = aeEventLoop.getEvents()[fd];

		// TODO
		int op = event.getMask() == AeConstants.AE_NONE ? -1 : 0;

		mask |= aeEventLoop.getEvents()[fd].getMask();

		if ((mask & AeConstants.AE_READABLE) != 0) {
			// ee.events |= EPOLLIN;
		}

		if ((mask & AeConstants.AE_WRITABLE) != 0) {
			// ee.events |= EPOLLOUT;
		}
		return 0;
	}

	@Override
	public int aeApiDelEvent(AeEventLoop aeEventLoop, int fd, int delmask) {

		AeApiState apiData = aeEventLoop.getApiData();

		int mask = aeEventLoop.getEvents()[fd].getMask() & (~delmask);

		if (mask != AeConstants.AE_NONE) {

		} else {

		}
		return 0;
	}

	@Override
	public int aeApiPoll(AeEventLoop aeEventLoop, TimeVal tv) throws IOException {

		AeApiState apiData = aeEventLoop.getApiData();

		int retVal = 0;
		if (Objects.nonNull(tv)) {
			long waitTimeMs = tv.getTvSec() * 1000 + tv.getTvUsec() / 1000;
			retVal = apiData.getSelector().select(waitTimeMs);
		} else {
			retVal = apiData.getSelector().select();
		}

		// 有准备就绪的事件
		if (retVal > 0) {

			// 准备就绪的事件
			Set<SelectionKey> selectionKeys = aeEventLoop.getApiData().getSelector().selectedKeys();

			int index = 0;
			for (SelectionKey selectionKey : selectionKeys) {
				int mask = 0;
				index++;

				if (selectionKey.isAcceptable()) {
					// TODO
					mask |= AeConstants.AE_READABLE;
				}

				if (selectionKey.isReadable()) {
					mask |= AeConstants.AE_WRITABLE;
				}

				if (selectionKey.isWritable()) {
					mask |= AeConstants.AE_WRITABLE;
				}
				aeEventLoop.getFired()[index - 1].setMask(mask);
				// TODO
				// aeEventLoop.getFired()[index - 1].setFd(selectionKey.);
			}

			// 清除已经获取到的事件
			selectionKeys.clear();

		}
		return retVal;
	}

	@Override
	public String aeApiName() {
		return "epoll";
	}

}
