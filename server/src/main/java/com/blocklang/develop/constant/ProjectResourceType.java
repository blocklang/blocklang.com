package com.blocklang.develop.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectResourceType {
	
	PROJECT("01", "项目"),
	MAIN("02", "项目入口"),
	// 在命名上，页面比程序模块更准确，因为页面中并没有包含后台服务，仅仅是前台页面。
	PAGE("03", "页面"),
	GROUP("04", "分组"),
	PANE("05", "面板"),
	PAGE_TEMPLET("06", "页面模板"),
	FILE("07", "文件"),
	SERVICE("08", "服务"), // REST API
	DEPENDENCE("09", "依赖"),
	BUILD("10", "Build配置信息");

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
