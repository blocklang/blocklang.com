package com.blocklang.core.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.core.constant.DataType;

public class DataTypeConverter implements AttributeConverter<DataType, String> {

	@Override
	public String convertToDatabaseColumn(DataType attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public DataType convertToEntityAttribute(String dbData) {
		return DataType.fromKey(dbData);
	}
}
