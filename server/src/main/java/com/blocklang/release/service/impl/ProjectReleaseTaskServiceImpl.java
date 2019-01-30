package com.blocklang.release.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.release.dao.ProjectReleaseTaskDao;
import com.blocklang.release.model.ProjectReleaseTask;
import com.blocklang.release.service.ProjectReleaseTaskService;

@Service
public class ProjectReleaseTaskServiceImpl implements ProjectReleaseTaskService {

	@Autowired
	private ProjectReleaseTaskDao projectReleaseTaskDao;
	
	@Override
	public ProjectReleaseTask save(ProjectReleaseTask projectReleaseTask) {
		return projectReleaseTaskDao.save(projectReleaseTask);
	}

}
