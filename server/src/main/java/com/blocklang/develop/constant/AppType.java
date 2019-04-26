package com.blocklang.develop.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum AppType {
	
	WEB("01", "Web", IconClass.WEB),
	ANDROID("02", "Android", IconClass.ANDROID),
	IOS("03", "ios", IconClass.IOS),
	WECHAT_MINI_APP("04", "微信小程序", IconClass.WECHAT),
	ALIPAY_MINI_APP("05", "支付宝小程序", IconClass.ALIPAY),
	QUICK_APP("06", "快应用", IconClass.QUICK_APP),
	UNKNOWN("99", "Unknown", "");

	private final String key;
	private final String value;
	private final String icon;

	private AppType(String key, String value, String icon) {
		this.key = key;
		this.value = value;
		this.icon = icon;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
	
	public String getIcon() {
		return this.icon;
	}
	
	public static AppType fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return UNKNOWN;
		}
		return Arrays.stream(AppType.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(UNKNOWN);
	}

	public static AppType fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return UNKNOWN;
		}
		return Arrays.stream(AppType.values())
				.filter((each) -> key.equals(each.getKey()))
				.findFirst()
				.orElse(UNKNOWN);
	}

}
