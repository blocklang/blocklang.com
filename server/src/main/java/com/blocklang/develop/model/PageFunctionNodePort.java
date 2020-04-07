package com.blocklang.develop.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.blocklang.develop.constant.FlowType;
import com.blocklang.develop.constant.PortType;
import com.blocklang.develop.constant.converter.FlowTypeConverter;
import com.blocklang.develop.constant.converter.PortTypeConverter;

@Entity
@Table(name = "page_func_node_port")
public class PageFunctionNodePort implements Serializable{

	private static final long serialVersionUID = 8264518297631225153L;

	@Id
	@Column(name = "dbid", length = 32, updatable = false)
	private String id;

	@Column(name = "project_resource_id", nullable = false)
	private Integer pageId;

	@Column(name = "page_func_node_id", length=32, nullable = false)
	private String nodeId;
	
	@Convert(converter = PortTypeConverter.class)
	@Column(name = "port_type", length = 32, nullable = false)
	private PortType portType;
	
	@Convert(converter = FlowTypeConverter.class)
	@Column(name = "flow_type", length = 16, nullable = false)
	private FlowType flowType;
	
	@Column(name = "output_sequence_port_text", length = 64)
	private String outputSequencePortText;
	
	@Column(name = "input_data_port_value", length = 64)
	private String inputDataPortValue;
	
//	@Column(name = "code", length = 32)
//	private String code;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public PortType getPortType() {
		return portType;
	}

	public void setPortType(PortType portType) {
		this.portType = portType;
	}

	public FlowType getFlowType() {
		return flowType;
	}

	public void setFlowType(FlowType flowType) {
		this.flowType = flowType;
	}

	public String getOutputSequencePortText() {
		return outputSequencePortText;
	}

	public void setOutputSequencePortText(String outputSequencePortText) {
		this.outputSequencePortText = outputSequencePortText;
	}

	public String getInputDataPortValue() {
		return inputDataPortValue;
	}

	public void setInputDataPortValue(String inputDataPortValue) {
		this.inputDataPortValue = inputDataPortValue;
	}

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
