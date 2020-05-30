package com.blocklang.marketplace.apiparser;

import java.util.Iterator;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.apiparser.widget.AddWidgetEvent;
import com.blocklang.marketplace.apiparser.widget.AddWidgetEventData;
import com.blocklang.marketplace.apiparser.widget.AddWidgetProperty;
import com.blocklang.marketplace.apiparser.widget.AddWidgetPropertyData;
import com.blocklang.marketplace.apiparser.widget.WidgetData;
import com.blocklang.marketplace.apiparser.widget.WidgetOperator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class OperatorFactory {
	
	private CliLogger logger;
	
	public OperatorFactory(CliLogger logger) {
		this.logger = logger;
	}

	// 增加操作后的变化点
	// 1. 增加数据类
	// 2. 在此处创建操作
	public WidgetOperator create(JsonNode changeNode) {
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
			if ("createWidget".equals(opName)) {
				var data = JsonUtil.treeToValue(changeNode.get("createWidget"), WidgetData.class);
				WidgetOperator op = new CreateWidget();
				op.setData(data);
				return op;
			} else if ("addProperty".equals(opName)) {
				var data = JsonUtil.treeToValue(changeNode.get("addProperty"), AddWidgetPropertyData.class); 
				WidgetOperator op = new AddWidgetProperty();
				op.setData(data);
				return op;
			} else if ("addEvent".equals(opName)) {
				var data = JsonUtil.treeToValue(changeNode.get("addEvent"), AddWidgetEventData.class);
				WidgetOperator op = new AddWidgetEvent();
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
