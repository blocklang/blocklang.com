package com.blocklang.marketplace.data.changelog;

import java.util.List;

public class AddWidgetEvent implements ChangeData {

	private List<WidgetEvent> events;

	public List<WidgetEvent> getEvents() {
		return events;
	}

	public void setEvents(List<WidgetEvent> events) {
		this.events = events;
	}
	
}
