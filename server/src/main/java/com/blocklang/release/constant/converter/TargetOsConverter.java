package com.blocklang.release.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.release.constant.TargetOs;

public class TargetOsConverter implements AttributeConverter<TargetOs, String> {

	@Override
	public String convertToDatabaseColumn(TargetOs attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public TargetOs convertToEntityAttribute(String dbData) {
		return TargetOs.fromKey(dbData);
	}

}