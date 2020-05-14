package com.blocklang.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;

/**
 * 
 * @author Zhengwei Jin
 *
 */
public class JsonUtilTest {

	@Test
	public void stringify() throws JsonProcessingException {
		TestBean bean = new TestBean();
		bean.setText("a");
		bean.setIntNumber(1);
		bean.setDoubleNumber(2.1);
		bean.setDate(LocalDateTime.of(2020, 5, 14, 20, 55));
		
		String jsonString = JsonUtil.stringify(bean);
		assertThat(jsonString)
			.contains("\"text\":\"a\"")
			.contains("\"intNumber\":1")
			.contains("\"doubleNumber\":2.1")
			.contains("\"date\":\"2020-05-14T20:55:00\"");
	}
	
	@Test
	public void fromJsonObject() throws JsonProcessingException {
		// 注意，时间字段中没有包含秒
		String jsonString = "{\"text\":\"a\",\"intNumber\":1,\"doubleNumber\":2.1,\"date\":\"2020-05-14T20:55\"}";
		TestBean bean = JsonUtil.fromJsonObject(jsonString, TestBean.class);
		
		TestBean expected = new TestBean();
		expected.setText("a");
		expected.setIntNumber(1);
		expected.setDoubleNumber(2.1);
		expected.setDate(LocalDateTime.of(2020, 5, 14, 20, 55));
		assertThat(bean).usingRecursiveComparison().isEqualTo(expected);
	}
	
	@Test
	public void fromJsonArray() throws JsonProcessingException{
		String jsonString = "[{\"text\":\"a\",\"intNumber\":1,\"doubleNumber\":2.1,\"date\":\"2020-05-14T20:55:00\"}]";
		List<TestBean> beans = JsonUtil.fromJsonArray(jsonString, TestBean.class);
		
		TestBean expected = new TestBean();
		expected.setText("a");
		expected.setIntNumber(1);
		expected.setDoubleNumber(2.1);
		expected.setDate(LocalDateTime.of(2020, 5, 14, 20, 55));
		assertThat(beans).first().usingRecursiveComparison().isEqualTo(expected);
	}
	
	@Test
	public void readTree() throws JsonProcessingException {
		String jsonString = "{\"text\":\"a\",\"intNumber\":1,\"doubleNumber\":2.1,\"date\":\"2020-05-14T20:55:00\"}";
		JsonNode rootNode = JsonUtil.readTree(jsonString);
		assertThat(rootNode.get("text").asText()).isEqualTo("a");
	}
	
	@Test
	public void treeToValue() throws JsonProcessingException {
		String jsonString = "{\"text\":\"a\",\"intNumber\":1,\"doubleNumber\":2.1,\"date\":\"2020-05-14T20:55:00\"}";
		JsonNode rootNode = JsonUtil.readTree(jsonString);
		TestBean bean = JsonUtil.treeToValue(rootNode, TestBean.class);
		
		TestBean expected = new TestBean();
		expected.setText("a");
		expected.setIntNumber(1);
		expected.setDoubleNumber(2.1);
		expected.setDate(LocalDateTime.of(2020, 5, 14, 20, 55));
		assertThat(bean).usingRecursiveComparison().isEqualTo(expected);
	}
	
	@Test
	public void validate() throws JsonProcessingException {
		String jsonString = "{\"text\":\"a\"}";
		JsonNode rootNode = JsonUtil.readTree(jsonString);
		Set<ValidationMessage> errors = JsonUtil.validate(rootNode, "{\"properties\":{\"text\": {\"type\": \"string\"}}}");
		assertThat(errors).isEmpty();
	}
}
