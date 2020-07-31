package com.blocklang.develop.data;

import com.blocklang.develop.model.ProjectBuildProfile;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.marketplace.data.ComponentRepoInfo;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;

// 以后衍生数据类都以 Data 结尾，不再以 Info 结尾
public class ProjectDependenceData extends ComponentRepoInfo{

	private ProjectDependence dependence;
	private ProjectBuildProfile profile;

	public ProjectDependenceData(ProjectDependence dependence,
			ComponentRepo componentRepo, 
			ComponentRepoVersion componentRepoVersion, 
			ApiRepo apiRepo, 
			ApiRepoVersion apiRepoVersion) {
		super(componentRepo, componentRepoVersion, apiRepo, apiRepoVersion);
		this.dependence = dependence;
	}
	
	public ProjectDependence getDependence() {
		return dependence;
	}

	public ProjectBuildProfile getProfile() {
		return profile;
	}

	public void setProfile(ProjectBuildProfile profile) {
		this.profile = profile;
	}

}
