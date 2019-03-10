package com.blocklang.release.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Comparator;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.release.constant.ReleaseResult;
import com.blocklang.release.model.ProjectReleaseTask;
import com.blocklang.release.service.ProjectReleaseTaskService;

public class ProjectReleaseTaskServiceImplTest extends AbstractServiceTest{

	@Autowired
	private ProjectReleaseTaskService projectReleaseTaskService;
	
	@Test
	public void save_success() {
		ProjectReleaseTask task = new ProjectReleaseTask();
		task.setProjectId(1);
		task.setVersion("0.0.1");
		task.setTitle("title");
		task.setDescription("description");
		task.setJdkAppId(2);
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
		task.setJdkAppId(2);
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
		task.setJdkAppId(2);
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
		ProjectReleaseTask task = new ProjectReleaseTask();
		task.setProjectId(1);
		task.setVersion("0.0.1");
		task.setTitle("title1");
		task.setDescription("description1");
		task.setJdkAppId(2);
		task.setStartTime(LocalDateTime.now());
		task.setReleaseResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now().minusSeconds(1));
		task.setCreateUserId(1);
		
		projectReleaseTaskService.save(task);
		
		assertThat(projectReleaseTaskService.findAllByProjectId(1).get(0)).hasFieldOrPropertyWithValue("jdkName", "oraclejdk11");
	}
	
}
