package com.blocklang.marketplace.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.marketplace.constant.WidgetPropertyValueType;

public class WidgetPropertyValueTypeConverter implements AttributeConverter<WidgetPropertyValueType, String> {

	@Override
	public String convertToDatabaseColumn(WidgetPropertyValueType attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public WidgetPropertyValueType convertToEntityAttribute(String dbData) {
		return WidgetPropertyValueType.fromKey(dbData);
	}

}

