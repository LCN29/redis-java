package com.can.util;

import com.can.config.DefaultConfig;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;

/**
 * <pre>
 *
 * </pre>
 *
 * @author
 * @date 2021-12-30  9:40
 */
public class TimeUtils {

	/**
	 * 获取当前的时间, 单位秒
	 *
	 * @return
	 */
	public static long getCurrentTimeWithSec() {

		return LocalDateTime.now().toEpochSecond(ZoneOffset.of(DefaultConfig.TIME_ZONE));
	}

	/**
	 * 获取当前的时间, 单位毫秒
	 *
	 * @return
	 */
	public static long getCurrentTimeWithMs() {
		return LocalDateTime.now().toInstant(ZoneOffset.of(DefaultConfig.TIME_ZONE)).toEpochMilli();
	}


	public static void main(String[] args) {
		LocalDateTime now = LocalDateTime.now();

		System.out.println(now.getLong(ChronoField.OFFSET_SECONDS));
		System.out.println(now.getLong(ChronoField.MILLI_OF_DAY));
		System.out.println(now.getLong(ChronoField.MICRO_OF_DAY));
	}
}
