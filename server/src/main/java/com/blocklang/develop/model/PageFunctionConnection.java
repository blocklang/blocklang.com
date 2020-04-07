package com.blocklang.develop.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "page_func_connection")
public class PageFunctionConnection implements Serializable{

	private static final long serialVersionUID = -3171174657720190996L;

	@Id
	@Column(name = "dbid", length = 32, updatable = false)
	private String id;
	
	@Column(name = "project_resource_id", nullable = false)
	private Integer pageId;
	
	@Column(name = "page_func_id", length = 32, nullable = false)
	private String functionId;
	
	@Column(name = "from_node_id", length = 32, nullable = false)
	private String fromNodeId;
	
	@Column(name = "from_output_port_id", length = 32, nullable = false)
	private String fromOutputPortId;
	
	@Column(name = "to_node_id", length = 32, nullable = false)
	private String toNodeId;
	
	@Column(name = "to_input_port_id", length = 32, nullable = false)
	private String toInputPortId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFromOutputPortId() {
		return fromOutputPortId;
	}

	public void setFromOutputPortId(String fromOutputPortId) {
		this.fromOutputPortId = fromOutputPortId;
	}

	public String getToInputPortId() {
		return toInputPortId;
	}

	public void setToInputPortId(String toInputPortId) {
		this.toInputPortId = toInputPortId;
	}

	public Integer getPageId() {
		return pageId;
	}

	public void setPageId(Integer pageId) {
		this.pageId = pageId;
	}

	public String getFunctionId() {
		return functionId;
	}

	public void setFunctionId(String functionId) {
		this.functionId = functionId;
	}

	public String getFromNodeId() {
		return fromNodeId;
	}

	public void setFromNodeId(String fromNodeId) {
		this.fromNodeId = fromNodeId;
	}

	public String getToNodeId() {
		return toNodeId;
	}

	public void setToNodeId(String toNodeId) {
		this.toNodeId = toNodeId;
	}
	
}
