package com.blocklang.develop.designer.data;

import java.util.Collections;
import java.util.List;

/**
 * 用户在可视化设计器中自定义的函数
 */
public class PageEventHandler {

	private String id;

	private List<VisualNode> nodes;

	private List<NodeConnection> sequenceConnections;

	private List<NodeConnection> dataConnections;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<VisualNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<VisualNode> nodes) {
		this.nodes = nodes;
	}

	public List<NodeConnection> getSequenceConnections() {
		return sequenceConnections == null ? Collections.emptyList() : sequenceConnections;
	}

	public void setSequenceConnections(List<NodeConnection> sequenceConnections) {
		this.sequenceConnections = sequenceConnections;
	}

	public List<NodeConnection> getDataConnections() {
		return dataConnections == null ? Collections.emptyList() : dataConnections;
	}

	public void setDataConnections(List<NodeConnection> dataConnections) {
		this.dataConnections = dataConnections;
	}

}
