package com.blocklang.release.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.release.constant.ReleaseResult;

public class ReleaseResultConverter implements AttributeConverter<ReleaseResult, String>{
	
	@Override
	public String convertToDatabaseColumn(ReleaseResult attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public ReleaseResult convertToEntityAttribute(String dbData) {
		return ReleaseResult.fromKey(dbData);
	}

}
