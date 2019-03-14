package com.blocklang.release.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Comparator;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.release.constant.ReleaseMethod;
import com.blocklang.release.constant.ReleaseResult;
import com.blocklang.release.dao.AppDao;
import com.blocklang.release.dao.AppReleaseDao;
import com.blocklang.release.model.App;
import com.blocklang.release.model.AppRelease;
import com.blocklang.release.model.ProjectReleaseTask;
import com.blocklang.release.service.ProjectReleaseTaskService;

public class ProjectReleaseTaskServiceImplTest extends AbstractServiceTest{

	@Autowired
	private AppDao appDao;
	@Autowired
	private AppReleaseDao appReleaseDao;
	@Autowired
	private ProjectReleaseTaskService projectReleaseTaskService;
	
	@Test
	public void save_success() {
		ProjectReleaseTask task = new ProjectReleaseTask();
		task.setProjectId(1);
		task.setVersion("0.0.1");
		task.setTitle("title");
		task.setDescription("description");
		task.setJdkReleaseId(2);
		task.setStartTime(LocalDateTime.now());
		task.setReleaseResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(1);
		
		assertThat(projectReleaseTaskService.save(task).getId()).isNotNull();
	}

	@Test
	public void find_all_by_project_id_no_data() {
		assertThat(projectReleaseTaskService.findAllByProjectId(1)).isEmpty();
	}
	
	@Test
	public void find_all_by_project_id_order_by_create_time_desc() {
		ProjectReleaseTask task = new ProjectReleaseTask();
		task.setProjectId(1);
		task.setVersion("0.0.1");
		task.setTitle("title1");
		task.setDescription("description1");
		task.setJdkReleaseId(2);
		task.setStartTime(LocalDateTime.now());
		task.setReleaseResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now().minusSeconds(1));
		task.setCreateUserId(1);
		
		projectReleaseTaskService.save(task);
		
		task = new ProjectReleaseTask();
		task.setProjectId(1);
		task.setVersion("0.0.2");
		task.setTitle("title2");
		task.setDescription("description2");
		task.setJdkReleaseId(2);
		task.setStartTime(LocalDateTime.now());
		task.setReleaseResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(1);
		
		projectReleaseTaskService.save(task);
		
		assertThat(projectReleaseTaskService.findAllByProjectId(1))
			.hasSize(2)
			.isSortedAccordingTo(Comparator.comparing(ProjectReleaseTask::getCreateTime).reversed());
	}
	
	@Test
	public void find_all_by_project_id_fileds_not_null() {
		App app = new App();
		app.setAppName("oraclejdk11");
		app.setProjectId(1);
		app.setCreateTime(LocalDateTime.now());
		app.setCreateUserId(1);
		
		Integer appId = appDao.save(app).getId();
		
		AppRelease appRelease = new AppRelease();
		appRelease.setAppId(appId);
		appRelease.setVersion("11.0.1");
		appRelease.setTitle("oracle jdk 11.0.1");
		appRelease.setReleaseTime(LocalDateTime.now());
		appRelease.setReleaseMethod(ReleaseMethod.AUTO);
		appRelease.setCreateTime(LocalDateTime.now());
		appRelease.setCreateUserId(1);
		
		Integer jdkReleaseId = appReleaseDao.save(appRelease).getId();
		
		ProjectReleaseTask task = new ProjectReleaseTask();
		task.setProjectId(1);
		task.setVersion("0.0.1");
		task.setTitle("title1");
		task.setDescription("description1");
		task.setJdkReleaseId(jdkReleaseId);
		task.setStartTime(LocalDateTime.now());
		task.setReleaseResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now().minusSeconds(1));
		task.setCreateUserId(1);
		
		projectReleaseTaskService.save(task);
		
		assertThat(projectReleaseTaskService.findAllByProjectId(1).get(0)).hasFieldOrPropertyWithValue("jdkName", "oraclejdk11");
	}
	
}
