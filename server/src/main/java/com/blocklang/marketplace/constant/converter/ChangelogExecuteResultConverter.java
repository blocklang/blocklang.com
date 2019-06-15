package com.blocklang.marketplace.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.marketplace.constant.ChangelogExecuteResult;

public class ChangelogExecuteResultConverter implements AttributeConverter<ChangelogExecuteResult, String> {

	@Override
	public String convertToDatabaseColumn(ChangelogExecuteResult attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public ChangelogExecuteResult convertToEntityAttribute(String dbData) {
		return ChangelogExecuteResult.fromKey(dbData);
	}

}

