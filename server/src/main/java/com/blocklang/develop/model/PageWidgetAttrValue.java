package com.blocklang.develop.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "page_widget_attr_value", uniqueConstraints = @UniqueConstraint(columnNames = { "page_widget_id",
		"widget_attr_code" }))
public class PageWidgetAttrValue implements Serializable {

	private static final long serialVersionUID = 7655522707829747223L;

	@Id
	@Column(name = "dbid", length = 32, updatable = false)
	private String id;

	@Column(name = "page_widget_id", length = 32, nullable = false)
	private String pageWidgetId;
	
	@Column(name = "widget_attr_code", nullable = false, length = 4)
	private String widgetAttrCode;
	
	@Column(name = "attr_value", nullable = false)
	private String attrValue;
	
	@Column(name = "is_expr", nullable = false)
	private Boolean expr = false;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPageWidgetId() {
		return pageWidgetId;
	}

	public void setPageWidgetId(String pageWidgetId) {
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
