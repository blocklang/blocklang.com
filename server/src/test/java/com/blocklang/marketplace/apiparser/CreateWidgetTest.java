package com.blocklang.marketplace.apiparser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.data.changelog.Widget;
import com.blocklang.marketplace.data.changelog.WidgetEvent;
import com.blocklang.marketplace.data.changelog.WidgetProperty;

public class CreateWidgetTest {

	@Test
	public void apply_create_one_widget_success() {
		WidgetOperatorContext context = new WidgetOperatorContext();
		
		Widget w = new Widget();
		w.setName("widget1");
		
		CreateWidget operator = new CreateWidget();
		assertThat(operator.apply(context, w)).isTrue();
		assertThat(context.getWidgets()).hasSize(1);
		
		assertThat(context.getWidgets().get(0).getCode()).isEqualTo("0001");
	}
	
	@Test
	public void apply_create_two_widget_success() {
		WidgetOperatorContext context = new WidgetOperatorContext();
		Widget w1 = new Widget();
		w1.setName("widget1");
		w1.setCode("0001");
		context.addWidget(w1);
		
		Widget w = new Widget();
		w.setName("widget2");
		
		CreateWidget operator = new CreateWidget();
		assertThat(operator.apply(context, w)).isTrue();
		assertThat(context.getWidgets()).hasSize(2);
		
		assertThat(context.getWidgets().get(0).getCode()).isEqualTo("0001");
		assertThat(context.getWidgets().get(1).getCode()).isEqualTo("0002");
	}
	
	@Test
	public void apply_contains_property_and_event_success() {
		WidgetOperatorContext context = new WidgetOperatorContext();
		
		Widget w = new Widget();
		w.setName("widget2");
		
		WidgetProperty prop = new WidgetProperty();
		prop.setName("prop1");
		w.setProperties(Collections.singletonList(prop));
		
		WidgetEvent event = new WidgetEvent();
		event.setName("event1");
		w.setEvents(Collections.singletonList(event));
		
		CreateWidget operator = new CreateWidget();
		assertThat(operator.apply(context, w)).isTrue();
		assertThat(context.getWidgets()).hasSize(1);
		
		Widget expected = context.getWidgets().get(0);
		assertThat(expected.getCode()).isEqualTo("0001");
		assertThat(expected.getProperties().get(0).getCode()).isEqualTo("0001");
		assertThat(expected.getEvents().get(0).getCode()).isEqualTo("0002");
	}
	
	@Test
	public void apply_widget_name_duplicated() {
		WidgetOperatorContext context = new WidgetOperatorContext();
		context.setLogger(mock(CliLogger.class));
		Widget w1 = new Widget();
		w1.setName("widget1");
		w1.setCode("0001");
		context.addWidget(w1);
		
		Widget w2 = new Widget();
		w2.setName("widget1");
		
		CreateWidget operator = new CreateWidget();
		assertThat(operator.apply(context, w2)).isFalse();
		assertThat(context.getWidgets()).hasSize(1);
	}
	
}
