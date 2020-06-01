package com.blocklang.marketplace.apiparser.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apiparser.OperatorContext;

public class CreateApiTest {
	
	private OperatorContext<ServiceData> context;
	private ServiceOperator operator;
	
	@BeforeEach
	public void setup() {
		context = new OperatorContext<>();
		operator = new CreateApi();
	}

	@Test
	public void apply_create_one_service_success() {
		ServiceData data = new ServiceData();
		data.setName("list_users");
		data.setUrl("/users");
		
		operator.setData(data);
		
		assertThat(operator.apply(context)).isTrue();
		assertThat(context.getComponents())
			.hasSize(1)
			.first()
			.hasFieldOrPropertyWithValue("code", "0001");
	}
	
	@Test
	public void apply_create_two_service_success() {
		ServiceData service1 = new ServiceData();
		service1.setName("service1");
		service1.setCode("0001");
		context.addComponent(service1);
		
		ServiceData service2 = new ServiceData();
		service2.setName("service2");
		operator.setData(service2);
		
		assertThat(operator.apply(context)).isTrue();
		assertThat(context.getComponents()).hasSize(2);
		
		assertThat(context.getComponents().get(0).getCode()).isEqualTo("0001");
		assertThat(context.getComponents().get(1).getCode()).isEqualTo("0002");
	}
	
	@Test
	public void apply_service_name_duplicated() {
		context.setLogger(mock(CliLogger.class));
		
		ServiceData service1 = new ServiceData();
		service1.setCode("0001");
		service1.setName("service1");
		context.addComponent(service1);
		
		ServiceData service2 = new ServiceData();
		service2.setName("service1");
		
		operator.setData(service2);
		
		assertThat(operator.apply(context)).isFalse();
		assertThat(context.getComponents()).hasSize(1);
	}
}
