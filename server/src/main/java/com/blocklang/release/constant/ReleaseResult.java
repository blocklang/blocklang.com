package com.blocklang.release.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ReleaseResult {
	
	INITED("01", "Inited"),
	STARTED("02", "Started"),
	FAILED("03", "Failed"),
	PASSED("04", "Passed"),
	CANCELED("05", "Canceled");
	
	private final String key;
	private final String value;

	private ReleaseResult(String key, String value) {
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
	
	public static ReleaseResult fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return ReleaseResult.INITED;
		}
		return Arrays.stream(ReleaseResult.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(ReleaseResult.INITED);
	}

	public static ReleaseResult fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return ReleaseResult.INITED;
		}
		return Arrays.stream(ReleaseResult.values())
				.filter((each) -> key.equals(each.getKey()))
				.findFirst()
				.orElse(ReleaseResult.INITED);
	}
}
