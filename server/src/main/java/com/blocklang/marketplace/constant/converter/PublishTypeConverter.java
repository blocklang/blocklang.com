package com.blocklang.marketplace.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.marketplace.constant.PublishType;

public class PublishTypeConverter implements AttributeConverter<PublishType, String> {

	@Override
	public String convertToDatabaseColumn(PublishType attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public PublishType convertToEntityAttribute(String dbData) {
		return PublishType.fromKey(dbData);
	}

}

