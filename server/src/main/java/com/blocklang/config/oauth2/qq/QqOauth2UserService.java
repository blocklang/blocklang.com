package com.blocklang.config.oauth2.qq;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

/**
 * 因为 QQ 互联中是分两个请求，一是根据 accessToken 获取 openId，然后根据 openId 获取 userInfo。
 * 而其他 OAuth2 实现都是一步完成，这里将这两步都卸载这个类中。
 * 
 * @author Zhengwei Jin
 *
 */
public class QqOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
	
	private static final String MISSING_OPEN_ID_URI_ERROR_CODE = "missing_open_id_uri";

	// 因为没有找到配置此 url 的地方，就先硬编码
	private static final String GET_OPEN_ID_URL = "https://graph.qq.com/oauth2.0/me";
	private RestOperations openIdRestOperations;
	private Converter<OAuth2UserRequest, RequestEntity<?>> openIdRequestEntityConverter = new QqOpenIdRequestEntityConverter(GET_OPEN_ID_URL);
	private static final ParameterizedTypeReference<OAuth2OpenIdResponse> PARAMETERIZED_RESPONSE_TYPE =
			new ParameterizedTypeReference<OAuth2OpenIdResponse>() {};
	
	public QqOauth2UserService() {
		QqOpenIdHttpMessageConverter converter = new QqOpenIdHttpMessageConverter();
		converter.setSupportedMediaTypes(Collections.singletonList(MediaType.TEXT_HTML));
		
		RestTemplate restTemplate = new RestTemplate(Arrays.asList(converter));
		restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
		this.openIdRestOperations = restTemplate;
	}

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		Assert.notNull(userRequest, "userRequest cannot be null");
		// 获取 openId
		System.out.println("load open id start");
		String openId;
		try {
			openId = this.loadOpenId(userRequest);
		}catch(OAuth2AuthenticationException e) {
			
			e.printStackTrace();
			
			throw e;
		}
		// userRequest 中的 additionalParameters 是不能直接修改的，
		// 因此这里重新创建一个 Oauth2UserRequest 对象，并在其中加入 openId
		Map<String, Object> additionalParameters = new HashMap<String, Object>();
		additionalParameters.put(QqOAuth2ParameterNames.OPEN_ID, openId);
		OAuth2UserRequest newUserRequest = new OAuth2UserRequest(userRequest.getClientRegistration(), userRequest.getAccessToken(), additionalParameters);
		return this.loadQqUser(newUserRequest);
	}

	private String loadOpenId(OAuth2UserRequest userRequest) {
		RequestEntity<?> request = this.openIdRequestEntityConverter.convert(userRequest);
		ResponseEntity<OAuth2OpenIdResponse> response;
		try {
			response = this.openIdRestOperations.exchange(request, PARAMETERIZED_RESPONSE_TYPE);
		}catch (OAuth2AuthorizationException ex) {
			OAuth2Error oauth2Error = ex.getError();
			StringBuilder errorDetails = new StringBuilder();
			errorDetails.append("Error details: [");
			errorDetails.append("OpenId Uri: ").append(GET_OPEN_ID_URL);
			errorDetails.append(", Error Code: ").append(oauth2Error.getErrorCode());
			if (oauth2Error.getDescription() != null) {
				errorDetails.append(", Error Description: ").append(oauth2Error.getDescription());
			}
			errorDetails.append("]");
			oauth2Error = new OAuth2Error(MISSING_OPEN_ID_URI_ERROR_CODE,
					"An error occurred while attempting to retrieve the openId Resource: " + errorDetails.toString(), null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
		} catch (RestClientException ex) {
			OAuth2Error oauth2Error = new OAuth2Error(MISSING_OPEN_ID_URI_ERROR_CODE,
					"An error occurred while attempting to retrieve the openId Resource: " + ex.getMessage(), null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
		}
		
		OAuth2OpenIdResponse openIdResponse = response.getBody();
		System.out.println("---- openIdResponse:" + openIdResponse.getOpenid());
		
		return openIdResponse.getOpenid();
	}

	private OAuth2User loadQqUser(OAuth2UserRequest userRequest) {
		DefaultOAuth2UserService service = new DefaultOAuth2UserService();
		
		QqUserHttpMessageConverter converter = new QqUserHttpMessageConverter(userRequest.getAdditionalParameters().get(QqOAuth2ParameterNames.OPEN_ID).toString());
		RestTemplate restTemplate = new RestTemplate(Arrays.asList(converter));
		restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
		service.setRestOperations(restTemplate);
		service.setRequestEntityConverter(new QqUserRequestEntityConverter());
		System.out.println("-----qq load user, AdditionalParameters: " + userRequest.getAdditionalParameters());
		try {
			return service.loadUser(userRequest);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
}
