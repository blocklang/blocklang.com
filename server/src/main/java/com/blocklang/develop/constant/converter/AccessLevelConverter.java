package com.blocklang.develop.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.develop.constant.AccessLevel;

public class AccessLevelConverter implements AttributeConverter<AccessLevel, String> {

	@Override
	public String convertToDatabaseColumn(AccessLevel attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public AccessLevel convertToEntityAttribute(String dbData) {
		return AccessLevel.fromKey(dbData);
	}

}
