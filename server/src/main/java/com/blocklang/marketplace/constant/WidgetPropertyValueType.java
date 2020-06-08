package com.blocklang.marketplace.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

/**
 * 使用于组件属性值、组件属性的可选值、部件事件的参数值
 * 
 * @author Zhengwei Jin
 *
 */
public enum WidgetPropertyValueType {

	NUMBER("number", "数字"),
	STRING("string", "字符串"),
	BOOLEAN("boolean", "布尔类型"),
	FUNCTION("function", "函数"),
	SLOT("slot", "插槽");
	
	private final String key;
	private final String value;

	private WidgetPropertyValueType(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
	
	public static WidgetPropertyValueType fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return Arrays.stream(WidgetPropertyValueType.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(null);
	}

	public static WidgetPropertyValueType fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		return Arrays.stream(WidgetPropertyValueType.values())
				.filter((each) -> key.equals(each.getKey()))
				.findFirst()
				.orElse(null);
	}
	
}
