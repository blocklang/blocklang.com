package com.blocklang.marketplace.apiparser.widget;

import com.blocklang.marketplace.apiparser.ChangeData;
import com.blocklang.marketplace.apiparser.OperatorContext;
import com.blocklang.marketplace.task.CodeGenerator;

public class AddWidgetEvent implements WidgetOperator{

	private AddWidgetEventData data;

	@Override
	public void setData(ChangeData data) {
		this.data = (AddWidgetEventData) data;
	}
	
	@Override
	public boolean apply(OperatorContext<WidgetData> context) {
		if(!validate(context)) {
			return false;
		}
		
		WidgetData widget = context.getSelectedComponent();
		String seed = widget.getMaxPropertyCode();
		CodeGenerator codeGen = new CodeGenerator(seed);
		
		data.getEvents().forEach(event -> event.setCode(codeGen.next()));
		
		widget.getEvents().addAll(data.getEvents());
		return true;
	}

	private boolean validate(OperatorContext<WidgetData> context) {
		WidgetData widget = context.getSelectedComponent();
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
