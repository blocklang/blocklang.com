package com.blocklang.marketplace.apirepo.apiobject.widget.change;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.Change;
import com.blocklang.marketplace.apirepo.apiobject.ApiObjectContext;
import com.blocklang.marketplace.apirepo.apiobject.widget.WidgetContext;
import com.blocklang.marketplace.apirepo.apiobject.widget.data.AddWidgetEventData;
import com.blocklang.marketplace.apirepo.apiobject.widget.data.WidgetData;
import com.blocklang.marketplace.apirepo.apiobject.widget.data.WidgetEvent;
import com.blocklang.marketplace.apirepo.apiobject.widget.data.WidgetProperty;
import com.blocklang.marketplace.data.MarketplaceStore;

public class AddWidgetEventTest {

	private ApiObjectContext context;
	private Change change;
	
	@BeforeEach
	public void setup() {
		var store = mock(MarketplaceStore.class);
		var logger = mock(CliLogger.class);
		
		context = new WidgetContext(store, logger);
		change = new AddWidgetEvent();
	}
	
	@Test
	public void apply_first_event_success() {
		String widgetName = "widget1";
		WidgetData widget = new WidgetData();
		widget.setName(widgetName);
		widget.setCode("0001");
		
		context.addObject(widget);
		
		WidgetEvent event1 = new WidgetEvent();
		event1.setName("event1");
		
		AddWidgetEventData eventData = new AddWidgetEventData();
		eventData.setEvents(Collections.singletonList(event1));
		change.setData(eventData);
		assertThat(change.apply(context)).isTrue();
		
		WidgetData expected = (WidgetData) context.getChangedObjects().get(0);
		assertThat(expected.getEvents())
			.hasSize(1)
			.first()
			.hasFieldOrPropertyWithValue("code", "0001");
	}
	
	@Test
	public void apply_second_event_success() {
		String widgetName = "widget1";
		WidgetData widget = new WidgetData();
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
		
		context.addObject(widget);
		
		WidgetEvent addEvent = new WidgetEvent();
		addEvent.setName("event2");
		
		AddWidgetEventData eventData = new AddWidgetEventData();
		eventData.setEvents(Collections.singletonList(addEvent));
		change.setData(eventData);
		
		assertThat(change.apply(context)).isTrue();
		
		WidgetData expected = (WidgetData) context.getChangedObjects().get(0);
		assertThat(expected.getEvents())
			.hasSize(2)
			.last()
			.hasFieldOrPropertyWithValue("code", "0003")
			.hasFieldOrPropertyWithValue("name", "event2");
	}
	
	@Test
	public void apply_widget_not_exist() {
		WidgetEvent event1 = new WidgetEvent();
		event1.setName("event1");
		
		AddWidgetEventData eventData = new AddWidgetEventData();
		eventData.setEvents(Collections.singletonList(event1));
		change.setData(eventData);
	
		assertThat(change.apply(context)).isFalse();
	}
	
	@Test
	public void apply_select_widget_when_add_widget() {
		String widgetName = "widget1";
		WidgetData widget = new WidgetData();
		widget.setName(widgetName);
		widget.setCode("0001");
		context.addObject(widget);
		
		WidgetEvent event1 = new WidgetEvent();
		event1.setName("event1");
		
		AddWidgetEventData eventData = new AddWidgetEventData();
		eventData.setEvents(Collections.singletonList(event1));
		change.setData(eventData);
		
		assertThat(change.apply(context)).isTrue();
	}
	
	@Test
	public void apply_event_name_exists() {
		String widgetName = "widget1";
		WidgetData widget = new WidgetData();
		widget.setName(widgetName);
		widget.setCode("0001");
		
		String eventName = "event1";
		WidgetEvent event = new WidgetEvent();
		event.setName(eventName);
		event.setCode("0001");
		
		widget.getEvents().add(event);
		context.addObject(widget);
		
		WidgetEvent addEvent = new WidgetEvent();
		addEvent.setName(eventName);
		
		AddWidgetEventData eventData = new AddWidgetEventData();
		eventData.setEvents(Collections.singletonList(addEvent));
		change.setData(eventData);
		assertThat(change.apply(context)).isFalse();
	}
	
}
