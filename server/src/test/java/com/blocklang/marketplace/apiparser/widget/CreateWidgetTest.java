package com.blocklang.marketplace.apiparser.widget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apiparser.OperatorContext;

public class CreateWidgetTest {

	@Test
	public void apply_create_one_widget_success() {
		OperatorContext<WidgetData> context = new OperatorContext<WidgetData>();
		
		WidgetData w = new WidgetData();
		w.setName("widget1");
		
		WidgetOperator operator = new CreateWidget();
		operator.setData(w);
		assertThat(operator.apply(context)).isTrue();
		assertThat(context.getComponents()).hasSize(1);
		assertThat(context.getComponents().get(0).getCode()).isEqualTo("0001");
	}
	
	@Test
	public void apply_create_two_widget_success() {
		OperatorContext<WidgetData> context = new OperatorContext<WidgetData>();
		WidgetData w1 = new WidgetData();
		w1.setName("widget1");
		w1.setCode("0001");
		context.addComponent(w1);
		
		WidgetData w = new WidgetData();
		w.setName("widget2");
		
		WidgetOperator operator = new CreateWidget();
		operator.setData(w);
		assertThat(operator.apply(context)).isTrue();
		assertThat(context.getComponents()).hasSize(2);
		
		assertThat(context.getComponents().get(0).getCode()).isEqualTo("0001");
		assertThat(context.getComponents().get(1).getCode()).isEqualTo("0002");
	}
	
	@Test
	public void apply_contains_property_and_event_success() {
		OperatorContext<WidgetData> context = new OperatorContext<WidgetData>();
		
		WidgetData w = new WidgetData();
		w.setName("widget2");
		
		WidgetProperty prop = new WidgetProperty();
		prop.setName("prop1");
		w.setProperties(Collections.singletonList(prop));
		
		WidgetEvent event = new WidgetEvent();
		event.setName("event1");
		w.setEvents(Collections.singletonList(event));
		
		WidgetOperator operator = new CreateWidget();
		operator.setData(w);
		assertThat(operator.apply(context)).isTrue();
		assertThat(context.getComponents()).hasSize(1);
		
		WidgetData expected = context.getComponents().get(0);
		assertThat(expected.getCode()).isEqualTo("0001");
		assertThat(expected.getProperties().get(0).getCode()).isEqualTo("0001");
		assertThat(expected.getEvents().get(0).getCode()).isEqualTo("0002");
	}
	
	@Test
	public void apply_widget_name_duplicated() {
		OperatorContext<WidgetData> context = new OperatorContext<WidgetData>();
		context.setLogger(mock(CliLogger.class));
		WidgetData w1 = new WidgetData();
		w1.setName("widget1");
		w1.setCode("0001");
		context.addComponent(w1);
		
		WidgetData w2 = new WidgetData();
		w2.setName("widget1");
		
		WidgetOperator operator = new CreateWidget();
		operator.setData(w2);
		assertThat(operator.apply(context)).isFalse();
		assertThat(context.getComponents()).hasSize(1);
	}
	
}
