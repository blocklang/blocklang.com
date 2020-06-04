package com.blocklang.marketplace.apirepo.widget.change;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.ApiObjectContext;
import com.blocklang.marketplace.apirepo.Change;
import com.blocklang.marketplace.apirepo.widget.WidgetContext;
import com.blocklang.marketplace.apirepo.widget.data.AddWidgetPropertyData;
import com.blocklang.marketplace.apirepo.widget.data.WidgetData;
import com.blocklang.marketplace.apirepo.widget.data.WidgetEvent;
import com.blocklang.marketplace.apirepo.widget.data.WidgetProperty;
import com.blocklang.marketplace.data.MarketplaceStore;

public class AddWidgetPropertyTest {

	private ApiObjectContext context;
	private Change change;
	
	@BeforeEach
	public void setup() {
		var store = mock(MarketplaceStore.class);
		var logger = mock(CliLogger.class);
		
		context = new WidgetContext(store, logger);
		change = new AddWidgetProperty();
	}
	
	@Test
	public void apply_widget_not_exist() {
		WidgetProperty prop = new WidgetProperty();
		prop.setName("prop1");
		
		AddWidgetPropertyData data = new AddWidgetPropertyData();
		data.setProperties(Collections.singletonList(prop));
		change.setData(data);
		
		assertThat(change.apply(context)).isFalse();
	}
	
	@DisplayName("当新增 Widget 时，默认选中该 Widget")
	@Test
	public void apply_select_widget_when_add_widget() {
		String widgetName = "widget1";
		WidgetData widget = new WidgetData();
		widget.setName(widgetName);
		widget.setCode("0001");
		
		context.addApiObject(widget);
		
		assertThat(context.getSelectedApiObject())
			.usingRecursiveComparison().isEqualTo(widget);
	}
	
	@Test
	public void apply_property_name_exists() {
		String propName = "prop1";
		
		String widgetName = "widget1";
		WidgetData widget = new WidgetData();
		widget.setName(widgetName);
		widget.setCode("0001");
		WidgetProperty prop1 = new WidgetProperty();
		prop1.setName(propName);
		prop1.setCode("0001");
		widget.getProperties().add(prop1);
		context.addApiObject(widget);
		
		WidgetProperty prop = new WidgetProperty();
		prop.setName(propName);
		AddWidgetPropertyData data = new AddWidgetPropertyData();
		data.setProperties(Collections.singletonList(prop));
		
		change.setData(data);
		assertThat(change.apply(context)).isFalse();
	}
	
	@Test
	public void apply_first_property_success() {
		String widgetName = "widget1";
		WidgetData widget = new WidgetData();
		widget.setName(widgetName);
		widget.setCode("0001");
		
		context.addApiObject(widget);
		
		WidgetProperty prop = new WidgetProperty();
		prop.setName("prop1");

		AddWidgetPropertyData data = new AddWidgetPropertyData();
		data.setProperties(Collections.singletonList(prop));
		change.setData(data);
		assertThat(change.apply(context)).isTrue();
		
		// 为属性设置唯一编码
		WidgetData expected = (WidgetData) context.getApiObjects().get(0);
		assertThat(expected.getProperties())
			.hasSize(1)
			.first().hasFieldOrPropertyWithValue("code", "0001");
	}
	
	@Test
	public void apply_second_property_success() {
		String widgetName = "widget1";
		WidgetData widget = new WidgetData();
		widget.setName(widgetName);
		widget.setCode("0001");
		
		WidgetProperty existProp = new WidgetProperty();
		existProp.setCode("0001");
		existProp.setName("prop1");
		widget.getProperties().add(existProp);
		
		WidgetEvent existEvent = new WidgetEvent();
		existEvent.setCode("0002");
		existEvent.setName("event1");
		widget.getEvents().add(existEvent);
		
		context.addApiObject(widget);
		
		WidgetProperty prop = new WidgetProperty();
		prop.setName("prop2");

		AddWidgetPropertyData data = new AddWidgetPropertyData();
		data.setProperties(Collections.singletonList(prop));
		change.setData(data);
		assertThat(change.apply(context)).isTrue();
		
		WidgetData expected = (WidgetData) context.getApiObjects().get(0);
		assertThat(expected.getProperties())
			.hasSize(2)
			.last()
			.hasFieldOrPropertyWithValue("code", "0003")
			.hasFieldOrPropertyWithValue("name", "prop2");
	}
}
