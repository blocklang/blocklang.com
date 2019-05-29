package com.blocklang.marketplace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.service.ComponentRepoPublishTaskService;
import com.blocklang.release.constant.ReleaseResult;

public class ComponentRepoPublishTaskServiceImplTest extends AbstractServiceTest{

	@Autowired
	private ComponentRepoPublishTaskService componentRepoPublishTaskService;
	
	@Test
	public void save_success() {
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		task.setGitUrl("a");
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(1);
		
		assertThat(componentRepoPublishTaskService.save(task).getId()).isNotNull();
	}
	
	@Test
	public void find_by_gitUrl_and_userId_no_data() {
		assertThat(componentRepoPublishTaskService.findByGitUrlAndUserId(1, "git-url")).isEmpty();
	}
	
	@Test
	public void find_by_gitUrl_and_userId_success() {
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		task.setGitUrl("git-url");
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(1);
		componentRepoPublishTaskService.save(task);
		
		assertThat(componentRepoPublishTaskService.findByGitUrlAndUserId(1, "git-url")).isPresent();
	}
}
