package com.blocklang.develop.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum NodeCategory {

	FUNCTION("function", "函数定义节点"),
	FUNCTION_CALL("functionCall", "函数调用节点"),
	VARIABLE_SET("variableSet", "设置变量节点"),
	VARIABLE_GET("variableGet", "获取变量节点");

	private final String key;
	private final String value;

	private NodeCategory(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
	
	public static NodeCategory fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return Arrays.stream(NodeCategory.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(null);
	}

	public static NodeCategory fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		return Arrays.stream(NodeCategory.values())
				.filter((each) -> key.equals(each.getKey()))
				.findFirst()
				.orElse(null);
	}
}
