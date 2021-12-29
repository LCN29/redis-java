package com.can.module.server;

/**
 * <pre>
 *
 * </pre>
 *
 * @author lcn29
 * @date 2021-12-29 21:43
 */
public class RedisServerConstants {

    public final static int CONFIG_DEFAULT_MAX_CLIENTS = 1000;

    public final static int CONFIG_MIN_RESERVED_FDS = 32;

    public final static int CONFIG_FDSET_INCR = CONFIG_MIN_RESERVED_FDS + 96;
}
