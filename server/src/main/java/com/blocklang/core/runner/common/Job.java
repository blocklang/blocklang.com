package com.blocklang.core.runner.common;

import java.util.ArrayList;
import java.util.List;

public class Job {

	private String id;
	private String name;

	private List<Step> steps = new ArrayList<Step>();

	public Job(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Step> getSteps() {
		return steps;
	}

	public void addStep(Step step) {
		steps.add(step);
	}

}
