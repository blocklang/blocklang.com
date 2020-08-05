package com.blocklang.marketplace.apirepo.schema.change;

import com.blocklang.marketplace.apirepo.Change;
import com.blocklang.marketplace.apirepo.ChangeData;
import com.blocklang.marketplace.apirepo.ChangedObjectContext;
import com.blocklang.marketplace.apirepo.CodeGenerator;
import com.blocklang.marketplace.apirepo.schema.ApiSchemaData;

public class CreateSchema extends Change{

	private ApiSchemaData data;

	@Override
	public void setData(ChangeData data) {
		this.data = (ApiSchemaData) data;
	}

	// TODO: 从 APIObjectContext 中提取出通用的接口
	@Override
	public boolean apply(ChangedObjectContext context) {
		if(!validate(context)) {
			context.getLogger().error("Object.name {0} 已经被占用，请更换", data.getName());
			return false;
		}
		
		// 补充数据
		data.setId(context.getObjectId());
		data.setCode(context.nextObjectCode());
		CodeGenerator propertiesCodeGen = new CodeGenerator(null);
		data.getProperties().forEach(prop -> prop.setCode(propertiesCodeGen.next()));
		
		context.addObject(data);
		
		return super.apply(context);
	}

	private boolean validate(ChangedObjectContext context) {
		return !context.objectNameUsed(data.getName());
	}

	
}
