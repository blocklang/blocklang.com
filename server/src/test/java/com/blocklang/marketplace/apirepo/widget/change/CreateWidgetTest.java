package com.blocklang.marketplace.apirepo.widget.change;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.ApiObjectContext;
import com.blocklang.marketplace.apirepo.Change;
import com.blocklang.marketplace.apirepo.widget.WidgetContext;
import com.blocklang.marketplace.apirepo.widget.data.WidgetData;
import com.blocklang.marketplace.apirepo.widget.data.WidgetEvent;
import com.blocklang.marketplace.apirepo.widget.data.WidgetProperty;
import com.blocklang.marketplace.data.MarketplaceStore;

public class CreateWidgetTest {

	private ApiObjectContext context;
	private Change change;
	
	@BeforeEach
	public void setup() {
		var store = mock(MarketplaceStore.class);
		var logger = mock(CliLogger.class);
		
		context = new WidgetContext(store, logger);
		change = new CreateWidget();
	}
	
	@Test
	public void apply_create_one_widget_success() {
		WidgetData w = new WidgetData();
		w.setName("widget1");
		change.setData(w);
		
		String apiObjectId = "1";
		context.setApiObjectId(apiObjectId);
		
		assertThat(change.apply(context)).isTrue();
		assertThat(context.getApiObjects()).hasSize(1);
		assertThat(context.getApiObjects().get(0).getId()).isEqualTo(apiObjectId);
		assertThat(context.getApiObjects().get(0).getCode()).isEqualTo("0001");
	}
	
	@Test
	public void apply_create_two_widget_success() {
		WidgetData w1 = new WidgetData();
		w1.setId("0");
		w1.setName("widget1");
		w1.setCode("0001");
		context.addApiObject(w1);
		
		String apiObjectId = "1";
		context.setApiObjectId(apiObjectId);
		
		WidgetData w2 = new WidgetData();
		w2.setName("widget2");
		
		change.setData(w2);
		assertThat(change.apply(context)).isTrue();
		assertThat(context.getApiObjects()).hasSize(2);
		
		assertThat(context.getApiObjects().get(1).getId()).isEqualTo(apiObjectId);
		assertThat(context.getApiObjects().get(0).getCode()).isEqualTo("0001");
		assertThat(context.getApiObjects().get(1).getCode()).isEqualTo("0002");
	}
	
	@Test
	public void apply_contains_property_and_event_success() {
		WidgetData w = new WidgetData();
		w.setName("widget2");
		
		WidgetProperty prop = new WidgetProperty();
		prop.setName("prop1");
		w.setProperties(Collections.singletonList(prop));
		
		WidgetEvent event = new WidgetEvent();
		event.setName("event1");
		w.setEvents(Collections.singletonList(event));
		
		change.setData(w);
		assertThat(change.apply(context)).isTrue();
		assertThat(context.getApiObjects()).hasSize(1);
		
		WidgetData expected = (WidgetData) context.getApiObjects().get(0);
		assertThat(expected.getCode()).isEqualTo("0001");
		assertThat(expected.getProperties().get(0).getCode()).isEqualTo("0001");
		assertThat(expected.getEvents().get(0).getCode()).isEqualTo("0002");
	}
	
	@Test
	public void apply_widget_name_duplicated() {
		WidgetData w1 = new WidgetData();
		w1.setName("widget1");
		w1.setCode("0001");
		context.addApiObject(w1);
		
		WidgetData w2 = new WidgetData();
		w2.setName("widget1");
		
		change.setData(w2);
		assertThat(change.apply(context)).isFalse();
		assertThat(context.getApiObjects()).hasSize(1);
	}
	
}
