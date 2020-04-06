package com.blocklang.develop.designer.data;

import java.util.List;

public class VisualNode {

	private String id;
	private Integer left;
	private Integer top;
	private String caption;
	private String text;
	private String layout;
	private String category;
	private InputSequencePort inputSequencePort;
	private List<OutputSequencePort> outputSequencePorts;
	private List<InputDataPort> inputDataPorts;
	private List<DataPort> outputDataPorts;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public InputSequencePort getInputSequencePort() {
		return inputSequencePort;
	}

	public void setInputSequencePort(InputSequencePort inputSequencePort) {
		this.inputSequencePort = inputSequencePort;
	}

	public List<OutputSequencePort> getOutputSequencePorts() {
		return outputSequencePorts;
	}

	public void setOutputSequencePorts(List<OutputSequencePort> outputSequencePorts) {
		this.outputSequencePorts = outputSequencePorts;
	}

	public List<InputDataPort> getInputDataPorts() {
		return inputDataPorts;
	}

	public void setInputDataPorts(List<InputDataPort> inputDataPorts) {
		this.inputDataPorts = inputDataPorts;
	}

	public List<DataPort> getOutputDataPorts() {
		return outputDataPorts;
	}

	public void setOutputDataPorts(List<DataPort> outputDataPorts) {
		this.outputDataPorts = outputDataPorts;
	}

}
