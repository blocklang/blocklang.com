package com.blocklang.core.runner.common;

import java.util.ArrayList;
import java.util.List;

public class Workflow {

	private String name;
	private List<Job> jobs = new ArrayList<Job>();

	public Workflow(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}
	
	public void addJob(Job job) {
		jobs.add(job);
	}

}
