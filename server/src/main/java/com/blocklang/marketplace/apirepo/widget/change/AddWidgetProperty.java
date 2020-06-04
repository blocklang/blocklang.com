package com.blocklang.marketplace.apirepo.widget.change;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.ApiObjectContext;
import com.blocklang.marketplace.apirepo.Change;
import com.blocklang.marketplace.apirepo.ChangeData;
import com.blocklang.marketplace.apirepo.widget.data.AddWidgetPropertyData;
import com.blocklang.marketplace.apirepo.widget.data.WidgetData;
import com.blocklang.marketplace.task.CodeGenerator;

public class AddWidgetProperty extends Change{

	private AddWidgetPropertyData data;
	
	@Override
	public void setData(ChangeData data) {
		this.data = (AddWidgetPropertyData) data;
	}
	
	@Override
	public boolean apply(ApiObjectContext context) {
		if(!validate(context)) {
			return false;
		}
		
		WidgetData widget = (WidgetData) context.getSelectedApiObject();
		
		String seed = widget.getMaxPropertyCode();
		CodeGenerator codeGen = new CodeGenerator(seed);
		
		data.getProperties().forEach(prop -> prop.setCode(codeGen.next()));
		widget.getProperties().addAll(data.getProperties());
		return true;
	}

	private boolean validate(ApiObjectContext context) {
		CliLogger logger = context.getLogger();
		WidgetData widget = (WidgetData) context.getSelectedApiObject();
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
