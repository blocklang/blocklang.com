package com.blocklang.marketplace.apiparser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.data.changelog.Widget;
import com.blocklang.marketplace.data.changelog.WidgetEvent;
import com.blocklang.marketplace.data.changelog.WidgetProperty;

public class AddWidgetPropertyTest {

	@Test
	public void apply_widget_not_exist() {
		WidgetOperatorContext context = new WidgetOperatorContext();
		context.setLogger(mock(CliLogger.class));
		
		WidgetProperty prop = new WidgetProperty();
		prop.setName("prop1");
		AddWidgetProperty operator = new AddWidgetProperty();
		assertThat(operator.apply(context, Collections.singletonList(prop))).isFalse();
	}
	
	// 未选中 widget
	@Test
	public void apply_not_select_widget() {
		WidgetOperatorContext context = new WidgetOperatorContext();
		context.setLogger(mock(CliLogger.class));
		
		String widgetName = "widget1";
		Widget widget = new Widget();
		widget.setName(widgetName);
		widget.setCode("0001");
		
		context.addWidget(widget);
		
		WidgetProperty prop = new WidgetProperty();
		prop.setName("prop1");
		AddWidgetProperty operator = new AddWidgetProperty();
		assertThat(operator.apply(context, Collections.singletonList(prop))).isFalse();
	}
	
	@Test
	public void apply_property_name_exists() {
		WidgetOperatorContext context = new WidgetOperatorContext();
		context.setLogger(mock(CliLogger.class));
		
		String propName = "prop1";
		
		String widgetName = "widget1";
		Widget widget = new Widget();
		widget.setName(widgetName);
		widget.setCode("0001");
		WidgetProperty prop1 = new WidgetProperty();
		prop1.setName(propName);
		prop1.setCode("0001");
		widget.getProperties().add(prop1);
		context.addWidget(widget);
		
		context.selectWidget(widgetName);
		
		WidgetProperty prop = new WidgetProperty();
		prop.setName(propName);
		AddWidgetProperty operator = new AddWidgetProperty();
		assertThat(operator.apply(context, Collections.singletonList(prop))).isFalse();
	}
	
	@Test
	public void apply_first_property_success() {
		WidgetOperatorContext context = new WidgetOperatorContext();
		
		String widgetName = "widget1";
		Widget widget = new Widget();
		widget.setName(widgetName);
		widget.setCode("0001");
		
		context.addWidget(widget);
		
		// 在 context 中指定当前 widget，这样才能将属性应用到当前 widget 上
		context.selectWidget(widgetName);
		
		WidgetProperty prop = new WidgetProperty();
		prop.setName("prop1");
		AddWidgetProperty operator = new AddWidgetProperty();
		assertThat(operator.apply(context, Collections.singletonList(prop))).isTrue();
		
		// 为属性设置唯一编码
		assertThat(context.getWidgets().get(0).getProperties())
			.hasSize(1)
			.first().hasFieldOrPropertyWithValue("code", "0001");
	}
	
	@Test
	public void apply_second_property_success() {
		WidgetOperatorContext context = new WidgetOperatorContext();
		
		String widgetName = "widget1";
		Widget widget = new Widget();
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
		
		context.addWidget(widget);
		context.selectWidget(widgetName);
		
		WidgetProperty prop = new WidgetProperty();
		prop.setName("prop2");
		AddWidgetProperty operator = new AddWidgetProperty();
		assertThat(operator.apply(context, Collections.singletonList(prop))).isTrue();
		
		assertThat(context.getWidgets().get(0).getProperties())
			.hasSize(2)
			.last()
			.hasFieldOrPropertyWithValue("code", "0003")
			.hasFieldOrPropertyWithValue("name", "prop2");
	}
}
