package com.can.module.server;

import com.can.module.ae.AeEventLoop;
import lombok.Data;

/**
 * <pre>
 *
 * </pre>
 *
 * @author lcn29
 * @date 2021-12-29 21:38
 */
@Data
public class RedisServer {

    /**
     * 事件轮询
     */
    private AeEventLoop el;


}
