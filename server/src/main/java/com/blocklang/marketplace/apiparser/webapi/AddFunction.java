package com.blocklang.marketplace.apiparser.webapi;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.apiparser.ChangeData;
import com.blocklang.marketplace.apiparser.OperatorContext;
import com.blocklang.marketplace.task.CodeGenerator;

public class AddFunction implements JsObjectOperator{

	private AddFunctionData data;
	
	@Override
	public void setData(ChangeData data) {
		this.data = (AddFunctionData) data;
	}

	@Override
	public boolean apply(OperatorContext<JsObjectData> context) {
		if(!validate(context)) {
			return false;
		}
		
		JsObjectData jsObject = context.getSelectedComponent();
		
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
	
	private boolean validate(OperatorContext<JsObjectData> context) {
		CliLogger logger = context.getLogger();
		JsObjectData jsObject = context.getSelectedComponent();
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
