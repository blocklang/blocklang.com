package com.blocklang.develop.designer.data;

import java.util.List;

import com.blocklang.develop.model.PageDataItem;

/**
 * 设计器中使用的页面模型
 * 
 * @author jinzw
 *
 */
public class PageModel {

	private Integer pageId;
	private List<AttachedWidget> widgets;
	private List<PageDataItem> data;
	private List<PageFunction> functions;

	public Integer getPageId() {
		return pageId;
	}

	public void setPageId(Integer pageId) {
		this.pageId = pageId;
	}

	public List<AttachedWidget> getWidgets() {
		return widgets;
	}

	public void setWidgets(List<AttachedWidget> widgets) {
		this.widgets = widgets;
	}

	public List<PageDataItem> getData() {
		return data;
	}

	public void setData(List<PageDataItem> data) {
		this.data = data;
	}

	public List<PageFunction> getFunctions() {
		return functions;
	}

	public void setFunctions(List<PageFunction> functions) {
		this.functions = functions;
	}

}
