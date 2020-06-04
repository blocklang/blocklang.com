package com.blocklang.marketplace.apirepo.service.change;

import com.blocklang.marketplace.apirepo.ApiObjectContext;
import com.blocklang.marketplace.apirepo.Change;
import com.blocklang.marketplace.apirepo.ChangeData;
import com.blocklang.marketplace.apirepo.service.data.ServiceData;

public class CreateApi extends Change{

	private ServiceData data;
	
	@Override
	public void setData(ChangeData data) {
		this.data = (ServiceData) data;
	}

	@Override
	public boolean apply(ApiObjectContext context) {
		if(!validate(context)) {
			context.getLogger().error("Service.name {0} 已被占用，请更换", data.getName());
			return false;
		}
		
		data.setId(context.getApiObjectId());
		data.setCode(context.nextApiObjectCode());
		context.addApiObject(data);
		
		return true;
	}

	/**
	 * 校验 data 是否有效
	 * 
	 * @param context
	 * @return 如果校验通过，则返回 <code>true</code>;否则返回 <code>false</code>
	 */
	private boolean validate(ApiObjectContext context) {
		return !context.apiObjectNameUsed(data.getName());
	}

}
