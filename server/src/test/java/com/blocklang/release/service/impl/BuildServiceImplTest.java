package com.blocklang.release.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.blocklang.core.service.PropertyService;
import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.develop.model.Repository;
import com.blocklang.release.constant.ReleaseResult;
import com.blocklang.release.dao.AppDao;
import com.blocklang.release.dao.ProjectReleaseTaskDao;
import com.blocklang.release.model.App;
import com.blocklang.release.model.ProjectReleaseTask;
import com.blocklang.release.service.BuildService;

public class BuildServiceImplTest extends AbstractServiceTest{
	
	@Autowired
	private AppDao appDao;
	@Autowired
	private ProjectReleaseTaskDao projectReleaseDao;
	@Autowired
	private BuildService buildService;
	@MockBean
	private PropertyService propertyService;
	
	// TODO: 解决 linux 上出现 java.nio.file.AccessDeniedException: /home/blocklang 的问题
	// 让此测试用例在 linux 上通过。
	@Disabled
	@Test
	public void build_success() throws IOException {
		Integer projectId = Integer.MAX_VALUE;
		Repository project = new Repository();
		project.setId(projectId);
		project.setName("demo_project");
		project.setCreateUserName("jack");
		
		App app = new App();
		app.setProjectId(projectId);
		app.setAppName("demo_project");
		app.setCreateTime(LocalDateTime.now());
		app.setCreateUserId(1);
		appDao.save(app);
		
		ProjectReleaseTask task = new ProjectReleaseTask();
		task.setProjectId(projectId);
		task.setTitle("title");
		task.setDescription("description");
		task.setJdkReleaseId(2);
		task.setVersion("0.0.1");
		task.setStartTime(LocalDateTime.now());
		task.setReleaseResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(1);
		ProjectReleaseTask savedTask = projectReleaseDao.save(task);
		
		buildService.build(project, savedTask);
	}
	
}
