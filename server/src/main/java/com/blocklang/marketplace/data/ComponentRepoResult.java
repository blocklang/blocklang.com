package com.blocklang.marketplace.data;

import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;

public class ComponentRepoResult {

	private ComponentRepoPublishTask publishTask;
	private ComponentRepo componentRepo;

	public ComponentRepoPublishTask getPublishTask() {
		return publishTask;
	}

	public void setPublishTask(ComponentRepoPublishTask publishTask) {
		this.publishTask = publishTask;
	}

	public ComponentRepo getComponentRepo() {
		return componentRepo;
	}

	public void setComponentRepo(ComponentRepo componentRepo) {
		this.componentRepo = componentRepo;
	}
}
