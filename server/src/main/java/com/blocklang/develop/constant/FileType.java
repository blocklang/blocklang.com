package com.blocklang.develop.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

/**
 * 文件类型
 * 
 * 仅支持文本文件。
 * 
 * @author Zhengwei Jin
 *
 */
public enum FileType {
	
	MARKDOWN("01", "Markdown");

	private final String key;
	private final String value;

	private FileType(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
	
	public static FileType fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return Arrays.stream(FileType.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(null);
	}

	public static FileType fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		return Arrays.stream(FileType.values())
				.filter((each) -> key.equals(each.getValue()))
				.findFirst()
				.orElse(null);
	}
}
