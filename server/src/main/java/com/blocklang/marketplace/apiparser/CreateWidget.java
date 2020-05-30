package com.blocklang.marketplace.apiparser;

import com.blocklang.marketplace.apiparser.widget.WidgetData;
import com.blocklang.marketplace.apiparser.widget.WidgetOperator;
import com.blocklang.marketplace.apiparser.widget.WidgetOperatorContext;
import com.blocklang.marketplace.task.CodeGenerator;

public class CreateWidget implements WidgetOperator {

	private WidgetData data;
	
	@Override
	public void setData(ChangeData data) {
		this.data = (WidgetData) data;
	}
	
	@Override
	public boolean apply(WidgetOperatorContext context) {
		if(validate(context, data)) {
			context.getLogger().error("Widget.name {0} 已经被占用，请更换", data.getName());
			return false;
		}
		
		data.setCode(context.getWidgetCodeGenerator().next());
		CodeGenerator propertiesCodeGen = new CodeGenerator(null);
		data.getProperties().forEach(prop -> prop.setCode(propertiesCodeGen.next()));
		data.getEvents().forEach(event -> event.setCode(propertiesCodeGen.next()));

		context.addWidget(data);
		return true;
	}

	private boolean validate(WidgetOperatorContext context, WidgetData data) {
		return context.getWidgets().stream().anyMatch(w -> w.getName().equals(data.getName()));
	}

}
