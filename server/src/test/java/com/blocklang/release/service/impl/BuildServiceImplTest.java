package com.blocklang.release.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.develop.model.Project;
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
	
	@Test
	public void build_success() throws IOException {
		Project project = new Project();
		project.setId(1);
		project.setName("demo_project");
		project.setCreateUserName("jack");
		
		App app = new App();
		app.setProjectId(1);
		app.setAppName("demo_project");
		app.setCreateTime(LocalDateTime.now());
		app.setCreateUserId(1);
		appDao.save(app);
		
		ProjectReleaseTask task = new ProjectReleaseTask();
		task.setProjectId(1);
		task.setTitle("title");
		task.setDescription("description");
		task.setJdkAppId(2);
		task.setVersion("0.0.1");
		task.setStartTime(LocalDateTime.now());
		task.setReleaseResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(1);
		ProjectReleaseTask savedTask = projectReleaseDao.save(task);
		
		buildService.build(project, savedTask);
	}
	
}
