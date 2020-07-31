package com.blocklang.marketplace.data;

import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;

/**
 * 封装组件库信息以及对应的 API 库信息
 * 
 * @author Zhengwei Jin
 *
 */
public class ComponentRepoInfo {

	private ComponentRepo componentRepo;
	// 获取组件库版本信息，如 master 分支
	private ComponentRepoVersion componentRepoVersion;
	private ApiRepo apiRepo;
	private ApiRepoVersion apiRepoVersion;
	
	public ComponentRepoInfo() {}
	
	public ComponentRepoInfo(ComponentRepo componentRepo, 
			ComponentRepoVersion componentRepoVersion, 
			ApiRepo apiRepo,
			ApiRepoVersion apiRepoVersion) {
		this.componentRepo = componentRepo;
		this.componentRepoVersion = componentRepoVersion;
		this.apiRepo = apiRepo;
		this.apiRepoVersion = apiRepoVersion;
	}
	
	public ComponentRepo getComponentRepo() {
		return componentRepo;
	}

	public ApiRepo getApiRepo() {
		return apiRepo;
	}

	public ComponentRepoVersion getComponentRepoVersion() {
		return componentRepoVersion;
	}

	public ApiRepoVersion getApiRepoVersion() {
		return apiRepoVersion;
	}

	public void setComponentRepo(ComponentRepo componentRepo) {
		this.componentRepo = componentRepo;
	}

	public void setComponentRepoVersion(ComponentRepoVersion componentRepoVersion) {
		this.componentRepoVersion = componentRepoVersion;
	}

	public void setApiRepo(ApiRepo apiRepo) {
		this.apiRepo = apiRepo;
	}

	public void setApiRepoVersion(ApiRepoVersion apiRepoVersion) {
		this.apiRepoVersion = apiRepoVersion;
	}
	
}
