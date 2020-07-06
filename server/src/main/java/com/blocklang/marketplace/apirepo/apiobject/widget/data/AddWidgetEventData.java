package com.blocklang.marketplace.apirepo.apiobject.widget.data;

import java.util.List;

import com.blocklang.marketplace.apirepo.ChangeData;

public class AddWidgetEventData implements ChangeData {

	private List<WidgetEvent> events;

	public List<WidgetEvent> getEvents() {
		return events;
	}

	public void setEvents(List<WidgetEvent> events) {
		this.events = events;
	}
	
}
