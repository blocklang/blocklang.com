package com.blocklang.develop.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "page_func")
public class PageFunction implements Serializable {

	private static final long serialVersionUID = 4242940025009427195L;

	@Id
	@Column(name = "dbid", length = 32, updatable = false)
	private String id;

	@Column(name = "project_resource_id", nullable = false)
	private Integer pageId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getPageId() {
		return pageId;
	}

	public void setPageId(Integer pageId) {
		this.pageId = pageId;
	}

}
