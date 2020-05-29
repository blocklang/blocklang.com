package com.blocklang.marketplace.apiparser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.data.changelog.Widget;
import com.blocklang.marketplace.data.changelog.WidgetEvent;
import com.blocklang.marketplace.data.changelog.WidgetProperty;

public class AddWidgetEventTest {

	@Test
	public void apply_first_event_success() {
		WidgetOperatorContext context = new WidgetOperatorContext();
		context.setLogger(mock(CliLogger.class));
		
		String widgetName = "widget1";
		Widget widget = new Widget();
		widget.setName(widgetName);
		widget.setCode("0001");
		
		context.addWidget(widget);
		context.selectWidget(widgetName);
		
		WidgetEvent event1 = new WidgetEvent();
		event1.setName("event1");
		
		AddWidgetEvent operator = new AddWidgetEvent();
		assertThat(operator.apply(context, Collections.singletonList(event1))).isTrue();
		
		assertThat(context.getWidgets().get(0).getEvents())
			.hasSize(1)
			.first()
			.hasFieldOrPropertyWithValue("code", "0001");
	}
	
	@Test
	public void apply_second_event_success() {
		WidgetOperatorContext context = new WidgetOperatorContext();
		context.setLogger(mock(CliLogger.class));
		
		String widgetName = "widget1";
		Widget widget = new Widget();
		widget.setName(widgetName);
		widget.setCode("0001");
		
		WidgetProperty existProp = new WidgetProperty();
		existProp.setName("prop1");
		existProp.setCode("0001");
		
		widget.getProperties().add(existProp);
		
		WidgetEvent existEvent = new WidgetEvent();
		existEvent.setName("event1");
		existEvent.setCode("0002");
		
		widget.getEvents().add(existEvent);
		
		context.addWidget(widget);
		context.selectWidget(widgetName);
		
		WidgetEvent addEvent = new WidgetEvent();
		addEvent.setName("event2");
		
		AddWidgetEvent operator = new AddWidgetEvent();
		assertThat(operator.apply(context, Collections.singletonList(addEvent))).isTrue();
		
		assertThat(context.getWidgets().get(0).getEvents())
			.hasSize(2)
			.last()
			.hasFieldOrPropertyWithValue("code", "0003")
			.hasFieldOrPropertyWithValue("name", "event2");
	}
	
	@Test
	public void apply_widget_not_exist() {
		WidgetOperatorContext context = new WidgetOperatorContext();
		context.setLogger(mock(CliLogger.class));
		
		WidgetEvent event1 = new WidgetEvent();
		event1.setName("event1");
		
		AddWidgetEvent operator = new AddWidgetEvent();
		assertThat(operator.apply(context, Collections.singletonList(event1))).isFalse();
	}
	
	@Test
	public void apply_widget_exist_but_not_select() {
		WidgetOperatorContext context = new WidgetOperatorContext();
		context.setLogger(mock(CliLogger.class));
		
		String widgetName = "widget1";
		Widget widget = new Widget();
		widget.setName(widgetName);
		widget.setCode("0001");
		context.addWidget(widget);
		
		WidgetEvent event1 = new WidgetEvent();
		event1.setName("event1");
		
		AddWidgetEvent operator = new AddWidgetEvent();
		assertThat(operator.apply(context, Collections.singletonList(event1))).isFalse();
	}
	
	@Test
	public void apply_event_name_exists() {
		WidgetOperatorContext context = new WidgetOperatorContext();
		context.setLogger(mock(CliLogger.class));
		
		String widgetName = "widget1";
		Widget widget = new Widget();
		widget.setName(widgetName);
		widget.setCode("0001");
		
		String eventName = "event1";
		WidgetEvent event = new WidgetEvent();
		event.setName(eventName);
		event.setCode("0001");
		
		widget.getEvents().add(event);
		context.addWidget(widget);
		
		context.selectWidget(widgetName);
		
		WidgetEvent addEvent = new WidgetEvent();
		addEvent.setName(eventName);
		
		AddWidgetEvent operator = new AddWidgetEvent();
		assertThat(operator.apply(context, Collections.singletonList(addEvent))).isFalse();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
