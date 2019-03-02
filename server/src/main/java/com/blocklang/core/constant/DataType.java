package com.blocklang.core.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum DataType {
	
	STRING("01", "String"),
	NUMBER("02", "Number");

	private final String key;
	private final String value;

	private DataType(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
	
	public static DataType fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return Arrays.stream(DataType.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(null);
	}

	public static DataType fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		return Arrays.stream(DataType.values())
				.filter((each) -> key.equals(each.getKey()))
				.findFirst()
				.orElse(null);
	}
}
