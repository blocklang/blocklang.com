package com.blocklang.release.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.release.constant.ReleaseMethod;

public class ReleaseMethodConverter implements AttributeConverter<ReleaseMethod, String> {

	@Override
	public String convertToDatabaseColumn(ReleaseMethod attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public ReleaseMethod convertToEntityAttribute(String dbData) {
		return ReleaseMethod.fromKey(dbData);
	}

}
