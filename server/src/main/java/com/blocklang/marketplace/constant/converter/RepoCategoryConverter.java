package com.blocklang.marketplace.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.marketplace.constant.RepoCategory;

public class RepoCategoryConverter implements AttributeConverter<RepoCategory, String> {

	@Override
	public String convertToDatabaseColumn(RepoCategory attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public RepoCategory convertToEntityAttribute(String dbData) {
		return RepoCategory.fromKey(dbData);
	}

}
