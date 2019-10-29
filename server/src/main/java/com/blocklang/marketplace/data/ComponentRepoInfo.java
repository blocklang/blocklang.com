package com.blocklang.marketplace.data;

import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ComponentRepo;

/**
 * 封装组件库信息以及对应的 API 库信息
 * 
 * @author Zhengwei Jin
 *
 */
public class ComponentRepoInfo {

	private ComponentRepo componentRepo;
	private ApiRepo apiRepo;
	
	public ComponentRepoInfo(ComponentRepo componentRepo, ApiRepo apiRepo) {
		this.componentRepo = componentRepo;
		this.apiRepo = apiRepo;
	}
	
	public ComponentRepo getComponentRepo() {
		return componentRepo;
	}

	public ApiRepo getApiRepo() {
		return apiRepo;
	}
	
}
