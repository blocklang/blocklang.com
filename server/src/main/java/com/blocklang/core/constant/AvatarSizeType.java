package com.blocklang.core.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum AvatarSizeType {
	
	SMALL("01", "Small"),
	MEDIUM("02", "Medium"),
	LARGE("03", "Large");

	private final String key;
	private final String value;

	private AvatarSizeType(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
	
	public static AvatarSizeType fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return Arrays.stream(AvatarSizeType.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(null);
	}

	public static AvatarSizeType fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		return Arrays.stream(AvatarSizeType.values())
				.filter((each) -> key.equals(each.getValue()))
				.findFirst()
				.orElse(null);
	}
}
