package com.blocklang.develop.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="page_widget")
public class PageWidget implements Serializable {

	private static final long serialVersionUID = 2066367990676054629L;

	@Id
	@Column(name = "dbid", length = 32, updatable = false)
	private String id;
	
	@Column(name = "parent_id", length = 32, nullable = false)
	private String parentId;
	
	@Column(name = "seq", nullable = false)
	private Integer seq;
	
	@Column(name = "project_resource_id", nullable = false)
	private Integer pageId;
	
	@Column(name = "api_repo_id", nullable = false)
	private Integer apiRepoId;
	
	@Column(name = "widget_code", length = 4, nullable = false)
	private String widgetCode;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
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
