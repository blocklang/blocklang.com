package com.blocklang.marketplace.apiparser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;

public class ServiceChangeSetSchemaValidatorTest {

	@Test
	public void run_param_is_null() {
		assertThrows(IllegalArgumentException.class, () -> new ServiceChangeSetSchemaValidator().run(null));
	}
	
	@Test
	public void run_no_data() {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();
		Set<ValidationMessage> errors = new ServiceChangeSetSchemaValidator().run(rootNode);
		assertThat(errors).hasSize(3);
	}
	
}
