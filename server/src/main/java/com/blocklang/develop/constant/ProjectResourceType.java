package com.blocklang.develop.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum ProjectResourceType {
	
	FUNCTION("01", "功能模块"),
	PROGRAM("02", "程序模块"),
	PANE("03", "面板"),
	PROGRAM_TEMPLET("04", "程序模块模板"),
	FILE("05", "文件"),
	SERVICE("06", "服务"); // REST API

	private final String key;
	private final String value;

	private ProjectResourceType(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
	
	public static ProjectResourceType fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return Arrays.stream(ProjectResourceType.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(null);
	}

	public static ProjectResourceType fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		return Arrays.stream(ProjectResourceType.values())
				.filter((each) -> key.equals(each.getKey()))
				.findFirst()
				.orElse(null);
	}
}
