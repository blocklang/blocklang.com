package com.blocklang.marketplace.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.marketplace.constant.RepoType;

public class RepoTypeConverter implements AttributeConverter<RepoType, String> {

	@Override
	public String convertToDatabaseColumn(RepoType attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public RepoType convertToEntityAttribute(String dbData) {
		return RepoType.fromKey(dbData);
	}

}
