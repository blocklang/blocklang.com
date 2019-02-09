package com.blocklang.core.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.core.constant.AvatarSizeType;

public class AvatarSizeTypeConverter implements AttributeConverter<AvatarSizeType, String> {

	@Override
	public String convertToDatabaseColumn(AvatarSizeType attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public AvatarSizeType convertToEntityAttribute(String dbData) {
		return AvatarSizeType.fromKey(dbData);
	}
}
