package com.blocklang.develop.data;

import com.blocklang.marketplace.data.ComponentRepoInfo;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;

// 以后衍生数据类都以 Data 结尾，不再以 Info 结尾
public class ProjectDependenceData extends ComponentRepoInfo{

	private Integer id;
	private ComponentRepoVersion componentRepoVersion;
	private ApiRepoVersion apiRepoVersion;
	
	public ProjectDependenceData(Integer id,
			ComponentRepo componentRepo, 
			ComponentRepoVersion componentRepoVersion, 
			ApiRepo apiRepo, 
			ApiRepoVersion apiRepoVersion) {
		super(componentRepo, apiRepo);
		this.id = id;
		this.componentRepoVersion = componentRepoVersion;
		this.apiRepoVersion = apiRepoVersion;
	}

	public Integer getId() {
		return id;
	}
	
	public ComponentRepoVersion getComponentRepoVersion() {
		return componentRepoVersion;
	}

	public void setComponentRepoVersion(ComponentRepoVersion componentRepoVersion) {
		this.componentRepoVersion = componentRepoVersion;
	}

	public ApiRepoVersion getApiRepoVersion() {
		return apiRepoVersion;
	}

	public void setApiRepoVersion(ApiRepoVersion apiRepoVersion) {
		this.apiRepoVersion = apiRepoVersion;
	}

}
