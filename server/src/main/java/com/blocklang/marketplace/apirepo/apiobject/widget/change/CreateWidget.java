package com.blocklang.marketplace.apirepo.apiobject.widget.change;

import com.blocklang.marketplace.apirepo.Change;
import com.blocklang.marketplace.apirepo.ChangeData;
import com.blocklang.marketplace.apirepo.ChangedObjectContext;
import com.blocklang.marketplace.apirepo.CodeGenerator;
import com.blocklang.marketplace.apirepo.apiobject.widget.data.WidgetData;

public class CreateWidget extends Change {

	private WidgetData data;
	
	@Override
	public void setData(ChangeData data) {
		this.data = (WidgetData) data;
	}
	
	@Override
	public boolean apply(ChangedObjectContext context) {
		if(!validate(context)) {
			context.getLogger().error("Widget.name {0} 已经被占用，请更换", data.getName());
			return false;
		}
		
		data.setId(context.getObjectId());
		data.setCode(context.nextObjectCode());
		CodeGenerator propertiesCodeGen = new CodeGenerator(null);
		data.getProperties().forEach(prop -> prop.setCode(propertiesCodeGen.next()));
		data.getEvents().forEach(event -> event.setCode(propertiesCodeGen.next()));

		context.addObject(data);
		return true;
	}

	private boolean validate(ChangedObjectContext context) {
		return !context.objectNameUsed(data.getName());
	}

}
