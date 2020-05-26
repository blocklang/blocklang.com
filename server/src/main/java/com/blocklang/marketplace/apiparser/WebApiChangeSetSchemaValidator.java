package com.blocklang.marketplace.apiparser;

import java.io.InputStream;
import java.util.Set;

import org.springframework.util.Assert;

import com.blocklang.core.runner.common.JsonSchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.SpecVersion.VersionFlag;

public class WebApiChangeSetSchemaValidator implements JsonSchemaValidator {

	private static final String SCHEMA_FILE_NAME = "webapi_api_change_set_schema.json";
	
	private JsonSchema jsonSchema;
	
	public WebApiChangeSetSchemaValidator() {
		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V201909);
		InputStream inputStream = getClass().getResourceAsStream(SCHEMA_FILE_NAME);
		jsonSchema = factory.getSchema(inputStream);
	}
	
	@Override
	public Set<ValidationMessage> run(JsonNode jsonNode) {
		Assert.notNull(jsonNode, "传入的 jsonNode 不能为 null");
		return jsonSchema.validate(jsonNode);
	}

}
