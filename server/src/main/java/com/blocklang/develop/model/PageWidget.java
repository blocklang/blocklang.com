package com.blocklang.develop.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.blocklang.core.model.PartialIdField;

@Entity
@Table(name="page_widget")
public class PageWidget extends PartialIdField {

	private static final long serialVersionUID = 3646189227595692037L;

	@Column(name = "parent_id", nullable = false)
	private Integer parentId;
	
	@Column(name = "seq", nullable = false)
	private Integer seq;
	
	@Column(name = "project_resource_id", nullable = false)
	private Integer pageId;
	
	@Column(name = "api_repo_id", nullable = false)
	private Integer apiRepoId;
	
	@Column(name = "widget_core", length = 4, nullable = false)
	private String widgetCode;

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public Integer getPageId() {
		return pageId;
	}

	public void setPageId(Integer pageId) {
		this.pageId = pageId;
	}

	public Integer getApiRepoId() {
		return apiRepoId;
	}

	public void setApiRepoId(Integer apiRepoId) {
		this.apiRepoId = apiRepoId;
	}

	public String getWidgetCode() {
		return widgetCode;
	}

	public void setWidgetCode(String widgetCode) {
		this.widgetCode = widgetCode;
	}
	
}
