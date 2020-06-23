package com.blocklang.marketplace.apirepo.widget;

import java.util.Iterator;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.apirepo.Change;
import com.blocklang.marketplace.apirepo.ChangeParserFactory;
import com.blocklang.marketplace.apirepo.widget.change.AddWidgetEvent;
import com.blocklang.marketplace.apirepo.widget.change.AddWidgetProperty;
import com.blocklang.marketplace.apirepo.widget.change.CreateWidget;
import com.blocklang.marketplace.apirepo.widget.data.AddWidgetEventData;
import com.blocklang.marketplace.apirepo.widget.data.AddWidgetPropertyData;
import com.blocklang.marketplace.apirepo.widget.data.WidgetData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class WidgetChangeParserFactory extends ChangeParserFactory {

	public WidgetChangeParserFactory(CliLogger logger) {
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
			if ("createWidget".equals(opName)) {
				var data = JsonUtil.treeToValue(changeNode.get("createWidget"), WidgetData.class);
				Change change = new CreateWidget();
				change.setData(data);
				return change;
			} else if ("addProperty".equals(opName)) {
				var data = JsonUtil.treeToValue(changeNode.get("addProperty"), AddWidgetPropertyData.class); 
				Change change = new AddWidgetProperty();
				change.setData(data);
				return change;
			} else if ("addEvent".equals(opName)) {
				var data = JsonUtil.treeToValue(changeNode.get("addEvent"), AddWidgetEventData.class);
				Change change = new AddWidgetEvent();
				change.setData(data);
				return change;
			}
		} catch (JsonProcessingException e) {
			logger.error(e);
		}

		logger.error("当前不支持 {0} 操作", opName);
		return null;
	}

}
