package com.blocklang.develop.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialIdField;

@Entity
@Table(name = "page_widget_attr_value", uniqueConstraints = @UniqueConstraint(columnNames = { "page_widget_id",
		"widget_attr_code" }))
public class PageWidgetAttrValue extends PartialIdField {

	private static final long serialVersionUID = 3737318167108619499L;

	@Column(name = "page_widget_id", nullable = false)
	private Integer pageWidgetId;
	
	@Column(name = "widget_attr_code", nullable = false, length = 4)
	private String widgetAttrCode;
	
	@Column(name = "attr_value", nullable = false)
	private String attrValue;
	
	@Column(name = "is_expr", nullable = false)
	private Boolean expr = false;

	public Integer getPageWidgetId() {
		return pageWidgetId;
	}

	public void setPageWidgetId(Integer pageWidgetId) {
		this.pageWidgetId = pageWidgetId;
	}

	public String getWidgetAttrCode() {
		return widgetAttrCode;
	}

	public void setWidgetAttrCode(String widgetAttrCode) {
		this.widgetAttrCode = widgetAttrCode;
	}

	public String getAttrValue() {
		return attrValue;
	}

	public void setAttrValue(String attrValue) {
		this.attrValue = attrValue;
	}

	public Boolean isExpr() {
		return expr;
	}

	public void setExpr(Boolean expr) {
		this.expr = expr;
	}
	
}
