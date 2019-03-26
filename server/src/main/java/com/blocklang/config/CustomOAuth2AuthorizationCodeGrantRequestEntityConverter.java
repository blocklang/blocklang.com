package com.blocklang.config;

import java.net.URI;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
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

	/**
	 * Returns the {@link RequestEntity} used for the Access Token Request.
	 *
	 * @param authorizationCodeGrantRequest the authorization code grant request
	 * @return the {@link RequestEntity} used for the Access Token Request
	 */
	@Override
	public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
		ClientRegistration clientRegistration = authorizationCodeGrantRequest.getClientRegistration();
		String registrationId = authorizationCodeGrantRequest.getClientRegistration().getRegistrationId();
		RequestEntity<?> entity = defaultConverter.convert(authorizationCodeGrantRequest);
		if(registrationId.equalsIgnoreCase(OauthSite.GITHUB.getValue())) {
			return entity;
		}else if(registrationId.equalsIgnoreCase(OauthSite.QQ.getValue())) {
			
			MultiValueMap<String, String> queryParameters = this.buildQueryParameters(authorizationCodeGrantRequest);
			URI uri = UriComponentsBuilder.fromUriString(clientRegistration.getProviderDetails().getTokenUri()).queryParams(queryParameters)
			.build()
			.toUri();
			
			System.out.println("convert: " + uri);
			return new RequestEntity<>(HttpMethod.GET, uri);
		}
		
		 
//		ClientRegistration clientRegistration = authorizationCodeGrantRequest.getClientRegistration();
//
//		HttpHeaders headers = OAuth2AuthorizationGrantRequestEntityUtils.getTokenRequestHeaders(clientRegistration);
//		MultiValueMap<String, String> formParameters = this.buildFormParameters(authorizationCodeGrantRequest);
//		URI uri = UriComponentsBuilder.fromUriString(clientRegistration.getProviderDetails().getTokenUri())
//				.build()
//				.toUri();
//
//		return new RequestEntity<>(formParameters, headers, HttpMethod.POST, uri);
		
		return entity;
		
	}

	/**
	 * Returns a {@link MultiValueMap} of the form parameters used for the Access Token Request body.
	 *
	 * @param authorizationCodeGrantRequest the authorization code grant request
	 * @return a {@link MultiValueMap} of the form parameters used for the Access Token Request body
	 */
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
