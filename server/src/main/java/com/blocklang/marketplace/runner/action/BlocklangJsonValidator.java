package com.blocklang.marketplace.runner.action;

import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

import org.springframework.util.Assert;

import com.blocklang.marketplace.constant.RepoType;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;

public class BlocklangJsonValidator {

	private static final String SCHEMA_IDE_REPO = "ide_repo_config_schema.json";
	private static final String SCHEMA_API_REPO = "api_repo_config_schema.json";
	private static final String SCHEMA_PROD_REPO = "prod_repo_config_schema.json";
	
	private static JsonSchema ideJsonSchema;
	private static JsonSchema apiJsonSchema;
	private static JsonSchema prodJsonSchema;
		
	public static Set<ValidationMessage> run(JsonNode jsonNode) {
		Assert.notNull(jsonNode, "jsonNode 的值不能为 null");
		String repo = jsonNode.get("repo").asText();
		JsonSchema jsonSchema = getJsonSchema(repo);
		if(jsonSchema == null) {
			return Collections.emptySet();
		}
		
		return jsonSchema.validate(jsonNode);
	}
	
	private static JsonSchema getJsonSchema(String repo) {
		if(RepoType.IDE.getValue().equals(repo)) {
			getIdeJsonSchema();
			return ideJsonSchema;
		} else if(RepoType.API.getValue().equals(repo)) {
			getApiJsonSchema();
			return apiJsonSchema;
		} else if(RepoType.PROD.getValue().equals(repo)) {
			getProdJsonSchema();
			return prodJsonSchema;
		}
		return null;
	}

	private static void getApiJsonSchema() {
		if(apiJsonSchema == null) {
			JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V201909);
			InputStream inputStream = BlocklangJsonValidator.class.getResourceAsStream(SCHEMA_API_REPO);
			apiJsonSchema = factory.getSchema(inputStream);
		}
	}
	
	private static void getIdeJsonSchema() {
		if(ideJsonSchema == null) {
			JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V201909);
			InputStream inputStream = BlocklangJsonValidator.class.getResourceAsStream(SCHEMA_IDE_REPO);
			ideJsonSchema = factory.getSchema(inputStream);
		}
	}
	
	private static void getProdJsonSchema() {
		if(prodJsonSchema == null) {
			JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V201909);
			InputStream inputStream = BlocklangJsonValidator.class.getResourceAsStream(SCHEMA_PROD_REPO);
			prodJsonSchema = factory.getSchema(inputStream);
		}
	}
	
}
