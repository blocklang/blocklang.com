package com.blocklang.marketplace.apiparser.widget;

import com.blocklang.marketplace.apiparser.ChangeData;
import com.blocklang.marketplace.apiparser.OperatorContext;
import com.blocklang.marketplace.task.CodeGenerator;

public class CreateWidget implements WidgetOperator {

	private WidgetData data;
	
	@Override
	public void setData(ChangeData data) {
		this.data = (WidgetData) data;
	}
	
	@Override
	public boolean apply(OperatorContext<WidgetData> context) {
		if(!validate(context)) {
			context.getLogger().error("Widget.name {0} 已经被占用，请更换", data.getName());
			return false;
		}
		
		data.setCode(context.getComponentCodeGenerator().next());
		CodeGenerator propertiesCodeGen = new CodeGenerator(null);
		data.getProperties().forEach(prop -> prop.setCode(propertiesCodeGen.next()));
		data.getEvents().forEach(event -> event.setCode(propertiesCodeGen.next()));

		context.addComponent(data);
		return true;
	}

	private boolean validate(OperatorContext<WidgetData> context) {
		return !context.getComponents().stream().anyMatch(w -> w.getName().equals(data.getName()));
	}

}
