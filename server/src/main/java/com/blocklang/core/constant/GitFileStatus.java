package com.blocklang.core.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum GitFileStatus {
	
	UNTRACKED("01", "untracked"),
	ADDED("02", "added"),
	CHANGE("03", "change"),
	REMOVED("04", "removed"),
	DELETED("05", "deleted"),
	MISSING("06", "missing"),
	MODIFIED("07", "modified"),
	CONFLICTING("08", "conflicting");

	private final String key;
	private final String value;

	private GitFileStatus(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
	
	public static GitFileStatus fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return Arrays.stream(GitFileStatus.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(null);
	}

	public static GitFileStatus fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		return Arrays.stream(GitFileStatus.values())
				.filter((each) -> key.equals(each.getKey()))
				.findFirst()
				.orElse(null);
	}
}
