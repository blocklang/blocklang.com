package com.blocklang.marketplace.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.marketplace.constant.ComponentAttrValueType;

public class ComponentAttrValueTypeConverter implements AttributeConverter<ComponentAttrValueType, String> {

	@Override
	public String convertToDatabaseColumn(ComponentAttrValueType attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public ComponentAttrValueType convertToEntityAttribute(String dbData) {
		return ComponentAttrValueType.fromKey(dbData);
	}

}

