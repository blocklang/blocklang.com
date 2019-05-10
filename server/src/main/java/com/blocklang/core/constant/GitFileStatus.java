package com.blocklang.core.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonValue;

public enum GitFileStatus {
	
	UNTRACKED("01", "untracked"),
	ADDED("02", "added"),
	CHANGED("03", "changed"),
	REMOVED("04", "removed"),
	DELETED("05", "deleted"),
	@Deprecated
	MISSING("06", "missing"),
	MODIFIED("07", "modified"),
	CONFLICTING("08", "conflicting");

	private final String key;
	private final String value;

	private GitFileStatus(String key, String value) {
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
