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
import com.blocklang.marketplace.apirepo.webapi.data.JsFunction;
import com.blocklang.marketplace.apirepo.webapi.data.JsObjectData;
import com.blocklang.marketplace.apirepo.webapi.data.Parameter;
import com.blocklang.marketplace.data.MarketplaceStore;

public class CreateObjectTest {
	
	private ApiObjectContext context;
	private Change change;
	
	@BeforeEach
	public void setup() {
		var store = mock(MarketplaceStore.class);
		var logger = mock(CliLogger.class);
		
		context = new JsObjectContext(store, logger);
		change = new CreateObject();
	}

	@Test
	public void apply_create_one_object_success() {
		JsObjectData data = new JsObjectData();
		data.setName("obj1");
		
		change.setData(data);
		
		String apiObjectId = "1";
		context.setApiObjectId(apiObjectId);
		
		assertThat(change.apply(context)).isTrue();
		assertThat(context.getApiObjects())
			.hasSize(1)
			.first()
			.hasFieldOrPropertyWithValue("code", "0001")
			.hasFieldOrPropertyWithValue("id", apiObjectId);
	}
	
	@Test
	public void apply_create_two_object_success() {
		JsObjectData obj1 = new JsObjectData();
		obj1.setName("obj1");
		obj1.setCode("0001");
		context.addApiObject(obj1);
		
		JsObjectData obj2 = new JsObjectData();
		obj2.setName("obj2");

		change = new CreateObject();
		change.setData(obj2);

		assertThat(change.apply(context)).isTrue();
		assertThat(context.getApiObjects()).hasSize(2);

		assertThat(context.getApiObjects().get(0).getCode()).isEqualTo("0001");
		assertThat(context.getApiObjects().get(1).getCode()).isEqualTo("0002");
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

		change.setData(obj1);

		assertThat(change.apply(context)).isTrue();
		assertThat(context.getApiObjects()).hasSize(1);

		JsObjectData expected = (JsObjectData) context.getApiObjects().get(0);
		assertThat(expected.getCode()).isEqualTo("0001");
		assertThat(expected.getFunctions().get(0).getCode()).isEqualTo("0001");
		assertThat(expected.getFunctions().get(0).getParameters().get(0).getCode()).isEqualTo("0001");
	}

	@Test
	public void apply_js_object_name_duplicated() {
		JsObjectData obj1 = new JsObjectData();
		obj1.setName("obj1");
		obj1.setCode("0001");
		context.addApiObject(obj1);

		JsObjectData obj2 = new JsObjectData();
		obj2.setName("obj1");

		change.setData(obj2);

		assertThat(change.apply(context)).isFalse();
		assertThat(context.getApiObjects()).hasSize(1);
	}
	
}
