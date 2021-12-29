package com.can.module.ae;

import com.can.ApplicationStarter;
import com.can.module.ae.event.AeFileEvent;
import com.can.module.ae.event.AeFiredEvent;
import com.can.module.ae.event.AeTimeEvent;

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

    /**
     * 创建 AeEventLoop
     * @param setSize
     * @return
     */
    public static AeEventLoop aeCreateEventLoop(int setSize) {

        AeEventLoop aeEventLoop = new AeEventLoop();

        aeEventLoop.setSetSize(setSize);
        aeEventLoop.setEvents(new AeFileEvent[setSize]);
        aeEventLoop.setFired(new AeFiredEvent[setSize]);
        aeEventLoop.setLastTime(LocalDateTime.now());

        aeEventLoop.setTimeEventNextId(0);
        aeEventLoop.setStop(0);
        aeEventLoop.setMaxFd(-1);

        // TODO aeApiCreate

        for (int i = 0; i < setSize; i++) {
            aeEventLoop.getEvents()[i].setMask(AeConstants.AE_NONE);
        }
        return aeEventLoop;
    }

    /**
     * 清除 AeEventLoop
     * @param eventLoop
     */
    public static void aeDeleteEventLoop(AeEventLoop eventLoop) {

        // TODO aeApiFree

        eventLoop.setEvents(null);
        eventLoop.setFired(null);
        eventLoop.setTimeEventHead(null);
        ApplicationStarter.getRedisServer().setEl(null);
    }

    /**
     * 启动 AeEventLoop
     * @param eventLoop
     */
    public static void aeMain(AeEventLoop eventLoop) {

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
     * 执行事件处理
     * @param eventLoop 事件循环
     * @param flags 处理标识
     * @return
     */
    private static int aeProcessEvents(AeEventLoop eventLoop, int flags) {

        int processed = 0, numevents;

        // flags 表明不需要处理时间事件 和 文件事件, 直接返回
        if ((flags & AeConstants.AE_TIME_EVENTS) == 0 && (flags & AeConstants.AE_FILE_EVENTS) == 0) {
            return 0;
        }

        // 最大文件描述符不等于 - 1, 表示有文件事件
        // flags 标识符表明要处理时间事件 同时不需要阻塞等待
        if (eventLoop.getMaxFd() != -1 || ((flags & AeConstants.AE_TIME_EVENTS) != 0 && (flags & AeConstants.AE_DONT_WAIT) == 0)) {

            int j;
            AeTimeEvent shortest = null;


            if ((flags & AeConstants.AE_TIME_EVENTS) != 0 && (flags & AeConstants.AE_DONT_WAIT) == 0) {
                shortest = aeSearchNearestTimer(eventLoop);
            }
        }

        return 1;
    }

    private static AeTimeEvent aeSearchNearestTimer(AeEventLoop eventLoop) {
        // TODO
        return null;
    }
}
