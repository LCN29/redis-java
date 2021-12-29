package com.can;

import com.can.module.server.RedisServer;
import com.can.module.ae.Ae;
import com.can.module.server.RedisServerConstants;

import java.util.Objects;

/**
 * Hello world!
 */
public class ApplicationStarter {

    /**
     * Redis 运行数据存储对象
     */
    private final static RedisServer REDIS_SERVER = new RedisServer();


    public static void main(String[] args) {

        // 初始化配置
        //initServerConfig();
        // 加载并解析配置文件
        //loadServerConfig();

        // 初始化服务器内部变量
        initServer();

        Ae.aeMain(REDIS_SERVER.getEl());
        Ae.aeDeleteEventLoop(REDIS_SERVER.getEl());
    }


    private static void initServerConfig() {
    }

    private static void loadServerConfig() {
    }

    private static void initServer() {

        // 创建 AeEventLoop, 同时保存到 redisServer 的 el 属性
        REDIS_SERVER.setEl(Ae.aeCreateEventLoop(RedisServerConstants.CONFIG_DEFAULT_MAX_CLIENTS + RedisServerConstants.CONFIG_MIN_RESERVED_FDS));

        if (Objects.isNull(REDIS_SERVER.getEl())) {
            System.err.println("Failed creating event loop");
            return;
        }
    }

    public static RedisServer getRedisServer() {
        return REDIS_SERVER;
    }

}
