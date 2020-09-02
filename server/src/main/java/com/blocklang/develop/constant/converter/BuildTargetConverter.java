package com.blocklang.develop.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.develop.constant.BuildTarget;

public class BuildTargetConverter implements AttributeConverter<BuildTarget, String> {

	@Override
	public String convertToDatabaseColumn(BuildTarget attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public BuildTarget convertToEntityAttribute(String dbData) {
		return BuildTarget.fromKey(dbData);
	}

}
