package com.blocklang.develop.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.blocklang.develop.constant.BindSource;
import com.blocklang.develop.constant.NodeCategory;
import com.blocklang.develop.constant.NodeLayout;
import com.blocklang.develop.constant.converter.BindSourceConverter;
import com.blocklang.develop.constant.converter.NodeCategoryConverter;
import com.blocklang.develop.constant.converter.NodeLayoutConverter;

@Entity
@Table(name = "page_func_node")
public class PageFunctionNode implements Serializable{

	private static final long serialVersionUID = -6044395148487692772L;

	@Id
	@Column(name = "dbid", length = 32, updatable = false)
	private String id;
	
	@Column(name = "project_resource_id", nullable = false)
	private Integer pageId;

	@Column(name = "page_func_id", length=32, nullable = false)
	private String functionId;
	
	@Column(name = "left", nullable = false)
	private Integer left;
	
	@Column(name = "top", nullable = false)
	private Integer top;
	
	@Convert(converter = NodeLayoutConverter.class)
	@Column(name = "layout", length = 16, nullable = false)
	private NodeLayout layout;
	
	@Convert(converter = NodeCategoryConverter.class)
	@Column(name = "category", length = 16, nullable = false)
	private NodeCategory category;
	
//	@Convert(converter = BindSourceConverter.class)
//	@Column(name = "bind_source", length = 16)
//	private BindSource bindSource;
//	
//	@Column(name = "api_repo_id")
//	private Integer apiRepoId;
//	
//	@Column(name = "code", length = 32)
//	private String code;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFunctionId() {
		return functionId;
	}

	public void setFunctionId(String functionId) {
		this.functionId = functionId;
	}

	public Integer getLeft() {
		return left;
	}

	public void setLeft(Integer left) {
		this.left = left;
	}

	public Integer getTop() {
		return top;
	}

	public void setTop(Integer top) {
		this.top = top;
	}

	public NodeLayout getLayout() {
		return layout;
	}

	public void setLayout(NodeLayout layout) {
		this.layout = layout;
	}

	public NodeCategory getCategory() {
		return category;
	}

	public void setCategory(NodeCategory category) {
		this.category = category;
	}

//	public BindSource getBindSource() {
//		return bindSource;
//	}
//
//	public void setBindSource(BindSource bindSource) {
//		this.bindSource = bindSource;
//	}
//
//	public Integer getApiRepoId() {
//		return apiRepoId;
//	}
//
//	public void setApiRepoId(Integer apiRepoId) {
//		this.apiRepoId = apiRepoId;
//	}
//
//	public String getCode() {
//		return code;
//	}
//
//	public void setCode(String code) {
//		this.code = code;
//	}

	public Integer getPageId() {
		return pageId;
	}

	public void setPageId(Integer pageId) {
		this.pageId = pageId;
	}

}
