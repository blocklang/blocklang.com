package com.blocklang.core.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum OauthSite {
	
	GITHUB("10", "Github"),
	WECHAT("11", "WeChat");

	private final String key;
	private final String value;

	private OauthSite(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
	
	public static OauthSite fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return Arrays.stream(OauthSite.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(null);
	}

	public static OauthSite fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		return Arrays.stream(OauthSite.values())
				.filter((each) -> key.equals(each.getKey()))
				.findFirst()
				.orElse(null);
	}
}
