package com.blocklang.config.oauth2.qq;

import java.net.URI;
import java.util.Collections;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.web.util.UriComponentsBuilder;

public class QqOpenIdRequestEntityConverter implements Converter<OAuth2UserRequest, RequestEntity<?>>{
	
	private String getOpenIdUrl;
	
	public QqOpenIdRequestEntityConverter(String getOpenIdUrl) {
		this.getOpenIdUrl = getOpenIdUrl;
	}
	
	@Override
	public RequestEntity<?> convert(OAuth2UserRequest userRequest) {
		HttpMethod httpMethod = HttpMethod.GET;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		URI uri = UriComponentsBuilder.fromUriString(getOpenIdUrl).queryParam(OAuth2ParameterNames.ACCESS_TOKEN, userRequest.getAccessToken().getTokenValue()).build().toUri();
		return new RequestEntity<>(headers, httpMethod, uri);
	}

}
