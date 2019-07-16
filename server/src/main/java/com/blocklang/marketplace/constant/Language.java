package com.blocklang.marketplace.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Language {
	
	JAVA("01", "Java"),
	TYPESCRIPT("02", "TypeScript"),
	UNKNOWN("99", "Unknown");
	
	private final String key;
	private final String value;

	private Language(String key, String value) {
		this.key = key;
		this.value = value;
	}

	@JsonValue
	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
	
	public static Language fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return Language.UNKNOWN;
		}
		return Arrays.stream(Language.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(Language.UNKNOWN);
	}

	public static Language fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return Language.UNKNOWN;
		}
		return Arrays.stream(Language.values())
				.filter((each) -> key.equals(each.getKey()))
				.findFirst()
				.orElse(Language.UNKNOWN);
	}
}
