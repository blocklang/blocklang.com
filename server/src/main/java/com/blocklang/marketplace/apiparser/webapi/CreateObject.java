package com.blocklang.marketplace.apiparser.webapi;

import com.blocklang.marketplace.apiparser.ChangeData;
import com.blocklang.marketplace.apiparser.OperatorContext;
import com.blocklang.marketplace.task.CodeGenerator;

public class CreateObject implements JsObjectOperator{

	private JsObjectData data;
	
	@Override
	public void setData(ChangeData data) {
		this.data = (JsObjectData) data;
	}

	@Override
	public boolean apply(OperatorContext<JsObjectData> context) {
		if(!validate(context)) {
			context.getLogger().error("Object.name {0} 已经被占用，请更换", data.getName());
			return false;
		}
		
		data.setCode(context.getComponentCodeGenerator().next());
		CodeGenerator funcCodeGen = new CodeGenerator(null);
		data.getFunctions().forEach(func -> {
			func.setCode(funcCodeGen.next());
			
			CodeGenerator paramCodeGen = new CodeGenerator(null);
			func.getParameters().forEach(param -> param.setCode(paramCodeGen.next()));
		});
		
		context.addComponent(data);
		return true;
	}

	private boolean validate(OperatorContext<JsObjectData> context) {
		return !context.getComponents().stream().anyMatch(w -> w.getName().equals(data.getName()));
	}

}
