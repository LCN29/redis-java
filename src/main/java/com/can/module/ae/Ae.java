package com.can.module.ae;

import com.can.ApplicationStarter;
import com.can.config.DefaultConfig;
import com.can.module.ae.api.AeApi;
import com.can.module.ae.event.AeFileEvent;
import com.can.module.ae.event.AeFiredEvent;
import com.can.module.ae.event.AeTimeEvent;
import com.can.module.ae.process.AeBeforeSleepProc;
import com.can.module.ae.process.AeEventFinalizerProc;
import com.can.module.ae.process.AeFileProc;
import com.can.module.ae.process.AeTimeProc;
import com.can.module.time.TimeVal;
import com.can.util.TimeUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <pre>
 *
 * </pre>
 *
 * @author lcn29
 * @date 2021-12-29 21:10
 */
public class Ae {

	// ae 底层 api 实现
	private final static AeApi AE_API = AeApi.getAeApiInstance(DefaultConfig.AE_API_TYPE);

	/**
	 * 创建 AeEventLoop
	 *
	 * @param setSize 初始大小
	 * @return
	 */
	public static AeEventLoop aeCreateEventLoop(int setSize) throws IOException {

		AeEventLoop aeEventLoop = new AeEventLoop();

		aeEventLoop.setSetSize(setSize);
		aeEventLoop.setEvents(new AeFileEvent[setSize]);
		aeEventLoop.setFired(new AeFiredEvent[setSize]);
		aeEventLoop.setLastTime(LocalDateTime.now());

		aeEventLoop.setTimeEventNextId(0);
		aeEventLoop.setStop(0);
		aeEventLoop.setMaxFd(-1);

		// TODO 待完成
		AE_API.aeApiCreate(aeEventLoop);

		for (int i = 0; i < setSize; i++) {
			aeEventLoop.getEvents()[i].setMask(AeConstants.AE_NONE);
		}
		return aeEventLoop;
	}

	/**
	 * 清除 AeEventLoop
	 *
	 * @param eventLoop 需要删除的事件轮询
	 */
	public static void aeDeleteEventLoop(AeEventLoop eventLoop) {

		// TODO 待完成
		AE_API.aeApiFree(eventLoop);

		eventLoop.setEvents(null);
		eventLoop.setFired(null);
		eventLoop.setTimeEventHead(null);
		ApplicationStarter.getRedisServer().setEl(null);
	}

	/**
	 * 新增文件事件
	 *
	 * @param eventLoop  事件轮询
	 * @param fd         文件描述符
	 * @param mask       文件标识
	 * @param proc       执行的函数
	 * @param clientData 客户数据
	 * @return
	 */
	public static int aeCreateFileEvent(AeEventLoop eventLoop, int fd, int mask, AeFileProc proc, Object clientData) {

		// 超过了最大的文件描述符
		if (fd >= eventLoop.getSetSize()) {
			return AeConstants.AE_ERR;
		}

		// 获取指定文件的事件
		AeFileEvent fe = eventLoop.getEvents()[fd];

		// TODO 待完成
		if (AE_API.aeApiAddEvent(eventLoop, fd, mask) == -1) {
			return AeConstants.AE_ERR;
		}

		fe.setMask(fe.getMask() | mask);

		if ((mask & AeConstants.AE_READABLE) != 0) {
			fe.setRFileProc(proc);
		}

		if ((mask & AeConstants.AE_WRITABLE) != 0) {
			fe.setWFileProc(proc);
		}

		fe.setClientData(clientData);

		if (fd > eventLoop.getMaxFd()) {
			eventLoop.setMaxFd(fd);
		}
		return AeConstants.AE_OK;
	}

	/**
	 * 删除文件事件
	 *
	 * @param eventLoop 事件轮询
	 * @param fd        文件描述符
	 * @param mask      文件标识
	 */
	public static void aeDeleteFileEvent(AeEventLoop eventLoop, int fd, int mask) {

		// 超过了最大的文件描述符
		if (fd >= eventLoop.getSetSize()) {
			return;
		}

		// 获取指定文件的事件
		AeFileEvent fe = eventLoop.getEvents()[fd];

		if (fe.getMask() == AeConstants.AE_NONE) {
			return;
		}

		// 当删除 AE_WRITABLE 时, 我们需要顺手删除 AE_BARRIER 的
		if ((mask & AeConstants.AE_WRITABLE) != 0) {
			mask |= AeConstants.AE_BARRIER;
		}

		// TODO 待完成
		AE_API.aeApiDelEvent(eventLoop, fd, mask);

		fe.setMask(fe.getMask() & (~mask));

		// 需要删除的事件刚好是当前事件轮询记录的最大的文件描述符, 同时文件的标识为 AE_NONE
		if (fd == eventLoop.getMaxFd() && fe.getMask() == AeConstants.AE_NONE) {

			for (int j = eventLoop.getMaxFd() - 1; j >= 0; j--) {
				if (eventLoop.getEvents()[j].getMask() != AeConstants.AE_NONE) {
					break;
				}
				eventLoop.setMaxFd(j);
			}
		}
	}

	/**
	 * 获取指定文件的标识
	 *
	 * @param eventLoop 事件轮询
	 * @param fd        文件描述符
	 * @return
	 */
	public static int aeCreateFileEvent(AeEventLoop eventLoop, int fd) {

		if (fd >= eventLoop.getSetSize()) {
			return 0;
		}

		AeFileEvent fe = eventLoop.getEvents()[fd];
		return fe.getMask();
	}

	/**
	 * 创建时间事件
	 *
	 * @param eventLoop     事件轮询
	 * @param milliseconds  多少毫秒后执行
	 * @param proc          执行的函数
	 * @param clientData    客户数据
	 * @param finalizerProc 结束时执行的函数
	 * @return 时间事件的 id
	 */
	public static long aeCreateTimeEvent(AeEventLoop eventLoop, long milliseconds, AeTimeProc proc, Object clientData,
										 AeEventFinalizerProc finalizerProc) {

		long id = eventLoop.getTimeEventNextId();
		eventLoop.setTimeEventNextId(id + 1);

		AeTimeEvent te = new AeTimeEvent();
		te.setId(id);
		aeAddMillisecondsToNow(milliseconds, te);
		te.setTimeProc(proc);
		te.setFinalizerProc(finalizerProc);
		te.setClientData(clientData);
		// 头插法
		te.setPrev(null);
		te.setNext(eventLoop.getTimeEventHead());
		if (Objects.nonNull(te.getNext())) {
			te.getNext().setPrev(te);
		}
		eventLoop.setTimeEventHead(te);
		return id;
	}

	/**
	 * 删除时间事件
	 *
	 * @param eventLoop 时间事件
	 * @param id        事件 id
	 * @return
	 */
	public static int aeDeleteTimeEvent(AeEventLoop eventLoop, long id) {

		AeTimeEvent te = eventLoop.getTimeEventHead();

		while (Objects.nonNull(te)) {
			if (te.getId() == id) {
				// 将 id 设置为删除标识, 在下次执行时, 进行删除
				te.setId(AeConstants.AE_DELETED_EVENT_ID);
				return AeConstants.AE_OK;
			}
			te = te.getNext();
		}

		return AeConstants.AE_ERR;
	}

	/**
	 * 设置事件轮询执行前的回调函数
	 *
	 * @param eventLoop
	 * @param beforesleep
	 */
	public static void aeSetBeforeSleepProc(AeEventLoop eventLoop, AeBeforeSleepProc beforesleep) {
		eventLoop.setBeforeSleep(beforesleep);
	}

	/**
	 * 设置事件轮询执行后的回调函数
	 *
	 * @param eventLoop
	 * @param aftersleep
	 */
	public static void aeSetAfterSleepProc(AeEventLoop eventLoop, AeBeforeSleepProc aftersleep) {
		eventLoop.setAfterSleep(aftersleep);
	}

	/**
	 * 启动 AeEventLoop
	 *
	 * @param eventLoop
	 */
	public static void aeMain(AeEventLoop eventLoop) throws IOException {

		eventLoop.setStop(0);

		// 进入死循环, 除非等于 1
		while (eventLoop.getStop() == 0) {
			if (Objects.nonNull(eventLoop.getBeforeSleep())) {
				eventLoop.getBeforeSleep().aeBeforeSleepProc(eventLoop);
			}
			// 执行事件处理
			aeProcessEvents(eventLoop, AeConstants.AE_ALL_EVENTS | AeConstants.AE_CALL_AFTER_SLEEP);
		}
	}

	/**
	 * 停止事件轮询
	 *
	 * @param eventLoop 事件轮询
	 */
	public static void aeStop(AeEventLoop eventLoop) {
		eventLoop.setStop(1);
	}

	/**
	 * 阻塞 milliseconds 毫秒, 直到给定的文件描述符变为 可写/可读/可执行
	 *
	 * @param fd           文件描述符
	 * @param mask         标识
	 * @param milliseconds 等待的毫秒数
	 * @return
	 */
	public static int aeWait(int fd, int mask, long milliseconds) {

		// Wait for milliseconds until the given file descriptor becomes writable/readable/exception

/*		struct pollfd pfd;
		int retmask = 0, retval;

		memset(&pfd, 0, sizeof(pfd));
		pfd.fd = fd;
		if (mask & AE_READABLE) pfd.events |= POLLIN;
		if (mask & AE_WRITABLE) pfd.events |= POLLOUT;

		if ((retval = poll(&pfd, 1, milliseconds))== 1) {
			if (pfd.revents & POLLIN) retmask |= AE_READABLE;
			if (pfd.revents & POLLOUT) retmask |= AE_WRITABLE;
			if (pfd.revents & POLLERR) retmask |= AE_WRITABLE;
			if (pfd.revents & POLLHUP) retmask |= AE_WRITABLE;
			return retmask;
		} else {
			return retval;
		}*/

		// TODO 待完成
		return 0;
	}

	/**
	 * 执行事件处理
	 *
	 * @param eventLoop 事件循环
	 * @param flags     处理标识
	 * @return
	 */
	private static int aeProcessEvents(AeEventLoop eventLoop, int flags) throws IOException {

		int processed = 0;

		// flags 表明不需要处理时间事件和文件事件, 直接返回
		if ((flags & AeConstants.AE_TIME_EVENTS) == 0 && (flags & AeConstants.AE_FILE_EVENTS) == 0) {
			return 0;
		}

		// 最大文件描述符不等于 - 1, 表示有文件事件
		// flags 标识符表明要处理时间事件 同时不需要阻塞等待
		if (eventLoop.getMaxFd() != -1 || ((flags & AeConstants.AE_TIME_EVENTS) != 0 && (flags & AeConstants.AE_DONT_WAIT) == 0)) {

			int j;
			AeTimeEvent shortest = null;

			// 隔多长时间执行时间时间
			TimeVal tv = new TimeVal();

			// flags 标识符表明要处理时间事件 同时不需要阻塞等待
			if ((flags & AeConstants.AE_TIME_EVENTS) != 0 && (flags & AeConstants.AE_DONT_WAIT) == 0) {
				shortest = aeSearchNearestTimer(eventLoop);
			}

			if (Objects.nonNull(shortest)) {

				// 当前的时间 多少秒又多少毫秒
				long nowSec = aeGetTime(0);
				long nowMs = aeGetTime(1) - nowSec * 1000;

				// 获取当前时间触发还需要多少毫秒
				long ms = (shortest.getWhenSec() - nowSec) * 1000 + shortest.getWhenMs() - nowMs;
				// 需要秒数小于等于 0, 则设置为 0, 表示立即执行
				tv.setTvSec(ms <= 0 ? 0 : ms / 1000);
				tv.setTvUsec(ms <= 0 ? 0 : (ms % 1000) * 1000);
			} else {

				if ((flags & AeConstants.AE_DONT_WAIT) != 0) {
					tv.setTvSec(0L);
					tv.setTvUsec(0L);
				} else {
					tv = null;
				}
			}

			// TODO 待完成
			int numevents = AE_API.aeApiPoll(eventLoop, tv);

			if (eventLoop.getAfterSleep() != null && (flags & AeConstants.AE_CALL_AFTER_SLEEP) != 0) {
				eventLoop.getAfterSleep().aeBeforeSleepProc(eventLoop);
			}

			for (j = 0; j < numevents; j++) {

				int mask = eventLoop.getFired()[j].getMask();
				int fd = eventLoop.getFired()[j].getFd();
				AeFileEvent fe = eventLoop.getEvents()[fd];

				int fired = 0;

				// 事件的标识是否包含 AeConstants.AE_BARRIER
				int invert = fe.getMask() & AeConstants.AE_BARRIER;

				if (invert != 0 && (fe.getMask() & mask & AeConstants.AE_READABLE) != 0) {
					fe.getRFileProc().aeFileProc(eventLoop, fd, fe.getClientData(), mask);
					fired++;
				}

				if ((fe.getMask() & mask & AeConstants.AE_WRITABLE) != 0) {
					if (fired != 0 || fe.getWFileProc() != fe.getRFileProc()) {
						fe.getWFileProc().aeFileProc(eventLoop, fd, fe.getClientData(), mask);
						fired++;
					}
				}

				if (invert != 0 && (fe.getMask() & mask & AeConstants.AE_READABLE) != 0) {
					if (fired != 0 || fe.getWFileProc() != fe.getRFileProc()) {
						fe.getRFileProc().aeFileProc(eventLoop, fd, fe.getClientData(), mask);
						fired++;
					}
				}
				processed++;
			}
		}

		if ((flags & AeConstants.AE_TIME_EVENTS) != 0) {
			processed += processTimeEvents(eventLoop);
		}

		return processed;
	}

	/**
	 * 获取执行时间最近的事件时间
	 *
	 * @param eventLoop
	 * @return
	 */
	private static AeTimeEvent aeSearchNearestTimer(AeEventLoop eventLoop) {

		AeTimeEvent timeEventHead = eventLoop.getTimeEventHead();

		AeTimeEvent nearest = null;

		while (Objects.nonNull(timeEventHead)) {

			if (Objects.isNull(nearest)) {
				nearest = timeEventHead;
				timeEventHead = timeEventHead.getNext();
				continue;
			}

			if (timeEventHead.getWhenSec() < nearest.getWhenSec()
					|| (timeEventHead.getWhenSec() == nearest.getWhenSec() && timeEventHead.getWhenMs() < nearest.getWhenMs())) {
				nearest = timeEventHead;
			}
			timeEventHead = timeEventHead.getNext();
		}

		return nearest;
	}

	/**
	 * 获取当前的时间
	 *
	 * @param type 0: 以秒的方式表示, 1: 以毫秒的方式表示
	 * @return 当前的时间
	 */
	private static long aeGetTime(int type) {
		return type == 0 ? TimeUtils.getCurrentTimeWithSec() : TimeUtils.getCurrentTimeWithMs();
	}

	/**
	 * 处理事件事件
	 *
	 * @param eventLoop 事件轮询
	 * @return 处理的事件数
	 */
	private static int processTimeEvents(AeEventLoop eventLoop) {

		int processed = 0;
		AeTimeEvent te;
		long maxId;
		LocalDateTime now = LocalDateTime.now();

		// 防止时间回调
		if (now.isBefore(eventLoop.getLastTime())) {

			te = eventLoop.getTimeEventHead();
			while (Objects.nonNull(te)) {
				// 设置执行时间为 0, 保证能执行
				te.setWhenSec(0L);
				te = te.getNext();
			}
		}

		eventLoop.setLastTime(now);
		// 重试赋值为时间事件链表的头部
		te = eventLoop.getTimeEventHead();
		maxId = eventLoop.getTimeEventNextId() - 1;

		// 遍历整个时间时间链表
		while (Objects.nonNull(te)) {

			// 删除所有不需要的时间时间
			if (te.getId() == AeConstants.AE_DELETED_EVENT_ID) {

				AeTimeEvent next = te.getNext();

				if (Objects.nonNull(te.getPrev())) {
					te.getPrev().setNext(next);
				} else {
					eventLoop.setTimeEventHead(next);
				}

				if (Objects.nonNull(next)) {
					next.setPrev(te.getPrev());
				}

				if (Objects.nonNull(te.getFinalizerProc())) {
					te.getFinalizerProc().aeEventFinalizerProc(eventLoop, te.getClientData());
				}

				te = next;
				continue;
			}

			// 事件的 id 大于当前的时间事件的最大 id, 跳过
			if (te.getId() > maxId) {
				te = te.getNext();
				continue;
			}

			long nowSec = aeGetTime(0);
			long nowMs = aeGetTime(1) - nowSec * 1000;

			// 时间事件的执行时间在当前时间之前, 执行
			if (nowSec > te.getWhenSec() || (nowSec == te.getWhenSec() && nowMs >= te.getWhenMs())) {

				long id = te.getId();
				int retval = te.getTimeProc().aeTimeProc(eventLoop, id, te.getClientData());

				processed++;

				if (retval == AeConstants.AE_NOMORE) {
					te.setId(AeConstants.AE_DELETED_EVENT_ID);
				} else {
					aeAddMillisecondsToNow(retval, te);
				}
			}
			te = te.getNext();

		}
		return processed;
	}

	/**
	 * 根据入参的毫秒数重算时间事件执行的时间
	 *
	 * @param milliseconds 间隔多少毫秒再执行
	 * @param aeTimeEvent  时间事件
	 */
	private static void aeAddMillisecondsToNow(long milliseconds, AeTimeEvent aeTimeEvent) {

		long curSec = aeGetTime(0);
		long curMs = aeGetTime(1) - curSec * 1000;

		long whenSec = curSec + milliseconds / 1000;
		long whenMs = curMs + milliseconds % 1000;

		if (whenMs >= 1000) {
			whenSec++;
			whenMs -= 1000;
		}

		aeTimeEvent.setWhenSec(whenSec);
		aeTimeEvent.setWhenMs(whenMs);
	}

}
