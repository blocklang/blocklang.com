package com.blocklang.config.oauth2.qq;

import java.net.URI;
import java.util.Collections;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

public class QqUserRequestEntityConverter implements Converter<OAuth2UserRequest, RequestEntity<?>>{

	@Override
	public RequestEntity<?> convert(OAuth2UserRequest userRequest) {
		ClientRegistration clientRegistration = userRequest.getClientRegistration();
		
		HttpMethod httpMethod = HttpMethod.GET;
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		URI uri = UriComponentsBuilder.fromUriString(clientRegistration.getProviderDetails().getUserInfoEndpoint().getUri())
				.queryParams(this.buildQueryParameters(userRequest))
				.build()
				.toUri();
		System.out.println("----user uri: " + uri.toString());
		return new RequestEntity<>(headers, httpMethod, uri);
	}
	
	private MultiValueMap<String, String> buildQueryParameters(OAuth2UserRequest userRequest) {
		MultiValueMap<String, String> queryParameters = new LinkedMultiValueMap<>();
		// https://graph.qq.com/user/get_user_info?access_token=YOUR_ACCESS_TOKEN&oauth_consumer_key=YOUR_APP_ID&openid=YOUR_OPENID
		queryParameters.add(OAuth2ParameterNames.ACCESS_TOKEN, userRequest.getAccessToken().getTokenValue());
		queryParameters.add(QqOAuth2ParameterNames.OAUTH_CONSUMER_KEY, userRequest.getClientRegistration().getClientId());
		queryParameters.add(QqOAuth2ParameterNames.OPEN_ID, userRequest.getAdditionalParameters().get(QqOAuth2ParameterNames.OPEN_ID).toString());
		return queryParameters;
	}

}
