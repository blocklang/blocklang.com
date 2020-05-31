package com.blocklang.marketplace.apiparser.webapi;

import java.util.Iterator;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class JsObjectOperatorFactory {
	private CliLogger logger;
	
	public JsObjectOperatorFactory(CliLogger logger) {
		this.logger = logger;
	}

	// 增加操作后的变化点
	// 1. 增加数据类
	// 2. 在此处创建操作
	public JsObjectOperator create(JsonNode changeNode) {
		String opName = null;
		int fieldCount = 0;
		Iterator<String> fieldNames = changeNode.fieldNames();
		while(fieldNames.hasNext()) {
			fieldCount++;
			opName = fieldNames.next();
		}
		if(fieldCount != 1) {
			logger.error("一个 change 节点中只能包含一个操作");
			return null;
		}

		try {
			if ("createObject".equals(opName)) {
				var data = JsonUtil.treeToValue(changeNode.get("createObject"), JsObjectData.class);
				JsObjectOperator op = new CreateObject();
				op.setData(data);
				return op;
			} else if ("addFunction".equals(opName)) {
				var data = JsonUtil.treeToValue(changeNode.get("addFunction"), AddFunctionData.class); 
				JsObjectOperator op = new AddFunction();
				op.setData(data);
				return op;
			}
		} catch (JsonProcessingException e) {
			logger.error(e);
		}

		logger.error("当前不支持 {0} 操作", opName);
		return null;
	}
}
