package com.blocklang.marketplace.apirepo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.blocklang.marketplace.apirepo.webapi.JsObjectChangeSetSchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.ValidationMessage;

public class WebApiChangeSetSchemaValidatorTest {
	@Test
	public void run_param_is_null() {
		assertThrows(IllegalArgumentException.class, () -> new JsObjectChangeSetSchemaValidator().run(null));
	}
	
	@Test
	public void run_no_data() {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();
		Set<ValidationMessage> errors = new JsObjectChangeSetSchemaValidator().run(rootNode);
		assertThat(errors).hasSize(3);
	}
	
	@Test
	public void run_create_object_name_required() {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		
		rootNode.put("id", "id1");
		rootNode.put("author", "author1");
		
		ArrayNode changeNodes = mapper.createArrayNode();
		ObjectNode change1 = mapper.createObjectNode();
		ObjectNode createObject = mapper.createObjectNode();

		createObject.put("description", "");
		change1.set("createObject", createObject);
		changeNodes.add(change1);
		
		rootNode.set("changes", changeNodes);
		
		Set<ValidationMessage> errors = new JsObjectChangeSetSchemaValidator().run(rootNode);
		// FIXME: 应该只提示缺少 name，但却也提示缺少 addFunction，这可能是 json validator 库的 bug
		assertThat(errors).hasSize(2);
	}
	
}
