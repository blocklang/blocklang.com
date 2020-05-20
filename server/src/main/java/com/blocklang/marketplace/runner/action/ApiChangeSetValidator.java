package com.blocklang.marketplace.runner.action;

import java.io.InputStream;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;

public abstract class ApiChangeSetValidator {

	private static final String SCHEMA_FILE_NAME = "api_change_set_schema.json";
	private static JsonSchema jsonSchema;
		
	private static void getJsonSchema() {
		if(jsonSchema == null) {
			JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V201909);
			InputStream inputStream = ApiChangeSetValidator.class.getResourceAsStream(SCHEMA_FILE_NAME);
			jsonSchema = factory.getSchema(inputStream);
		}
	}
	
	public static Set<ValidationMessage> run(JsonNode rootNode) {
		getJsonSchema();
		return jsonSchema.validate(rootNode);
	}
	
}
