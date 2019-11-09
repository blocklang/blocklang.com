package com.blocklang.develop.designer.data;

import java.util.List;

/**
 * 设计器中使用的页面模型
 * 
 * @author jinzw
 *
 */
public class PageModel {

	private Integer pageId;
	private List<AttachedWidget> widgets;

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

}
