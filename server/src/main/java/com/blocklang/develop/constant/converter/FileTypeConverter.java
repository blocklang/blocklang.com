package com.blocklang.develop.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.develop.constant.FileType;

public class FileTypeConverter implements AttributeConverter<FileType, String> {

	@Override
	public String convertToDatabaseColumn(FileType attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public FileType convertToEntityAttribute(String dbData) {
		return FileType.fromKey(dbData);
	}
}
