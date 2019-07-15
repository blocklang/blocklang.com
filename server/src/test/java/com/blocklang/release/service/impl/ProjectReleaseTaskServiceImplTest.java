package com.blocklang.release.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.dao.UserDao;
import com.blocklang.core.model.UserInfo;
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
	private UserDao userDao;
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
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		
		App app = new App();
		app.setAppName("oraclejdk");
		app.setProjectId(1);
		app.setCreateTime(LocalDateTime.now());
		app.setCreateUserId(1);
		
		Integer appId = appDao.save(app).getId();
		
		AppRelease appRelease = new AppRelease();
		appRelease.setAppId(appId);
		appRelease.setVersion("11.0.2");
		appRelease.setTitle("oracle jdk 11.0.2");
		appRelease.setReleaseTime(LocalDateTime.now());
		appRelease.setReleaseMethod(ReleaseMethod.AUTO);
		appRelease.setCreateTime(LocalDateTime.now());
		appRelease.setCreateUserId(userId);
		
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
		task.setCreateUserId(userId);
		
		projectReleaseTaskService.save(task);
		
		assertThat(projectReleaseTaskService.findAllByProjectId(1).get(0))
			.hasFieldOrPropertyWithValue("jdkName", "oraclejdk")
			.hasFieldOrPropertyWithValue("jdkVersion", "11.0.2")
			.hasFieldOrPropertyWithValue("createUserName", "user_name")
			.hasFieldOrPropertyWithValue("createUserAvatarUrl", "avatar_url");
	}
	
	@Test
	public void count_equal_to_0() {
		assertThat(projectReleaseTaskService.count(1)).isEqualTo(0);
	}
	
	@Test
	public void count_equal_to_1() {
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
		
		assertThat(projectReleaseTaskService.count(1)).isEqualTo(1);
	}
	
	@Test
	public void find_by_project_id_and_version_no_data() {
		assertThat(projectReleaseTaskService.findByProjectIdAndVersion(Integer.MAX_VALUE, "0.1.0")).isEmpty();
	}
	
	@Test
	public void find_by_project_id_and_version_success() {
		Integer projectId = Integer.MAX_VALUE;
		
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		
		App app = new App();
		app.setAppName("oraclejdk");
		app.setProjectId(projectId);
		app.setCreateTime(LocalDateTime.now());
		app.setCreateUserId(1);
		
		Integer appId = appDao.save(app).getId();
		
		AppRelease appRelease = new AppRelease();
		appRelease.setAppId(appId);
		appRelease.setVersion("11.0.2");
		appRelease.setTitle("oracle jdk 11.0.2");
		appRelease.setReleaseTime(LocalDateTime.now());
		appRelease.setReleaseMethod(ReleaseMethod.AUTO);
		appRelease.setCreateTime(LocalDateTime.now());
		appRelease.setCreateUserId(userId);
		
		Integer jdkReleaseId = appReleaseDao.save(appRelease).getId();
		
		ProjectReleaseTask task = new ProjectReleaseTask();
		task.setProjectId(projectId);
		task.setVersion("0.0.1");
		task.setTitle("title1");
		task.setDescription("description1");
		task.setJdkReleaseId(jdkReleaseId);
		task.setStartTime(LocalDateTime.now());
		task.setReleaseResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now().minusSeconds(1));
		task.setCreateUserId(userId);
		
		projectReleaseTaskService.save(task);
		
		Optional<ProjectReleaseTask> taskOption = projectReleaseTaskService.findByProjectIdAndVersion(projectId, "0.0.1");
		assertThat(taskOption).isPresent();
		assertThat(taskOption.get())
			.hasFieldOrPropertyWithValue("jdkName", "oraclejdk")
			.hasFieldOrPropertyWithValue("jdkVersion", "11.0.2")
			.hasFieldOrPropertyWithValue("createUserName", "user_name")
			.hasFieldOrPropertyWithValue("createUserAvatarUrl", "avatar_url");
	}
}
