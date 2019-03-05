package com.blocklang.develop.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum DeployState {

	UNDEPLOY("01", "UnDeploy");

	private final String key;
	private final String value;

	private DeployState(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
	
	public static DeployState fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return Arrays.stream(DeployState.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(null);
	}

	public static DeployState fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		return Arrays.stream(DeployState.values())
				.filter((each) -> key.equals(each.getKey()))
				.findFirst()
				.orElse(null);
	}
}
