package com.blocklang.marketplace.apiparser.service;

import com.blocklang.marketplace.apiparser.ChangeData;
import com.blocklang.marketplace.apiparser.OperatorContext;

public class CreateApi implements ServiceOperator{

	private ServiceData data;
	
	@Override
	public void setData(ChangeData data) {
		this.data = (ServiceData) data;
	}

	@Override
	public boolean apply(OperatorContext<ServiceData> context) {
		if(!validate(context)) {
			context.getLogger().error("Service.name {0} 已被占用，请更换", data.getName());
			return false;
		}
		
		data.setCode(context.getComponentCodeGenerator().next());
		
		context.addComponent(data);
		
		return true;
	}

	/**
	 * 校验 data 是否有效
	 * 
	 * @param context
	 * @return 如果校验通过，则返回 <code>true</code>;否则返回 <code>false</code>
	 */
	private boolean validate(OperatorContext<ServiceData> context) {
		return !context.getComponents().stream().anyMatch(s -> s.getName().equals(data.getName()));
	}

}
