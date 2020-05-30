package com.blocklang.marketplace.apiparser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apiparser.widget.AddWidgetPropertyData;
import com.blocklang.marketplace.apiparser.widget.WidgetData;
import com.blocklang.marketplace.apiparser.widget.WidgetOperatorContext;
import com.blocklang.marketplace.apiparser.widget.WidgetProperty;
import com.blocklang.marketplace.data.changelog.Change;

public class WidgetMergerTest {

	@Test
	public void apply() {
		WidgetOperatorContext context = new WidgetOperatorContext();
		context.setLogger(mock(CliLogger.class));
		
		WidgetMerger merger = new WidgetMerger();
		
		List<Change> changes = new ArrayList<>();
		
		WidgetData widget = new WidgetData();
		widget.setName("widget1");
		changes.add(new Change("createWidget", widget));
		
		WidgetProperty prop1 = new WidgetProperty();
		prop1.setName("prop1");
		com.blocklang.marketplace.apiparser.widget.AddWidgetPropertyData added = new AddWidgetPropertyData();
		added.setProperties(Collections.singletonList(prop1));
		changes.add(new Change("addProperty", added));
		
		assertThat(merger.apply(context, changes)).isTrue();
		
		WidgetData result = context.getSelectedWidget();
		assertThat(result.getName()).isEqualTo("widget1");
		assertThat(result.getCode()).isEqualTo("0001");
		assertThat(result.getProperties().get(0).getName()).isEqualTo("prop1");
		assertThat(result.getProperties().get(0).getCode()).isEqualTo("0001");
	}
	
}
