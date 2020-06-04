package com.blocklang.marketplace.apirepo.widget.change;

import com.blocklang.marketplace.apirepo.ChangeData;
import com.blocklang.marketplace.apirepo.widget.data.WidgetData;
import com.blocklang.marketplace.apirepo.ApiObjectContext;
import com.blocklang.marketplace.apirepo.Change;
import com.blocklang.marketplace.task.CodeGenerator;

public class CreateWidget extends Change {

	private WidgetData data;
	
	@Override
	public void setData(ChangeData data) {
		this.data = (WidgetData) data;
	}
	
	@Override
	public boolean apply(ApiObjectContext context) {
		if(!validate(context)) {
			context.getLogger().error("Widget.name {0} 已经被占用，请更换", data.getName());
			return false;
		}
		
		data.setId(context.getApiObjectId());
		data.setCode(context.nextApiObjectCode());
		CodeGenerator propertiesCodeGen = new CodeGenerator(null);
		data.getProperties().forEach(prop -> prop.setCode(propertiesCodeGen.next()));
		data.getEvents().forEach(event -> event.setCode(propertiesCodeGen.next()));

		context.addApiObject(data);
		return true;
	}

	private boolean validate(ApiObjectContext context) {
		return !context.apiObjectNameUsed(data.getName());
	}

}
