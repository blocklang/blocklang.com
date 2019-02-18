package com.blocklang.develop.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.git.GitUtils;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.service.UserService;
import com.blocklang.core.util.IdGenerator;
import com.blocklang.develop.dao.ProjectDao;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectContext;
import com.blocklang.develop.service.ProjectService;
import com.blocklang.release.dao.AppDao;
import com.blocklang.release.model.App;

@Service
public class ProjectServiceImpl implements ProjectService {
	
	private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

	@Autowired
	private ProjectDao projectDao;
	@Autowired
	private AppDao appDao;
	@Autowired
	private PropertyService propertyService;
	
	// 如果需要缓存时，使用 service，不要使用 dao
	// 因为只会为 service 添加缓存
	@Autowired
	private UserService userService;
	
	@Override
	public Optional<Project> find(String userName, String projectName) {
		return userService.findByLoginName(userName).flatMap(user -> {
			return projectDao.findByCreateUserIdAndName(user.getId(), projectName);
		});
	}

	@Override
	public Project create(UserInfo user, Project project) {
		
		Project savedProject = projectDao.save(project);
		
		App app = new App();
		String appName = "@" + user.getLoginName() + "/" + project.getName();
		app.setAppName(appName);
		app.setProjectId(savedProject.getId());
		app.setCreateUserId(project.getCreateUserId());
		app.setCreateTime(LocalDateTime.now());
		app.setRegistrationToken(IdGenerator.shortUuid());
		appDao.save(app);
		
		propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).ifPresent(rootDir -> {
			ProjectContext context = new ProjectContext(user.getLoginName(), project.getName(), rootDir);
			try {
				GitUtils.init(context.getGitRepositoryDirectory(), user.getLoginName(), user.getEmail());
			}catch (RuntimeException e) {
				logger.error(String.format("为项目 {} 初始创建 git 仓库失败", appName), e);
			}
		});
		
		return savedProject;
	}

}
