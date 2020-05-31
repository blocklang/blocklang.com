package com.blocklang.marketplace.apiparser.webapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apiparser.OperatorContext;

public class CreateObjectTest {
	
	private OperatorContext<JsObjectData> context;
	private JsObjectOperator operator;
	
	@BeforeEach
	public void setup() {
		context = new OperatorContext<>();
		operator = new CreateObject();
	}

	@Test
	public void apply_create_one_object_success() {
		JsObjectData data = new JsObjectData();
		data.setName("obj1");
		
		operator.setData(data);
		
		assertThat(operator.apply(context)).isTrue();
		assertThat(context.getComponents())
			.hasSize(1)
			.first()
			.hasFieldOrPropertyWithValue("code", "0001");
	}
	
	@Test
	public void apply_create_two_object_success() {
		JsObjectData obj1 = new JsObjectData();
		obj1.setName("obj1");
		obj1.setCode("0001");
		context.addComponent(obj1);
		
		JsObjectData obj2 = new JsObjectData();
		obj2.setName("obj2");

		operator = new CreateObject();
		operator.setData(obj2);

		assertThat(operator.apply(context)).isTrue();
		assertThat(context.getComponents()).hasSize(2);

		assertThat(context.getComponents().get(0).getCode()).isEqualTo("0001");
		assertThat(context.getComponents().get(1).getCode()).isEqualTo("0002");
	}

	@Test
	public void apply_new_object_contains_function() {
		JsObjectData obj1 = new JsObjectData();
		obj1.setName("obj1");

		JsFunction func1 = new JsFunction();
		func1.setName("func1");
		
		Parameter param1 = new Parameter();
		param1.setName("param1");
		func1.getParameters().add(param1);

		obj1.setFunctions(Collections.singletonList(func1));

		operator.setData(obj1);

		assertThat(operator.apply(context)).isTrue();
		assertThat(context.getComponents()).hasSize(1);

		var expected = context.getComponents().get(0);
		assertThat(expected.getCode()).isEqualTo("0001");
		assertThat(expected.getFunctions().get(0).getCode()).isEqualTo("0001");
		assertThat(expected.getFunctions().get(0).getParameters().get(0).getCode()).isEqualTo("0001");
	}

	@Test
	public void apply_js_object_name_duplicated() {
		context.setLogger(mock(CliLogger.class));
		
		JsObjectData obj1 = new JsObjectData();
		obj1.setName("obj1");
		obj1.setCode("0001");
		context.addComponent(obj1);

		JsObjectData obj2 = new JsObjectData();
		obj2.setName("obj1");

		operator.setData(obj2);

		assertThat(operator.apply(context)).isFalse();
		assertThat(context.getComponents()).hasSize(1);
	}
	
}
