package com.blocklang.marketplace.apiparser.widget;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apiparser.ChangeData;
import com.blocklang.marketplace.apiparser.OperatorContext;
import com.blocklang.marketplace.task.CodeGenerator;

public class AddWidgetProperty implements WidgetOperator{

	private AddWidgetPropertyData data;
	
	@Override
	public void setData(ChangeData data) {
		this.data = (AddWidgetPropertyData) data;
	}
	
	@Override
	public boolean apply(OperatorContext<WidgetData> context) {
		if(!validate(context)) {
			return false;
		}
		
		WidgetData widget = context.getSelectedComponent();
		
		String seed = widget.getMaxPropertyCode();
		CodeGenerator codeGen = new CodeGenerator(seed);
		
		data.getProperties().forEach(prop -> prop.setCode(codeGen.next()));
		widget.getProperties().addAll(data.getProperties());
		return true;
	}

	private boolean validate(OperatorContext<WidgetData> context) {
		CliLogger logger = context.getLogger();
		WidgetData widget = context.getSelectedComponent();
		if(widget == null) {
			context.getLogger().error("无法执行 addProperty 操作，因为尚未创建 Widget");
			return false;
		}
		
		var existProperties = widget.getProperties();
		return data.getProperties().stream().allMatch(addedProperty -> {
			var propNameUsed = existProperties
					.stream()
					.anyMatch(existProperty -> existProperty.getName().equals(addedProperty.getName()));
			if(propNameUsed) {
				logger.error("属性名 {0} 已存在", addedProperty.getName());
				return false;
			}
			return true;
		});
	}

}
