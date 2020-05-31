package com.blocklang.marketplace.apiparser.webapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apiparser.OperatorContext;

public class AddFunctionTest {

	private OperatorContext<JsObjectData> context;
	private JsObjectOperator operator;
	
	@BeforeEach
	public void setup() {
		context = new OperatorContext<>();
		context.setLogger(mock(CliLogger.class));
		
		operator = new AddFunction();
	}
	
	@Test
	public void apply_first_func_success() {
		JsObjectData obj1 = new JsObjectData();
		obj1.setName("obj1");
		obj1.setCode("0001");
		context.addComponent(obj1);
		
		JsFunction func1 = new JsFunction();
		func1.setName("func1");
		
		Parameter param1 = new Parameter();
		param1.setName("param1");
		func1.getParameters().add(param1);
		
		AddFunctionData funcData = new AddFunctionData();
		funcData.setFunctions(Collections.singletonList(func1));
		
		operator.setData(funcData);
		assertThat(operator.apply(context)).isTrue();
		assertThat(context.getComponents().get(0).getFunctions())
			.hasSize(1)
			.first()
			.hasFieldOrPropertyWithValue("code", "0001");
		assertThat(context.getComponents().get(0).getFunctions().get(0).getParameters())
			.hasSize(1)
			.first()
			.hasFieldOrPropertyWithValue("code", "0001");
	}
	
	@Test
	public void apply_second_func_success() {
		JsObjectData obj1 = new JsObjectData();
		obj1.setName("obj1");
		obj1.setCode("0001");
		
		JsFunction func1 = new JsFunction();
		func1.setName("func1");
		func1.setCode("0001");
		obj1.getFunctions().add(func1);
		
		context.addComponent(obj1);
		
		JsFunction func2 = new JsFunction();
		func2.setName("func2");
		
		AddFunctionData funcData = new AddFunctionData();
		funcData.setFunctions(Collections.singletonList(func2));
		operator.setData(funcData);
		
		assertThat(operator.apply(context)).isTrue();
		assertThat(context.getComponents().get(0).getFunctions())
			.hasSize(2)
			.last()
			.hasFieldOrPropertyWithValue("code", "0002")
			.hasFieldOrPropertyWithValue("name", "func2");
	}
	
	@Test
	public void apply_js_object_not_exist() {
		JsFunction func1 = new JsFunction();
		func1.setName("func1");
		
		AddFunctionData data = new AddFunctionData();
		data.setFunctions(Collections.singletonList(func1));
		
		operator.setData(data);
		
		assertThat(operator.apply(context)).isFalse();
	}
	
	@Test
	public void apply_select_object_when_add_object() {
		JsObjectData obj1 = new JsObjectData();
		obj1.setName("obj1");
		obj1.setCode("0001");
		context.addComponent(obj1);
		
		assertThat(context.getSelectedComponent())
			.usingRecursiveComparison().isEqualTo(obj1);
	}
	
	// 先约定一个对象中的函数名必须唯一，暂不支持重载
	@Test
	public void apply_func_name_exists() {
		JsObjectData obj1 = new JsObjectData();
		obj1.setName("obj1");
		obj1.setCode("0001");
		
		JsFunction func1 = new JsFunction();
		func1.setName("func1");
		func1.setCode("0001");
		
		obj1.getFunctions().add(func1);
		
		context.addComponent(obj1);
		
		JsFunction func2 = new JsFunction();
		func2.setName("func1");
		
		AddFunctionData funcData = new AddFunctionData();
		funcData.setFunctions(Collections.singletonList(func2));
		operator.setData(funcData);
		
		assertThat(operator.apply(context)).isFalse();
	}
	
}
