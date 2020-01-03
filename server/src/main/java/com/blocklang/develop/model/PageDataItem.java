package com.blocklang.develop.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "page_data", uniqueConstraints = @UniqueConstraint(columnNames = { "project_resource_id", "parent_id",
		"name" }))
public class PageDataItem implements Serializable {

	private static final long serialVersionUID = -7686028426281625398L;

	@Id
	@Column(name = "dbid", length = 32, updatable = false)
	private String id;
	
	@Column(name = "parent_id", length = 32, nullable = false)
	private String parentId;
	
	@Column(name = "seq", nullable = false)
	private Integer seq;
	
	@Column(name = "project_resource_id", nullable = false)
	private Integer pageId;
	
	@Column(name = "name", nullable = false)
	private String name;
	
	@Column(name = "type", nullable = false)
	private String type;
	
	@Column(name = "default_value", nullable = true)
	private String value;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
