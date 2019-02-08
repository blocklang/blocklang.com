package com.blocklang.release.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;

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
		
		assertThat(projectReleaseTaskService.save(task).getId(), is(notNullValue()));
	}
}
