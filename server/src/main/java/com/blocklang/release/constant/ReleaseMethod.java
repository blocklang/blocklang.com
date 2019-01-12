package com.blocklang.release.constant;

public enum ReleaseMethod {
	AUTO("01", "自动发布"), UPLOAD("02", "人工上传");

	private final String key;
	private final String value;

	private ReleaseMethod(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

}
