package com.blocklang.release.constant.converter;

import java.util.Arrays;

import javax.persistence.AttributeConverter;

import org.apache.commons.lang3.StringUtils;

import com.blocklang.release.constant.Arch;

public class ArchConverter implements AttributeConverter<Arch, String> {

	@Override
	public String convertToDatabaseColumn(Arch attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public Arch convertToEntityAttribute(String dbData) {
		if(StringUtils.isBlank(dbData)) {
			return null;
		}
		return Arrays.stream(Arch.values())
				.filter((each) -> dbData.equals(each.getKey()))
				.findFirst()
				.orElse(null);
	}

}