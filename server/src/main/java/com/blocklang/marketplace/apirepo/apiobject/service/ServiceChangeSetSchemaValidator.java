package com.blocklang.marketplace.apirepo.apiobject.service;

import java.io.InputStream;
import java.util.Set;

import org.springframework.util.Assert;

import com.blocklang.core.runner.common.JsonSchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;

public class ServiceChangeSetSchemaValidator implements JsonSchemaValidator {

	private static final String SCHEMA_FILE_NAME = "api_change_set_schema.json";
	
	private JsonSchema jsonSchema;
	
	public ServiceChangeSetSchemaValidator() {
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
