package com.blocklang.develop.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.develop.constant.DeviceType;

public class DeviceTypeConverter implements AttributeConverter<DeviceType, String> {

	@Override
	public String convertToDatabaseColumn(DeviceType attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public DeviceType convertToEntityAttribute(String dbData) {
		return DeviceType.fromKey(dbData);
	}

}
