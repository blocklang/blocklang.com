package com.blocklang.develop.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.develop.constant.AppType;

public class AppTypeConverter implements AttributeConverter<AppType, String> {

	@Override
	public String convertToDatabaseColumn(AppType attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public AppType convertToEntityAttribute(String dbData) {
		return AppType.fromKey(dbData);
	}

}
