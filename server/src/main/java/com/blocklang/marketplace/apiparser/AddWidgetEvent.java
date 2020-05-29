package com.blocklang.marketplace.apiparser;

import java.util.List;

import com.blocklang.marketplace.data.changelog.Widget;
import com.blocklang.marketplace.data.changelog.WidgetEvent;
import com.blocklang.marketplace.task.CodeGenerator;

public class AddWidgetEvent implements WidgetOperator<List<WidgetEvent>>{

	@Override
	public boolean apply(WidgetOperatorContext context, List<WidgetEvent> data) {
		if(!validate(context, data)) {
			return false;
		}
		
		Widget widget = context.getSelectedWidget();
		String seed = widget.getMaxPropertyCode();
		CodeGenerator codeGen = new CodeGenerator(seed);
		
		data.forEach(event -> event.setCode(codeGen.next()));
		
		widget.getEvents().addAll(data);
		return true;
	}

	private boolean validate(WidgetOperatorContext context, List<WidgetEvent> data) {
		Widget widget = context.getSelectedWidget();
		if(widget == null) {
			context.getLogger().error("无法执行 addEvent 操作，因为尚未创建 Widget");
			return false;
		}
		
		var existEvents = widget.getEvents();
		return data.stream().allMatch(addedEvent -> {
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
