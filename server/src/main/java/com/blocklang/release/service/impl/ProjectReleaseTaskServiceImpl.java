package com.blocklang.release.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.blocklang.core.service.UserService;
import com.blocklang.release.dao.AppDao;
import com.blocklang.release.dao.AppReleaseDao;
import com.blocklang.release.dao.ProjectReleaseTaskDao;
import com.blocklang.release.model.ProjectReleaseTask;
import com.blocklang.release.service.ProjectReleaseTaskService;

@Service
public class ProjectReleaseTaskServiceImpl implements ProjectReleaseTaskService {

	@Autowired
	private ProjectReleaseTaskDao projectReleaseTaskDao;
	@Autowired
	private AppReleaseDao appReleaseDao;
	@Autowired
	private AppDao appDao;
	@Autowired
	private UserService userService;
	
	@Override
	public ProjectReleaseTask save(ProjectReleaseTask projectReleaseTask) {
		return projectReleaseTaskDao.save(projectReleaseTask);
	}

	@Override
	public List<ProjectReleaseTask> findAllByProjectId(Integer projectId) {
		Pageable pageable = PageRequest.of(0, 100, Sort.by(Direction.DESC, "createTime"));
		List<ProjectReleaseTask> result = projectReleaseTaskDao.findAllByProjectId(projectId, pageable);
		result.forEach(task -> {
			appReleaseDao.findById(task.getJdkReleaseId()).flatMap(release -> {
				task.setJdkVersion(release.getVersion());
				return appDao.findById(release.getAppId());
			}).ifPresent(app -> {
				task.setJdkName(app.getAppName());
			});
			userService.findById(task.getCreateUserId()).ifPresent(user -> {
				task.setCreateUserName(user.getLoginName());
				task.setCreateUserAvatarUrl(user.getAvatarUrl());
			});
		});
		return result;
	}

	@Override
	public Long count(Integer projectId) {
		return projectReleaseTaskDao.countByProjectId(projectId);
	}

}
