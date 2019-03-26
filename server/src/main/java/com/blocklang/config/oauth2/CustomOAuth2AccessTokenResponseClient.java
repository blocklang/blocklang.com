package com.blocklang.config.oauth2;

import java.util.Arrays;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.blocklang.config.oauth2.qq.QqAccessTokenResponseHttpMessageConverter;

public class CustomOAuth2AccessTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>{

	private DefaultAuthorizationCodeTokenResponseClient defaultAuthorizationCodeTokenResponseClient = 
			new DefaultAuthorizationCodeTokenResponseClient();
	
	private Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> requestEntityConverter =
			new CustomOAuth2AuthorizationCodeGrantRequestEntityConverter();
	
	public CustomOAuth2AccessTokenResponseClient() {
		RestTemplate restTemplate = new RestTemplate(Arrays.asList(
				new FormHttpMessageConverter(), 
				new OAuth2AccessTokenResponseHttpMessageConverter(),
				// 在此处添加自定制实现
				new QqAccessTokenResponseHttpMessageConverter()
		));
		restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
		
		defaultAuthorizationCodeTokenResponseClient.setRestOperations(restTemplate);
		defaultAuthorizationCodeTokenResponseClient.setRequestEntityConverter(requestEntityConverter);
	}
	
	@Override
	public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
		return defaultAuthorizationCodeTokenResponseClient.getTokenResponse(authorizationCodeGrantRequest);
	}

}
