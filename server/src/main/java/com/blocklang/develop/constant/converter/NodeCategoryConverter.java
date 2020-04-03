package com.blocklang.develop.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.develop.constant.NodeCategory;

public class NodeCategoryConverter implements AttributeConverter<NodeCategory, String> {

	@Override
	public String convertToDatabaseColumn(NodeCategory attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public NodeCategory convertToEntityAttribute(String dbData) {
		return NodeCategory.fromKey(dbData);
	}
}
