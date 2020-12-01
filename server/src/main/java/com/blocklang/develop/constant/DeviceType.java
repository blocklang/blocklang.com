package com.blocklang.develop.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 设备类型
 * 
 * @author jinzw
 *
 */
public enum DeviceType {

	PHONE("01", "Phone"),
	CAR("02", "Car"),
	TV("03", "TV"),
	WEARABLE("04", "Wearable"),
	LITE_WEARABLE("05", "Lite Wearable"),
	SMART_VISION("06", "Smart Vision");
	
	private final String key;
	private final String value;
	
	private DeviceType(String key, String value) {
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
	
	public static DeviceType fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return Arrays.stream(DeviceType.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(null);
	}

	public static DeviceType fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		return Arrays.stream(DeviceType.values())
				.filter((each) -> key.equals(each.getKey()))
				.findFirst()
				.orElse(null);
	}
}
