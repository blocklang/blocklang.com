package com.blocklang.release.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.release.constant.OsType;

public class OsTypeConverter implements AttributeConverter<OsType, String> {

	@Override
	public String convertToDatabaseColumn(OsType attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public OsType convertToEntityAttribute(String dbData) {
		return OsType.fromKey(dbData);
	}
}
