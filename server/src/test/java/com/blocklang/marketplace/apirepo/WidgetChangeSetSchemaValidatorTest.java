package com.blocklang.marketplace.apirepo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.blocklang.marketplace.apirepo.widget.WidgetChangeSetSchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;

// schema validator 主要测试 schema 文件中的配置
public class WidgetChangeSetSchemaValidatorTest {

	@Test
	public void run_param_is_null() {
		assertThrows(IllegalArgumentException.class, () -> new WidgetChangeSetSchemaValidator().run(null));
	}
	
	@Test
	public void run_no_data() {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();
		Set<ValidationMessage> errors = new WidgetChangeSetSchemaValidator().run(rootNode);
		assertThat(errors).hasSize(3);
	}
}
