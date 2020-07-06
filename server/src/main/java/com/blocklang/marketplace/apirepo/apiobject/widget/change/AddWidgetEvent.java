package com.blocklang.marketplace.apirepo.apiobject.widget.change;

import com.blocklang.marketplace.apirepo.Change;
import com.blocklang.marketplace.apirepo.ChangeData;
import com.blocklang.marketplace.apirepo.ChangedObjectContext;
import com.blocklang.marketplace.apirepo.CodeGenerator;
import com.blocklang.marketplace.apirepo.apiobject.widget.data.AddWidgetEventData;
import com.blocklang.marketplace.apirepo.apiobject.widget.data.WidgetData;

public class AddWidgetEvent extends Change{

	private AddWidgetEventData data;

	@Override
	public void setData(ChangeData data) {
		this.data = (AddWidgetEventData) data;
	}
	
	@Override
	public boolean apply(ChangedObjectContext context) {
		if(!validate(context)) {
			return false;
		}
		
		WidgetData widget = (WidgetData) context.getSelectedObject();
		String seed = widget.getMaxPropertyCode();
		CodeGenerator codeGen = new CodeGenerator(seed);
		
		data.getEvents().forEach(event -> event.setCode(codeGen.next()));
		
		widget.getEvents().addAll(data.getEvents());
		return true;
	}

	private boolean validate(ChangedObjectContext context) {
		WidgetData widget = (WidgetData) context.getSelectedObject();
		if(widget == null) {
			context.getLogger().error("无法执行 addEvent 操作，因为尚未创建 Widget");
			return false;
		}
		
		var existEvents = widget.getEvents();
		return data.getEvents().stream().allMatch(addedEvent -> {
			var eventNameUsed = existEvents
					.stream()
					.anyMatch(existEvent -> existEvent.getName().equals(addedEvent.getName()));
			if(eventNameUsed) {
				context.getLogger().error("事件名 {0} 已存在", addedEvent.getName());
				return false;
			}
			return true;
		});
	}
	
}
