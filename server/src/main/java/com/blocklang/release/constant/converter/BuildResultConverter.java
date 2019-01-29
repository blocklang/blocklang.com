package com.blocklang.release.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.release.constant.BuildResult;

public class BuildResultConverter implements AttributeConverter<BuildResult, String>{
	@Override
	public String convertToDatabaseColumn(BuildResult attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public BuildResult convertToEntityAttribute(String dbData) {
		return BuildResult.fromKey(dbData);
	}
}
