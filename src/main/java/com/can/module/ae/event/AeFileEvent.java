package com.can.module.ae.event;

import com.can.module.ae.process.AeFileProc;
import lombok.Data;

/**
 * <pre>
 * 文件事件
 *
 * </pre>
 *
 * @author
 * @date 2021-12-29  18:00
 */
@Data
public class AeFileEvent {

	/** 事件标识, 取值范围 {@link com.can.module.ae.enums.AeMaskEnum} */
	int mask;

	AeFileProc rFileProc;

	AeFileProc wFileProc;

	Object clientData;
}
