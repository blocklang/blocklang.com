package com.blocklang.release.constant.converter;

import java.util.Arrays;

import javax.persistence.AttributeConverter;

import org.apache.commons.lang3.StringUtils;

import com.blocklang.release.constant.ReleaseMethod;

public class ReleaseMethodConverter implements AttributeConverter<ReleaseMethod, String> {

	@Override
	public String convertToDatabaseColumn(ReleaseMethod attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public ReleaseMethod convertToEntityAttribute(String dbData) {
		if(StringUtils.isBlank(dbData)) {
			return null;
		}
		return Arrays.stream(ReleaseMethod.values())
				.filter((each) -> dbData.equals(each.getKey()))
				.findFirst()
				.orElse(null);
	}

}
