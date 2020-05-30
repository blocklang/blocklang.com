package com.blocklang.marketplace.apiparser.widget;

import java.util.ArrayList;
import java.util.List;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.task.CodeGenerator;

/**
 * 日志变更的上下文
 * 
 * @author Zhengwei Jin
 *
 */
public class WidgetOperatorContext {

	private List<WidgetData> widgets = new ArrayList<WidgetData>();
	private CodeGenerator codeGenerator;
	
	private WidgetData current;
	
	private CliLogger logger;

	public List<WidgetData> getWidgets() {
		return this.widgets;
	}

	public void addWidget(WidgetData widget) {
		this.widgets.add(widget);
		this.current = widget;
	}

	public CliLogger getLogger() {
		return logger;
	}

	public void setLogger(CliLogger logger) {
		this.logger = logger;
	}

	public CodeGenerator getWidgetCodeGenerator() {
		if(codeGenerator != null) {
			return codeGenerator;
		}
		String seed = widgets.isEmpty() ? null : widgets.get(widgets.size() - 1).getCode();
		return new CodeGenerator(seed);
	}

	public WidgetData getSelectedWidget() {
		return current;
	}
	
}
