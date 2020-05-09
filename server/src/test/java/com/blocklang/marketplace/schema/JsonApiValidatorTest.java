package com.blocklang.marketplace.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.blocklang.marketplace.data.ApiJson;
import com.networknt.schema.ValidationMessage;

public class JsonApiValidatorTest {
	
	private ApiJson apiJson;
	
	// 为了方便测试，先创建一个能通过校验的 api json，然后在测试时将测试项设为无效
	@BeforeEach
	public void setUp() {
		apiJson = new ApiJson();
		apiJson.setName("your-api-repo");
		apiJson.setCategory("Widget");
		apiJson.setDisplayName("your api repo");
		apiJson.setDescription("description");
	}
	
	@DisplayName("name 的值不能为空")
	@Test
	public void validate_name_required() throws IOException {
		apiJson.setName(null);
		
		List<ValidationMessage> errors = JsonApiValidator.execute(apiJson);
		
		assertThat(errors).hasSize(1);
		ValidationMessage error = errors.get(0);
		assertThat(error.getType()).isEqualTo("required");
		assertThat(error.getMessage()).startsWith("$.name");
	}
	
	@DisplayName("name 值的长度不能超过60个字节(一个汉字占两个字节)")
	@Test
	public void validate_name_max_length_greater_than_60() throws IOException {
		String name = StringUtils.repeat("a", 61);
		apiJson.setName(name);
		
		List<ValidationMessage> errors = JsonApiValidator.execute(apiJson);
		
		assertThat(errors).hasSize(1);
		ValidationMessage error = errors.get(0);
		assertThat(error.getType()).isEqualTo("maxLength");
		assertThat(error.getMessage()).startsWith("$.name");
	}
	
	@DisplayName("name 的值只能为英文字母、数字、中划线(-)、下划线(_)、点(.)")
	@Test
	public void validate_name_match_naming_convertion() throws IOException {
		String name = "无效字符";
		apiJson.setName(name);
		
		List<ValidationMessage> errors = JsonApiValidator.execute(apiJson);
		
		assertThat(errors).hasSize(1);
		ValidationMessage error = errors.get(0);
		assertThat(error.getType()).isEqualTo("pattern");
		assertThat(error.getMessage()).startsWith("$.name");
	}
	
	@DisplayName("displayName 的值不能超过60个子节(一个汉字占两个字节)")
	@Test
	public void validate_display_max_length_greater_than_60() throws IOException {
		String displayName = StringUtils.repeat("a", 61);
		apiJson.setDisplayName(displayName);
		
		List<ValidationMessage> errors = JsonApiValidator.execute(apiJson);
		
		assertThat(errors).hasSize(1);
		ValidationMessage error = errors.get(0);
		assertThat(error.getType()).isEqualTo("maxLength");
		assertThat(error.getMessage()).startsWith("$.displayName");
	}
	
	@DisplayName("category 的值不能为空")
	@Test
	public void validate_category_required() throws IOException {
		apiJson.setCategory(null);
		
		List<ValidationMessage> errors = JsonApiValidator.execute(apiJson);
		
		assertThat(errors).hasSize(1);
		ValidationMessage error = errors.get(0);
		assertThat(error.getType()).isEqualTo("required");
		assertThat(error.getMessage()).startsWith("$.category");
	}
	
	@DisplayName("category 的值只能为 Widget，Service 或 WebApi")
	@Test
	public void validate_category_enum() throws IOException {
		String category = "invalid";
		apiJson.setCategory(category);
		
		List<ValidationMessage> errors = JsonApiValidator.execute(apiJson);
		
		assertThat(errors).hasSize(1);
		ValidationMessage error = errors.get(0);
		assertThat(error.getType()).isEqualTo("enum");
		assertThat(error.getMessage()).startsWith("$.category");
	}
	
	@DisplayName("description 值的长度不能超过500个字节(一个汉字占两个字节)")
	@Test
	public void validate_description_max_length_greater_than_500() throws IOException {
		String description = StringUtils.repeat("a", 501);
		apiJson.setDescription(description);
		
		List<ValidationMessage> errors = JsonApiValidator.execute(apiJson);
		
		assertThat(errors).hasSize(1);
		ValidationMessage error = errors.get(0);
		assertThat(error.getType()).isEqualTo("maxLength");
		assertThat(error.getMessage()).startsWith("$.description");
	}
	
	@Test
	public void validate_is_empty() throws IOException {
		apiJson.setName(null);
		apiJson.setDisplayName(null);
		apiJson.setDescription(null);
		apiJson.setCategory(null);
		List<ValidationMessage> errors = JsonApiValidator.execute(apiJson);
		
		assertThat(errors).hasSize(2);// 共有两个必填项
		assertThat(errors).allMatch(error -> error.getType().equals("required"));
	}

}
