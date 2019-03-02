package com.blocklang.develop.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum AccessLevel {

	READ("01", "Read"),
	WRITE("02", "Write"),
	ADMIN("03", "Admin");

	private final String key;
	private final String value;

	private AccessLevel(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
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
