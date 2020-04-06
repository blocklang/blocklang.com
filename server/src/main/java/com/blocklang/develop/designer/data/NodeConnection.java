package com.blocklang.develop.designer.data;

public class NodeConnection {

	private String id;
	private String fromNode;
	private String fromOutput;
	private String toNode;
	private String toInput;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFromNode() {
		return fromNode;
	}

	public void setFromNode(String fromNode) {
		this.fromNode = fromNode;
	}

	public String getFromOutput() {
		return fromOutput;
	}

	public void setFromOutput(String fromOutput) {
		this.fromOutput = fromOutput;
	}

	public String getToNode() {
		return toNode;
	}

	public void setToNode(String toNode) {
		this.toNode = toNode;
	}

	public String getToInput() {
		return toInput;
	}

	public void setToInput(String toInput) {
		this.toInput = toInput;
	}

}
