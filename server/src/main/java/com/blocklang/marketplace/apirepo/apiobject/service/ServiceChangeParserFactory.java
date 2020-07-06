package com.blocklang.marketplace.apirepo.apiobject.service;

import java.util.Iterator;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.apirepo.Change;
import com.blocklang.marketplace.apirepo.ChangeParserFactory;
import com.blocklang.marketplace.apirepo.apiobject.service.change.CreateApi;
import com.blocklang.marketplace.apirepo.apiobject.service.data.ServiceData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class ServiceChangeParserFactory extends ChangeParserFactory {

	public ServiceChangeParserFactory(CliLogger logger) {
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
			if ("createApi".equals(opName)) {
				var data = JsonUtil.treeToValue(changeNode.get("createApi"), ServiceData.class);
				Change op = new CreateApi();
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
