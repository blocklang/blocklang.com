package com.blocklang.develop.data;

import com.blocklang.marketplace.data.ComponentRepoInfo;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ComponentRepo;

// 以后衍生数据类都以 Data 结尾，不再以 Info 结尾
public class ProjectDependenceData extends ComponentRepoInfo{

	private Integer componentRepoVersionId;
	
	public ProjectDependenceData(ComponentRepo componentRepo, ApiRepo apiRepo, Integer componentRepoVersionId) {
		super(componentRepo, apiRepo);
		this.componentRepoVersionId = componentRepoVersionId;
	}

	public Integer getComponentRepoVersionId() {
		return componentRepoVersionId;
	}

	public void setComponentRepoVersionId(Integer componentRepoVersionId) {
		this.componentRepoVersionId = componentRepoVersionId;
	}

}
