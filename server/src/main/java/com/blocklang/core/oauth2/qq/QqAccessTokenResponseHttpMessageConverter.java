package com.blocklang.core.oauth2.qq;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.util.StreamUtils;

public class QqAccessTokenResponseHttpMessageConverter extends OAuth2AccessTokenResponseHttpMessageConverter{

	private static final Logger logger = LoggerFactory.getLogger(QqAccessTokenResponseHttpMessageConverter.class);
	
	public QqAccessTokenResponseHttpMessageConverter() {
		setSupportedMediaTypes(Collections.singletonList(MediaType.TEXT_HTML));
	}

	@Override
	protected OAuth2AccessTokenResponse readInternal(Class<? extends OAuth2AccessTokenResponse> clazz, HttpInputMessage inputMessage)
			throws HttpMessageNotReadableException {
		try {
			String body = StreamUtils.copyToString(inputMessage.getBody(), Charset.defaultCharset());
			Map<String, String> tokenResponseParameters = new HashMap<String, String>();
			Pattern pattern = Pattern.compile("(?<key>\\w+)=(?<value>\\w+)");
			Matcher matcher = pattern.matcher(body);
			while (matcher.find()) {
				String key = matcher.group("key");
				String value = matcher.group("value");
				tokenResponseParameters.put(key, value);
			}
			// qq 的返回值中没有 tokenType
			tokenResponseParameters.put(OAuth2ParameterNames.TOKEN_TYPE, OAuth2AccessToken.TokenType.BEARER.getValue());
			return this.tokenResponseConverter.convert(tokenResponseParameters);
		} catch (Exception ex) {
			logger.error("An error occurred reading the OAuth 2.0 Access Token Response: ", ex);
			throw new HttpMessageNotReadableException("An error occurred reading the OAuth 2.0 Access Token Response: " +
					ex.getMessage(), ex, inputMessage);
		}
	}

}
