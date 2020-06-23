package com.blocklang.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;

/**
 * 基于 jackson 封装的 json 工具类
 * 
 * @author Zhengwei Jin
 *
 */
public abstract class JsonUtil {

	private static ObjectMapper mapper = new ObjectMapper();

	static {
		// 如果属性值为 null，则生成 json 字符串时移除此属性。
		mapper.setSerializationInclusion(Include.NON_NULL); 
		
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
		javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
		javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
		mapper.registerModule(javaTimeModule);
	}

	public static <T> String stringify(T value) throws JsonProcessingException {
		return mapper.writeValueAsString(value);
	}

	public static <T> T fromJsonObject(String jsonString, Class<T> clazz) throws JsonProcessingException {
		return mapper.readValue(jsonString, clazz);
	}

	public static <T> List<T> fromJsonArray(String jsonString, 
			Class<T> elementClass)
			throws JsonProcessingException {
		return mapper.readValue(jsonString, 
				mapper.getTypeFactory().constructCollectionType(List.class, elementClass));
	}
	
	public static JsonNode readTree(String jsonString) throws JsonProcessingException {
		return mapper.readTree(jsonString);
	}
	
	public static <T> T treeToValue(JsonNode node, Class<T> clazz) throws JsonProcessingException {
		return mapper.treeToValue(node, clazz);
	}
 	
	/**
	 * 校验 json 格式是否遵循 schema 定义。
	 * 
	 * 注意：因为 json schema 规范尚不支持自定义错误信息，详见 https://github.com/networknt/json-schema-validator/issues/286
	 * 当此功能实现后，再完善。
	 * 
	 * @param node json node
	 * @param schema schema 定义
	 * @return 返回错误信息
	 */
	public static Set<ValidationMessage> validate(JsonNode node, String schema) {
		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V201909);
		return factory.getSchema(schema).validate(node);
	}
}
