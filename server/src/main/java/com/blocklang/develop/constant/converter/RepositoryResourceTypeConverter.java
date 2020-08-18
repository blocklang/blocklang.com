package com.blocklang.develop.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.develop.constant.RepositoryResourceType;

public class RepositoryResourceTypeConverter implements AttributeConverter<RepositoryResourceType, String> {

	@Override
	public String convertToDatabaseColumn(RepositoryResourceType attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public RepositoryResourceType convertToEntityAttribute(String dbData) {
		return RepositoryResourceType.fromKey(dbData);
	}

}
