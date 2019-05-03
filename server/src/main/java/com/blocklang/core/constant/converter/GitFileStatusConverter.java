package com.blocklang.core.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.core.constant.GitFileStatus;

public class GitFileStatusConverter implements AttributeConverter<GitFileStatus, String> {

	@Override
	public String convertToDatabaseColumn(GitFileStatus attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public GitFileStatus convertToEntityAttribute(String dbData) {
		return GitFileStatus.fromKey(dbData);
	}
}