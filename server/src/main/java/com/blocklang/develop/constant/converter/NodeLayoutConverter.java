package com.blocklang.develop.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.develop.constant.NodeLayout;

public class NodeLayoutConverter implements AttributeConverter<NodeLayout, String> {

	@Override
	public String convertToDatabaseColumn(NodeLayout attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public NodeLayout convertToEntityAttribute(String dbData) {
		return NodeLayout.fromKey(dbData);
	}
}
