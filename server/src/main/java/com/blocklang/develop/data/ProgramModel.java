package com.blocklang.develop.data;

import java.util.List;

import com.blocklang.develop.model.ProjectResource;

public class ProgramModel {

	private ProjectResource resource;
	private List uiModel;
	private List view;
	private List data;
	private List methods;
	public ProjectResource getResource() {
		return resource;
	}
	public void setResource(ProjectResource resource) {
		this.resource = resource;
	}
	public List getUiModel() {
		return uiModel;
	}
	public void setUiModel(List uiModel) {
		this.uiModel = uiModel;
	}
	public List getView() {
		return view;
	}
	public void setView(List view) {
		this.view = view;
	}
	public List getData() {
		return data;
	}
	public void setData(List data) {
		this.data = data;
	}
	public List getMethods() {
		return methods;
	}
	public void setMethods(List methods) {
		this.methods = methods;
	}
	
}
