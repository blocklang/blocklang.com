package com.blocklang.develop.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AccessLevel {
	
	FORBIDDEN("01", "Forbidden", 1), // 未设置和 forbidden 表示同一个意思，即没有权限
	READ("02", "Read", 2),
	WRITE("03", "Write", 3),
	ADMIN("04", "Admin", 4);
	
	private final String key;
	private final String value;
	private final int score;

	private AccessLevel(String key, String value, int score) {
		this.key = key;
		this.value = value;
		this.score = score;
	}

	@JsonValue
	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
	
	public int getScore() {
		return this.score;
	}
	
	public static AccessLevel fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return Arrays.stream(AccessLevel.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(null);
	}

	public static AccessLevel fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		return Arrays.stream(AccessLevel.values())
				.filter((each) -> key.equals(each.getKey()))
				.findFirst()
				.orElse(null);
	}
}
