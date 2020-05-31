package com.blocklang.marketplace.apiparser.widget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apiparser.OperatorContext;

public class AddWidgetPropertyTest {

	@Test
	public void apply_widget_not_exist() {
		OperatorContext<WidgetData> context = new OperatorContext<WidgetData>();
		context.setLogger(mock(CliLogger.class));
		
		WidgetProperty prop = new WidgetProperty();
		prop.setName("prop1");
		WidgetOperator operator = new AddWidgetProperty();
		
		AddWidgetPropertyData data = new AddWidgetPropertyData();
		data.setProperties(Collections.singletonList(prop));
		operator.setData(data);
		
		assertThat(operator.apply(context)).isFalse();
	}
	
	@DisplayName("当新增 Widget 时，默认选中该 Widget")
	@Test
	public void apply_select_widget_when_add_widget() {
		OperatorContext<WidgetData> context = new OperatorContext<WidgetData>();
		context.setLogger(mock(CliLogger.class));
		
		String widgetName = "widget1";
		WidgetData widget = new WidgetData();
		widget.setName(widgetName);
		widget.setCode("0001");
		
		context.addComponent(widget);
		
		WidgetProperty prop = new WidgetProperty();
		prop.setName("prop1");
		WidgetOperator operator = new AddWidgetProperty();
		AddWidgetPropertyData data = new AddWidgetPropertyData();
		data.setProperties(Collections.singletonList(prop));
		operator.setData(data);
		assertThat(operator.apply(context)).isTrue();
	}
	
	@Test
	public void apply_property_name_exists() {
		OperatorContext<WidgetData> context = new OperatorContext<WidgetData>();
		context.setLogger(mock(CliLogger.class));
		
		String propName = "prop1";
		
		String widgetName = "widget1";
		WidgetData widget = new WidgetData();
		widget.setName(widgetName);
		widget.setCode("0001");
		WidgetProperty prop1 = new WidgetProperty();
		prop1.setName(propName);
		prop1.setCode("0001");
		widget.getProperties().add(prop1);
		context.addComponent(widget); // 默认选中新添加的 widget
		
		WidgetProperty prop = new WidgetProperty();
		prop.setName(propName);
		WidgetOperator operator = new AddWidgetProperty();
		AddWidgetPropertyData data = new AddWidgetPropertyData();
		data.setProperties(Collections.singletonList(prop));
		operator.setData(data);
		assertThat(operator.apply(context)).isFalse();
	}
	
	@Test
	public void apply_first_property_success() {
		OperatorContext<WidgetData> context = new OperatorContext<WidgetData>();
		
		String widgetName = "widget1";
		WidgetData widget = new WidgetData();
		widget.setName(widgetName);
		widget.setCode("0001");
		
		context.addComponent(widget);
		
		WidgetProperty prop = new WidgetProperty();
		prop.setName("prop1");
		WidgetOperator operator = new AddWidgetProperty();
		AddWidgetPropertyData data = new AddWidgetPropertyData();
		data.setProperties(Collections.singletonList(prop));
		operator.setData(data);
		assertThat(operator.apply(context)).isTrue();
		
		// 为属性设置唯一编码
		assertThat(context.getComponents().get(0).getProperties())
			.hasSize(1)
			.first().hasFieldOrPropertyWithValue("code", "0001");
	}
	
	@Test
	public void apply_second_property_success() {
		OperatorContext<WidgetData> context = new OperatorContext<WidgetData>();
		
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
		
		context.addComponent(widget);
		
		WidgetProperty prop = new WidgetProperty();
		prop.setName("prop2");
		WidgetOperator operator = new AddWidgetProperty();
		AddWidgetPropertyData data = new AddWidgetPropertyData();
		data.setProperties(Collections.singletonList(prop));
		operator.setData(data);
		assertThat(operator.apply(context)).isTrue();
		
		assertThat(context.getComponents().get(0).getProperties())
			.hasSize(2)
			.last()
			.hasFieldOrPropertyWithValue("code", "0003")
			.hasFieldOrPropertyWithValue("name", "prop2");
	}
}
