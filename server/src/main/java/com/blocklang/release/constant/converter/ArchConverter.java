package com.blocklang.release.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.release.constant.Arch;

public class ArchConverter implements AttributeConverter<Arch, String> {

	@Override
	public String convertToDatabaseColumn(Arch attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public Arch convertToEntityAttribute(String dbData) {
		return Arch.fromKey(dbData);
	}

}