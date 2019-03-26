package com.blocklang.config.oauth2;

import java.net.URI;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.blocklang.core.constant.OauthSite;

public class CustomOAuth2AuthorizationCodeGrantRequestEntityConverter implements Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>>{

	private OAuth2AuthorizationCodeGrantRequestEntityConverter defaultConverter;

	public CustomOAuth2AuthorizationCodeGrantRequestEntityConverter() {
		defaultConverter = new OAuth2AuthorizationCodeGrantRequestEntityConverter();
	}

	@Override
	public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
		ClientRegistration clientRegistration = authorizationCodeGrantRequest.getClientRegistration();
		String registrationId = authorizationCodeGrantRequest.getClientRegistration().getRegistrationId();
		
		if(registrationId.equalsIgnoreCase(OauthSite.QQ.getValue())) {
			// QQ 互联使用的是 GET 请求
			MultiValueMap<String, String> queryParameters = this.buildQueryParameters(authorizationCodeGrantRequest);
			URI uri = UriComponentsBuilder.fromUriString(clientRegistration.getProviderDetails().getTokenUri())
					.queryParams(queryParameters).build().toUri();
			return new RequestEntity<>(HttpMethod.GET, uri);
		}
		
		// Github 等使用的是 POST 请求
		return defaultConverter.convert(authorizationCodeGrantRequest);		
	}

	private MultiValueMap<String, String> buildQueryParameters(OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
		ClientRegistration clientRegistration = authorizationCodeGrantRequest.getClientRegistration();
		OAuth2AuthorizationExchange authorizationExchange = authorizationCodeGrantRequest.getAuthorizationExchange();

		MultiValueMap<String, String> queryParameters = new LinkedMultiValueMap<>();
		queryParameters.add(OAuth2ParameterNames.GRANT_TYPE, authorizationCodeGrantRequest.getGrantType().getValue());
		queryParameters.add(OAuth2ParameterNames.CODE, authorizationExchange.getAuthorizationResponse().getCode());
		queryParameters.add(OAuth2ParameterNames.REDIRECT_URI, authorizationExchange.getAuthorizationRequest().getRedirectUri());
		queryParameters.add(OAuth2ParameterNames.CLIENT_ID, clientRegistration.getClientId());
		queryParameters.add(OAuth2ParameterNames.CLIENT_SECRET, clientRegistration.getClientSecret());

		return queryParameters;
	}
	
}
