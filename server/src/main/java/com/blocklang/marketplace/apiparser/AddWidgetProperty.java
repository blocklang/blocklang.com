package com.blocklang.marketplace.apiparser;

import java.util.List;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.data.changelog.Widget;
import com.blocklang.marketplace.data.changelog.WidgetProperty;
import com.blocklang.marketplace.task.CodeGenerator;

public class AddWidgetProperty implements WidgetOperator<List<WidgetProperty>>{

	@Override
	public boolean apply(WidgetOperatorContext context, List<WidgetProperty> data) {
		if(!validate(context, data)) {
			return false;
		}
		
		Widget widget = context.getSelectedWidget();
		
		String seed = widget.getMaxPropertyCode();
		CodeGenerator codeGen = new CodeGenerator(seed);
		
		data.forEach(prop -> prop.setCode(codeGen.next()));
		widget.getProperties().addAll(data);
		return true;
	}

	private boolean validate(WidgetOperatorContext context, List<WidgetProperty> data) {
		CliLogger logger = context.getLogger();
		Widget widget = context.getSelectedWidget();
		if(widget == null) {
			context.getLogger().error("无法执行 addProperty 操作，因为尚未创建 Widget");
			return false;
		}
		
		var existProperties = widget.getProperties();
		return data.stream().allMatch(addedProperty -> {
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
