package com.can.module.ae.event;

import lombok.Data;

/**
 * <pre>
 * 已触发的事件
 * </pre>
 *
 * @author
 * @date 2021-12-29  18:11
 */
@Data
public class AeFiredEvent {

	int fd;

	int mask;
}
