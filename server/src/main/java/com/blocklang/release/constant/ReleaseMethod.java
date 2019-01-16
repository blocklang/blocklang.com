package com.blocklang.release.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum ReleaseMethod {
	
	AUTO("01", "自动发布"), UPLOAD("02", "人工上传");

	private final String key;
	private final String value;

	private ReleaseMethod(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}

	public static ReleaseMethod fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return Arrays.stream(ReleaseMethod.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(null);
	}

	public static ReleaseMethod fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		return Arrays.stream(ReleaseMethod.values())
				.filter((each) -> key.equals(each.getKey()))
				.findFirst()
				.orElse(null);
	}
}
