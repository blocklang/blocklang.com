package com.blocklang.marketplace.apirepo.apiobject.service.change;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.Change;
import com.blocklang.marketplace.apirepo.apiobject.ApiObjectContext;
import com.blocklang.marketplace.apirepo.apiobject.service.ServiceContext;
import com.blocklang.marketplace.apirepo.apiobject.service.change.CreateApi;
import com.blocklang.marketplace.apirepo.apiobject.service.data.ServiceData;
import com.blocklang.marketplace.data.MarketplaceStore;

public class CreateApiTest {
	
	private ApiObjectContext context;
	private Change change;
	
	@BeforeEach
	public void setup() {
		var store = mock(MarketplaceStore.class);
		var logger = mock(CliLogger.class);
		
		context = new ServiceContext(store, logger);
		change = new CreateApi();
	}

	@Test
	public void apply_create_one_service_success() {
		ServiceData data = new ServiceData();
		data.setName("list_users");
		data.setUrl("/users");
		
		var apiObjectId = "1";
		context.setObjectId(apiObjectId);
		change.setData(data);
		
		assertThat(change.apply(context)).isTrue();
		
		assertThat(context.getChangedObjects())
			.hasSize(1)
			.first()
			.hasFieldOrPropertyWithValue("code", "0001")
			.hasFieldOrPropertyWithValue("id", apiObjectId);
	}
	
	@Test
	public void apply_create_two_service_success() {
		ServiceData service1 = new ServiceData();
		service1.setName("service1");
		service1.setCode("0001");
		context.addObject(service1);
		
		ServiceData service2 = new ServiceData();
		service2.setName("service2");
		change.setData(service2);
		
		assertThat(change.apply(context)).isTrue();
		assertThat(context.getChangedObjects()).hasSize(2);
		
		assertThat(context.getChangedObjects().get(0).getCode()).isEqualTo("0001");
		assertThat(context.getChangedObjects().get(1).getCode()).isEqualTo("0002");
	}
	
	@Test
	public void apply_service_name_duplicated() {
		ServiceData service1 = new ServiceData();
		service1.setCode("0001");
		service1.setName("service1");
		context.addObject(service1);
		
		ServiceData service2 = new ServiceData();
		service2.setName("service1");
		
		change.setData(service2);
		
		assertThat(change.apply(context)).isFalse();
		assertThat(context.getChangedObjects()).hasSize(1);
	}
}
