package com.blocklang.config.oauth2.qq;

import java.io.IOException;
import java.nio.charset.Charset;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class QqOpenIdHttpMessageConverter extends AbstractHttpMessageConverter<OAuth2OpenIdResponse>{

	private ObjectMapper objectMapper;
	public QqOpenIdHttpMessageConverter() {
		objectMapper = Jackson2ObjectMapperBuilder.json().build();
	}
	
	@Override
	protected boolean supports(Class<?> clazz) {
		return OAuth2OpenIdResponse.class.isAssignableFrom(clazz);
	}

	@Override
	protected OAuth2OpenIdResponse readInternal(Class<? extends OAuth2OpenIdResponse> clazz,
			HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		String body = StreamUtils.copyToString(inputMessage.getBody(), Charset.defaultCharset());
		System.out.println("----response body: " + body);
		String jsonString = body.substring(10, body.length() - 2);
		try {
			return objectMapper.readValue(jsonString, OAuth2OpenIdResponse.class);
		} catch (Exception ex) {
				throw new HttpMessageNotReadableException("An error occurred reading the QQ open id Response: " +
						ex.getMessage(), ex, inputMessage);
		}
	}

	@Override
	protected void writeInternal(OAuth2OpenIdResponse t, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		// Do nothing
	}



}
