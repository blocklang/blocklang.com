package com.blocklang.develop.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.develop.constant.FlowType;

public class FlowTypeConverter implements AttributeConverter<FlowType, String> {

	@Override
	public String convertToDatabaseColumn(FlowType attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public FlowType convertToEntityAttribute(String dbData) {
		return FlowType.fromKey(dbData);
	}
}

