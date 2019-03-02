package com.blocklang.release.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum BuildResult {

	INITED("01", "Inited"),
	STARTED("02", "Started"),
	FAILED("03", "Failed"),
	PASSED("04", "Passed"),
	CANCELED("05", "Canceled");
	
	private final String key;
	private final String value;

	private BuildResult(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
	
	public static BuildResult fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return BuildResult.INITED;
		}
		return Arrays.stream(BuildResult.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(BuildResult.INITED);
	}

	public static BuildResult fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return BuildResult.INITED;
		}
		return Arrays.stream(BuildResult.values())
				.filter((each) -> key.equals(each.getKey()))
				.findFirst()
				.orElse(BuildResult.INITED);
	}
}
