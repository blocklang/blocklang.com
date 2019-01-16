package com.blocklang.release.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum OsType {

	UNKNOWN("01", "Unknown"),
	ANDROID("02", "Android"),
	EMSCRIPTEN("03", "Emscripten"),
	LINUX("04", "Linux"),
	REDHAT("05", "Redhat"),
	UBUNTU("06", "Ubuntu"),
	DEBIAN("07", "Debian"),
	ARCH("08", "Arch"),
	CENTOS("09", "Centos"),
	FEDORA("10", "Fedora"),
	ALPINE("11", "Alpine"),
	MACOS("12", "MacOS"),
	REDOX("13", "Redox"),
	WINDOWS("14", "Windows"),
	ANY("99", "Any");
	
	private final String key;
	private final String value;

	private OsType(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
	
	public static OsType fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return OsType.UNKNOWN;
		}
		return Arrays.stream(OsType.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(OsType.UNKNOWN);
	}

	public static OsType fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return OsType.UNKNOWN;
		}
		return Arrays.stream(OsType.values())
				.filter((each) -> key.equals(each.getValue()))
				.findFirst()
				.orElse(OsType.UNKNOWN);
	}
}
