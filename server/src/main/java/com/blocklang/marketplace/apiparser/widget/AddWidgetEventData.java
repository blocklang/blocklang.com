package com.blocklang.marketplace.apiparser.widget;

import java.util.List;

import com.blocklang.marketplace.apiparser.ChangeData;

public class AddWidgetEventData implements ChangeData {

	private List<WidgetEvent> events;

	public List<WidgetEvent> getEvents() {
		return events;
	}

	public void setEvents(List<WidgetEvent> events) {
		this.events = events;
	}
	
}
