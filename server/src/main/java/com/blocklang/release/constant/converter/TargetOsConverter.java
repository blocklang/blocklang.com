package com.blocklang.release.constant.converter;

import java.util.Arrays;

import javax.persistence.AttributeConverter;

import org.apache.commons.lang3.StringUtils;

import com.blocklang.release.constant.TargetOs;

public class TargetOsConverter implements AttributeConverter<TargetOs, String> {

	@Override
	public String convertToDatabaseColumn(TargetOs attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public TargetOs convertToEntityAttribute(String dbData) {
		if(StringUtils.isBlank(dbData)) {
			return null;
		}
		return Arrays.stream(TargetOs.values())
				.filter((each) -> dbData.equals(each.getKey()))
				.findFirst()
				.orElse(null);
	}

}