package com.blocklang.marketplace.apirepo.webapi.change;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apirepo.ApiObjectContext;
import com.blocklang.marketplace.apirepo.Change;
import com.blocklang.marketplace.apirepo.ChangeData;
import com.blocklang.marketplace.apirepo.webapi.data.AddFunctionData;
import com.blocklang.marketplace.apirepo.webapi.data.JsObjectData;
import com.blocklang.marketplace.task.CodeGenerator;

public class AddFunction extends Change{

	private AddFunctionData data;
	
	@Override
	public void setData(ChangeData data) {
		this.data = (AddFunctionData) data;
	}

	@Override
	public boolean apply(ApiObjectContext context) {
		if(!validate(context)) {
			return false;
		}
		
		JsObjectData jsObject = (JsObjectData) context.getSelectedApiObject();
		
		String seed = jsObject.getMaxFunctionCode();
		CodeGenerator codeGen = new CodeGenerator(seed);
		data.getFunctions().forEach(func -> {
			func.setCode(codeGen.next());
			
			CodeGenerator paramCodeGen = new CodeGenerator(null);
			func.getParameters().forEach(param -> param.setCode(paramCodeGen.next()));
		});
		
		jsObject.getFunctions().addAll(data.getFunctions());
		return true;
	}
	
	private boolean validate(ApiObjectContext context) {
		CliLogger logger = context.getLogger();
		JsObjectData jsObject = (JsObjectData) context.getSelectedApiObject();
		if(jsObject == null) {
			context.getLogger().error("无法执行 addFunction 操作，因为尚未创建 JavaScript 对象");
			return false;
		}
		
		var existFunctions = jsObject.getFunctions();
		return data.getFunctions().stream().allMatch(addedFunction -> {
			var funcNameUsed = existFunctions
					.stream()
					.anyMatch(existProperty -> existProperty.getName().equals(addedFunction.getName()));
			if(funcNameUsed) {
				logger.error("函数名 {0} 已存在", addedFunction.getName());
				return false;
			}
			return true;
		});
	}

}
