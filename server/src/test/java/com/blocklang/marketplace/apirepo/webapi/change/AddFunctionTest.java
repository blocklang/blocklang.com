package com.blocklang.marketplace.apirepo.webapi.change;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.ApiObjectContext;
import com.blocklang.marketplace.apirepo.Change;
import com.blocklang.marketplace.apirepo.webapi.JsObjectContext;
import com.blocklang.marketplace.apirepo.webapi.data.AddFunctionData;
import com.blocklang.marketplace.apirepo.webapi.data.JsFunction;
import com.blocklang.marketplace.apirepo.webapi.data.JsObjectData;
import com.blocklang.marketplace.apirepo.webapi.data.Parameter;
import com.blocklang.marketplace.data.MarketplaceStore;

public class AddFunctionTest {

	private ApiObjectContext context;
	private Change change;
	
	@BeforeEach
	public void setup() {
		var store = mock(MarketplaceStore.class);
		var logger = mock(CliLogger.class);
		
		context = new JsObjectContext(store, logger);
		
		change = new AddFunction();
	}
	
	@Test
	public void apply_first_func_success() {
		JsObjectData obj1 = new JsObjectData();
		obj1.setName("obj1");
		obj1.setCode("0001");
		context.addApiObject(obj1);
		
		JsFunction func1 = new JsFunction();
		func1.setName("func1");
		
		Parameter param1 = new Parameter();
		param1.setName("param1");
		func1.getParameters().add(param1);
		
		AddFunctionData funcData = new AddFunctionData();
		funcData.setFunctions(Collections.singletonList(func1));
		
		change.setData(funcData);
		assertThat(change.apply(context)).isTrue();
		
		JsObjectData expected = (JsObjectData) context.getApiObjects().get(0);
		assertThat(expected.getFunctions())
			.hasSize(1)
			.first()
			.hasFieldOrPropertyWithValue("code", "0001");
		assertThat(expected.getFunctions().get(0).getParameters())
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
		
		context.addApiObject(obj1);
		
		JsFunction func2 = new JsFunction();
		func2.setName("func2");
		
		AddFunctionData funcData = new AddFunctionData();
		funcData.setFunctions(Collections.singletonList(func2));
		change.setData(funcData);
		
		assertThat(change.apply(context)).isTrue();
		JsObjectData expected = (JsObjectData) context.getApiObjects().get(0);
		assertThat(expected.getFunctions())
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
		
		change.setData(data);
		
		assertThat(change.apply(context)).isFalse();
	}
	
	@Test
	public void apply_select_object_when_add_object() {
		JsObjectData obj1 = new JsObjectData();
		obj1.setName("obj1");
		obj1.setCode("0001");
		context.addApiObject(obj1);
		
		assertThat(context.getSelectedApiObject())
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
		
		context.addApiObject(obj1);
		
		JsFunction func2 = new JsFunction();
		func2.setName("func1");
		
		AddFunctionData funcData = new AddFunctionData();
		funcData.setFunctions(Collections.singletonList(func2));
		change.setData(funcData);
		
		assertThat(change.apply(context)).isFalse();
	}
	
}
