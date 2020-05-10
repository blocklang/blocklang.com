package com.blocklang.marketplace.schema;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.blocklang.marketplace.data.ApiJson;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;

/**
 * 校验 api 仓库根目录下的 api.json 文件是否符合规范
 * 
 * @author Zhengwei Jin
 *
 */
public class JsonApiValidator {

	private static final String SCHEMA_FILE_NAME = "api_json_schema.json";
	
	/**
	 * 校验 apiJson 中的格式是否遵循 api.json 的 schema 定义。
	 * 
	 * 注意：因为 json schema 规范尚不支持自定义错误信息，详见 https://github.com/networknt/json-schema-validator/issues/286
	 * 当此功能实现后，再完善。
	 * 
	 * @param apiJson
	 * @return 返回错误信息
	 */
	public static List<ValidationMessage> execute(ApiJson apiJson) {
		Set<ValidationMessage> errors = getJsonSchema().validate(getJsonNode(apiJson));
		return new ArrayList<>(errors);
	}

	private static JsonNode getJsonNode(ApiJson apiJson) {
		ObjectMapper mapper = new ObjectMapper();
		// 如果属性值为 null，则生成 json 字符串时移除此属性。
		mapper.setSerializationInclusion(Include.NON_NULL); 
		return mapper.valueToTree(apiJson);
	}

	private static JsonSchema getJsonSchema() {
		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V201909);
		InputStream inputStream = JsonApiValidator.class.getResourceAsStream(SCHEMA_FILE_NAME);
		return factory.getSchema(inputStream);
	}

}
