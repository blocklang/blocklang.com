package com.blocklang.release.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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

	@Override
	public List<ProjectReleaseTask> findAllByProjectId(Integer projectId) {
		Pageable pageable = PageRequest.of(0, 100, Sort.by(Direction.DESC, "createTime"));
		return projectReleaseTaskDao.findAllByProjectId(projectId, pageable);
	}

}
