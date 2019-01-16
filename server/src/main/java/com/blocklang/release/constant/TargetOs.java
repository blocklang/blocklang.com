package com.blocklang.release.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum TargetOs {
	
	UNKNOWN("01", "Unknown"),
	ANDROID("02", "Android"),
	BITRIG("03", "Bitrig"),
	CLOUDABI("04", "CloudABI"),
	DRAGONFLY("05", "Dragonfly"),
	EMSCRIPTEN("06", "Emscripten"),
	FREEBSD("07", "FreeBSD"),
	FUCHSIA("08", "Fuchsia"),
	HAIKU("09", "Haiku"),
	IOS("10", "iOS"),
	LINUX("11", "Linux"),
	MACOS("12", "MacOS"),
	NETBSD("13", "NetBSD"),
	OPENBSD("14", "OpenBSD"),
	REDOX("15", "Redox"),
	SOLARIS("16", "Solaris"),
	WINDOWS("17", "Windows"),
	ANY("99", "Any");

	private final String key;
	private final String value;

	private TargetOs(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
	
	public static TargetOs fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return TargetOs.UNKNOWN;
		}
		return Arrays.stream(TargetOs.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(TargetOs.UNKNOWN);
	}

	public static TargetOs fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return TargetOs.UNKNOWN;
		}
		return Arrays.stream(TargetOs.values())
				.filter((each) -> key.equals(each.getValue()))
				.findFirst()
				.orElse(TargetOs.UNKNOWN);
	}
}
