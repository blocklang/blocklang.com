package com.blocklang.develop.designer.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VisualNode {

	private String id;
	private Integer left;
	private Integer top;
	private String caption;
	private String text;
	private String layout;
	private String category;
	private String dataItemId;
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
		return outputSequencePorts == null ? Collections.emptyList() : outputSequencePorts;
	}

	public void setOutputSequencePorts(List<OutputSequencePort> outputSequencePorts) {
		this.outputSequencePorts = outputSequencePorts;
	}

	public List<InputDataPort> getInputDataPorts() {
		return inputDataPorts == null ? Collections.emptyList() : inputDataPorts;
	}

	public void setInputDataPorts(List<InputDataPort> inputDataPorts) {
		this.inputDataPorts = inputDataPorts;
	}

	public List<DataPort> getOutputDataPorts() {
		return outputDataPorts == null ? Collections.emptyList() : outputDataPorts;
	}

	public void setOutputDataPorts(List<DataPort> outputDataPorts) {
		this.outputDataPorts = outputDataPorts;
	}

	public void addOutputSequencePort(OutputSequencePort osp) {
		if(this.outputSequencePorts == null) {
			this.outputSequencePorts = new ArrayList<OutputSequencePort>();
		}
		this.outputSequencePorts.add(osp);
	}
	
	public void addInputDataPort(InputDataPort idp) {
		if(this.inputDataPorts == null) {
			this.inputDataPorts = new ArrayList<InputDataPort>();
		}
		this.inputDataPorts.add(idp);
	}

	public void addOutputDataPort(DataPort odp) {
		if(this.outputDataPorts == null) {
			this.outputDataPorts = new ArrayList<DataPort>();
		}
		this.outputDataPorts.add(odp);
	}

	public String getDataItemId() {
		return dataItemId;
	}

	public void setDataItemId(String dataItemId) {
		this.dataItemId = dataItemId;
	}
	
}
