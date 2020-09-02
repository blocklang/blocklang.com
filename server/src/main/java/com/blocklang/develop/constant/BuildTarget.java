package com.blocklang.develop.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BuildTarget {
	WEAPP("weapp", "微信小程序"),
	SWAN("swan", "百度小程序"),
	ALIPAY("alipay", "支付宝小程序"),
	TT("tt", "子节跳动小程序"),
	QQ("qq", "QQ小程序"),
	JD("jd", "京东小程序"),
	QUICK_APP("quickapp", "快应用");

	private final String key;
	private final String value;

	private BuildTarget(String key, String value) {
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
	
	public static BuildTarget fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return Arrays.stream(BuildTarget.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(null);
	}

	public static BuildTarget fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		return Arrays.stream(BuildTarget.values())
				.filter((each) -> key.equals(each.getKey()))
				.findFirst()
				.orElse(null);
	}
}
