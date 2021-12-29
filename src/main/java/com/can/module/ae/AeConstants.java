package com.can.module.ae;

/**
 * <pre>
 * Ae 模块使用到的常量
 * </pre>
 *
 * @author lcn29
 * @date 2021-12-29 21:05
 */
public class AeConstants {

    /**
     * 未注册任何事件
     */
    public final static int AE_NONE = 0;

    /**
     * 当文件描述符为可读时触发
     */
    public final static int AE_READABLE = 1;

    /**
     * 当文件描述符为可写时触发
     */
    public final static int AE_WRITABLE = 2;

    /**
     * 如果同时设置了一个事件为可读和可写, 如果可读事件已在同一事件循环迭代中触发，则永远不要再触发该事件
     * 但是配置为这个，可以以先写后读的方式执行
     */
    public final static int AE_BARRIER = 3;


    /** 文件事件 00000000 00000000 00000000 00000001 */
    public final static int AE_FILE_EVENTS = 1;

    /** 时间事件 00000000 00000000 00000000 00000010 */
    public final static int AE_TIME_EVENTS = 2;

    /** 所有事件, 结果就是 3, 00000000 00000000 00000000 00000011 */
    public final static int AE_ALL_EVENTS =  (AE_FILE_EVENTS | AE_TIME_EVENTS);

    /** 时间事件需要阻塞等待 00000000 00000000 00000000 00000100 */
    public final static int AE_DONT_WAIT = 4;

    /** 00000000 00000000 00000000 00001000 */
    public final static int AE_CALL_AFTER_SLEEP = 8;

}
