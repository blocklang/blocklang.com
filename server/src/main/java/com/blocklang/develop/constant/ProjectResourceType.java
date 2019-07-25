package com.blocklang.develop.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectResourceType {
	
	// 在命名上，页面比程序模块更准确，因为页面中并没有包含后台服务，仅仅是前台页面。
	PAGE("01", "页面"),
	GROUP("02", "分组"),
	PANE("03", "面板"),
	PAGE_TEMPLET("04", "页面模板"),
	FILE("05", "文件"),
	SERVICE("06", "服务"), // REST API
	DEPENDENCE("07", "依赖"); 

	private final String key;
	private final String value;

	private ProjectResourceType(String key, String value) {
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
