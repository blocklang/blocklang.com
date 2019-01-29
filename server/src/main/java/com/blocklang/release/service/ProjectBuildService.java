package com.blocklang.release.service;

import com.blocklang.release.model.ProjectBuild;

public interface ProjectBuildService {

	ProjectBuild save(ProjectBuild projectBuild);

	void update(ProjectBuild projectBuild);

}
