package com.blocklang.core.constant.converter;

import javax.persistence.AttributeConverter;

import com.blocklang.core.constant.OauthSite;

public class OauthSiteConverter implements AttributeConverter<OauthSite, String> {

	@Override
	public String convertToDatabaseColumn(OauthSite attribute) {
		return attribute == null ? null : attribute.getKey();
	}

	@Override
	public OauthSite convertToEntityAttribute(String dbData) {
		return OauthSite.fromKey(dbData);
	}
}