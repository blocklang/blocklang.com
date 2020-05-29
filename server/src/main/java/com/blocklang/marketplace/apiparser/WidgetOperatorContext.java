package com.blocklang.marketplace.apiparser;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.data.changelog.Widget;
import com.blocklang.marketplace.task.CodeGenerator;

/**
 * 日志变更的上下文
 * 
 * @author Zhengwei Jin
 *
 */
public class WidgetOperatorContext {

	private List<Widget> widgets = new ArrayList<Widget>();
	private CodeGenerator widgetCodeGenerator;
	
	private Widget current;
	
	private CliLogger logger;

	public List<Widget> getWidgets() {
		return this.widgets;
	}

	public void addWidget(Widget widget) {
		this.widgets.add(widget);
	}

	public CliLogger getLogger() {
		return logger;
	}

	public void setLogger(CliLogger logger) {
		this.logger = logger;
	}

	public CodeGenerator getWidgetCodeGenerator() {
		if(widgetCodeGenerator != null) {
			return widgetCodeGenerator;
		}
		String seed = widgets.isEmpty() ? null : widgets.get(widgets.size() - 1).getCode();
		return new CodeGenerator(seed);
	}

	public void selectWidget(String widgetName) {
		if(StringUtils.isBlank(widgetName)) {
			return;
		}
		current = widgets.stream()
				.filter(widget -> widgetName.equals(widget.getName()))
				.findFirst()
				.orElse(null);
	}

	public Widget getSelectedWidget() {
		return current;
	}
	
}
