package com.blocklang.core.util;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.NumberUtils;

public class NumberUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(NumberUtil.class);

	public static Optional<Integer> toInt(String text) {
		try {
			return Optional.of(NumberUtils.parseNumber(text, Integer.class));
		} catch (IllegalArgumentException e) {
			logger.error("无法将 ‘" + text + "’ 转换为数字", e);
			return Optional.empty();
		}
	}
}
