package com.blocklang.develop.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.develop.constant.BindSource;

public class BindSourceConverter implements AttributeConverter<BindSource, String> {

	@Override
	public String convertToDatabaseColumn(BindSource attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public BindSource convertToEntityAttribute(String dbData) {
		return BindSource.fromKey(dbData);
	}
}
