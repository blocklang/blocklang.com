package com.blocklang.marketplace.apiparser;

import com.blocklang.marketplace.data.changelog.Widget;
import com.blocklang.marketplace.task.CodeGenerator;

public class CreateWidget implements WidgetOperator<Widget> {

	@Override
	public boolean apply(WidgetOperatorContext context, Widget data) {
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

	private boolean validate(WidgetOperatorContext context, Widget data) {
		return context.getWidgets().stream().anyMatch(w -> w.getName().equals(data.getName()));
	}

}
