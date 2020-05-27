package com.blocklang.core.runner.common;

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;

public interface JsonSchemaValidator {
	Set<ValidationMessage> run(JsonNode rootNode);
}
