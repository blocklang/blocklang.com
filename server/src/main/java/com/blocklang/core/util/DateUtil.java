package com.blocklang.core.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public abstract class DateUtil {

	public static LocalDateTime ofSecond(long seconds) {
		return Instant.ofEpochSecond(seconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
	}
	
}
