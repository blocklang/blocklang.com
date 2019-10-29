package com.blocklang.marketplace.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.marketplace.constant.Language;

public class LanguageConverter implements AttributeConverter<Language, String> {

	@Override
	public String convertToDatabaseColumn(Language attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public Language convertToEntityAttribute(String dbData) {
		return Language.fromKey(dbData);
	}

}
