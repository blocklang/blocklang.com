package com.blocklang.develop.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum AppType {
	
	WEB("01", "Web"),
	ANDROID("02", "Android"),
	IOS("03", "Web"),
	WECHAT_MINI_APP("04", "微信小程序"),
	ALIPAY_MINI_APP("05", "支付宝小程序"),
	QUICK_APP("06", "快应用");

	private final String key;
	private final String value;

	private AppType(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
	
	public static AppType fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return Arrays.stream(AppType.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(null);
	}

	public static AppType fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		return Arrays.stream(AppType.values())
				.filter((each) -> key.equals(each.getValue()))
				.findFirst()
				.orElse(null);
	}
}
