package com.blocklang.develop.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.develop.constant.DeployState;

public class DeployStateConverter implements AttributeConverter<DeployState, String>{
	@Override
	public String convertToDatabaseColumn(DeployState attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public DeployState convertToEntityAttribute(String dbData) {
		return DeployState.fromKey(dbData);
	}
}
