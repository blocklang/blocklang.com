package com.blocklang.marketplace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

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
	
	@Test
	public void find_user_publishing_no_data() {
		List<ComponentRepoPublishTask> result = componentRepoPublishTaskService.findUserPublishingTasks(1);
		assertThat(result).isEmpty();;
	}
	
	@Test
	public void find_user_publishing_tasks_started() {
		Integer createUserId = 1;
		
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		task.setGitUrl("git-url-1");
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(createUserId);
		componentRepoPublishTaskService.save(task);
		
		task = new ComponentRepoPublishTask();
		task.setGitUrl("git-url-2");
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(2);
		componentRepoPublishTaskService.save(task);
		
		List<ComponentRepoPublishTask> result = componentRepoPublishTaskService.findUserPublishingTasks(1);
		assertThat(result).hasSize(1);
	}
	
	@Test
	public void find_user_publishing_tasks_inited_or_failed_or_passed_or_canceled() {
		Integer createUserId = 1;
		
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		task.setGitUrl("git-url-1");
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.INITED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(createUserId);
		componentRepoPublishTaskService.save(task);
		
		task = new ComponentRepoPublishTask();
		task.setGitUrl("git-url-2");
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.FAILED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(createUserId);
		componentRepoPublishTaskService.save(task);
		
		task = new ComponentRepoPublishTask();
		task.setGitUrl("git-url-3");
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.PASSED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(createUserId);
		componentRepoPublishTaskService.save(task);
		
		task = new ComponentRepoPublishTask();
		task.setGitUrl("git-url-4");
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.CANCELED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(createUserId);
		componentRepoPublishTaskService.save(task);
		
		List<ComponentRepoPublishTask> result = componentRepoPublishTaskService.findUserPublishingTasks(1);
		assertThat(result).isEmpty();
	}
	
	@Test
	public void find_user_publishing_tasks_order_by_create_time_desc() {
		Integer createUserId = 1;
		
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		task.setGitUrl("git-url-1");
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now().minusSeconds(1));
		task.setCreateUserId(createUserId);
		componentRepoPublishTaskService.save(task);
		
		task = new ComponentRepoPublishTask();
		task.setGitUrl("git-url-2");
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(createUserId);
		componentRepoPublishTaskService.save(task);
		
		List<ComponentRepoPublishTask> result = componentRepoPublishTaskService.findUserPublishingTasks(createUserId);
		assertThat(result).hasSize(2).isSortedAccordingTo(Comparator.comparing(ComponentRepoPublishTask::getCreateTime).reversed());
	}
}
