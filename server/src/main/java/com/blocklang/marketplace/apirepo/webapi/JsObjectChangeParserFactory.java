package com.blocklang.marketplace.apirepo.webapi;

import java.util.Iterator;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.apirepo.Change;
import com.blocklang.marketplace.apirepo.ChangeParserFactory;
import com.blocklang.marketplace.apirepo.webapi.change.AddFunction;
import com.blocklang.marketplace.apirepo.webapi.change.CreateObject;
import com.blocklang.marketplace.apirepo.webapi.data.AddFunctionData;
import com.blocklang.marketplace.apirepo.webapi.data.JsObjectData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class JsObjectChangeParserFactory extends ChangeParserFactory {

	public JsObjectChangeParserFactory(CliLogger logger) {
		super(logger);
	}

	@Override
	public Change create(JsonNode changeNode) {
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
				Change op = new CreateObject();
				op.setData(data);
				return op;
			} else if ("addFunction".equals(opName)) {
				var data = JsonUtil.treeToValue(changeNode.get("addFunction"), AddFunctionData.class); 
				Change op = new AddFunction();
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
