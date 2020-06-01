package com.blocklang.marketplace.apiparser.service;

import java.util.Iterator;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class ServiceOperatorFactory {

	private CliLogger logger;
	
	public ServiceOperatorFactory(CliLogger logger) {
		this.logger = logger;
	}
	
	public ServiceOperator create(JsonNode changeNode) {
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
			if ("createApi".equals(opName)) {
				var data = JsonUtil.treeToValue(changeNode.get("createApi"), ServiceData.class);
				ServiceOperator op = new CreateApi();
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
