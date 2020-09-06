package com.blocklang.develop.data;

import com.blocklang.develop.model.ProjectBuildProfile;
import com.blocklang.develop.model.ProjectDependency;
import com.blocklang.marketplace.data.ComponentRepoInfo;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;

// 以后衍生数据类都以 Data 结尾，不再以 Info 结尾
public class ProjectDependencyData extends ComponentRepoInfo{

	private ProjectDependency dependency;
	private ProjectBuildProfile profile;

	public ProjectDependencyData(ProjectDependency dependency,
			ComponentRepo componentRepo, 
			ComponentRepoVersion componentRepoVersion, 
			ApiRepo apiRepo, 
			ApiRepoVersion apiRepoVersion) {
		super(componentRepo, componentRepoVersion, apiRepo, apiRepoVersion);
		this.dependency = dependency;
	}
	
	public ProjectDependency getDependency() {
		return dependency;
	}

	public ProjectBuildProfile getProfile() {
		return profile;
	}

	public void setProfile(ProjectBuildProfile profile) {
		this.profile = profile;
	}

}
