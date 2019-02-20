package com.blocklang.develop.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.develop.constant.ProjectResourceType;

public class ProjectResourceTypeConverter implements AttributeConverter<ProjectResourceType, String> {

	@Override
	public String convertToDatabaseColumn(ProjectResourceType attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public ProjectResourceType convertToEntityAttribute(String dbData) {
		return ProjectResourceType.fromKey(dbData);
	}

}
