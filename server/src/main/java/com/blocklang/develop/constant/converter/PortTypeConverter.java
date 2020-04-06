package com.blocklang.develop.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.develop.constant.PortType;

public class PortTypeConverter implements AttributeConverter<PortType, String> {

	@Override
	public String convertToDatabaseColumn(PortType attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public PortType convertToEntityAttribute(String dbData) {
		return PortType.fromKey(dbData);
	}
}

