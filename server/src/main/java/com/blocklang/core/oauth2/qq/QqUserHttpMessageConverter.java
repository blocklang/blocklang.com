package com.blocklang.core.oauth2.qq;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

// 注意，必须实现 GenericHttpMessageConverter，不然匹配不到
public class QqUserHttpMessageConverter extends AbstractGenericHttpMessageConverter<Map<String, Object>>{
	private String openId;
	private ObjectMapper objectMapper;
	public QqUserHttpMessageConverter(String openId) {
		super(MediaType.valueOf("text/html;charset=utf-8"));
		this.openId = openId;
		this.objectMapper = Jackson2ObjectMapperBuilder.json().build();
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return Map.class.isAssignableFrom(clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Object> readInternal(Class<? extends Map<String, Object>> clazz,
			HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		String body = StreamUtils.copyToString(inputMessage.getBody(), Charset.defaultCharset());

		try {
			Map<String, Object> result = objectMapper.readValue(body, Map.class);
			result.put(QqOAuth2ParameterNames.OPEN_ID, this.openId);
			return result;
		} catch (Exception ex) {
				throw new HttpMessageNotReadableException("An error occurred reading the QQ user Response: " +
						ex.getMessage(), ex, inputMessage);
		}
	}

	@Override
	public Map<String, Object> read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		return readInternal(null, inputMessage);
	}
	
	@Override
	protected void writeInternal(Map<String, Object> t, Type type, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		// Do nothing
	}
}
