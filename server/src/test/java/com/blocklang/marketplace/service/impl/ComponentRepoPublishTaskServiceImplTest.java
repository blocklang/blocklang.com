package com.blocklang.marketplace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.dao.UserDao;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.marketplace.constant.PublishType;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.service.ComponentRepoPublishTaskService;
import com.blocklang.release.constant.ReleaseResult;

public class ComponentRepoPublishTaskServiceImplTest extends AbstractServiceTest{

	@Autowired
	private ComponentRepoPublishTaskService componentRepoPublishTaskService;
	@Autowired
	private UserDao userDao;
	
	@Test
	public void save_success() {
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		task.setGitUrl("https://a.com/jack/repo");
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(1);
		
		ComponentRepoPublishTask savedTask = componentRepoPublishTaskService.save(task);
		assertThat(savedTask.getId()).isNotNull();
		assertThat(savedTask.getSeq()).isEqualTo(1);
	}
	
	@Test
	public void save_seq_increase() {
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		task.setGitUrl("https://a.com/jack/repo");
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(1);
		ComponentRepoPublishTask savedTask = componentRepoPublishTaskService.save(task);
		
		task = new ComponentRepoPublishTask();
		task.setGitUrl("https://a.com/jack/repo");
		task.setStartTime(LocalDateTime.now());
		task.setPublishType(PublishType.UPGRADE);
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(1);
		
		savedTask = componentRepoPublishTaskService.save(task);
		assertThat(savedTask.getId()).isNotNull();
		assertThat(savedTask.getSeq()).isEqualTo(2);
	}
	
	@Test
	public void save_seq_start_from_1() {
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		task.setGitUrl("https://a.com/jack/repo-A");
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(1);
		ComponentRepoPublishTask savedTask = componentRepoPublishTaskService.save(task);
		
		assertThat(savedTask.getSeq()).isEqualTo(1);
		
		task = new ComponentRepoPublishTask();
		task.setGitUrl("https://a.com/jack/repo-B");
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(1);
		
		savedTask = componentRepoPublishTaskService.save(task);
		assertThat(savedTask.getSeq()).isEqualTo(1);
	}
	
	@Test
	public void find_by_gitUrl_and_userId_no_data() {
		assertThat(componentRepoPublishTaskService.findByGitUrlAndUserId(1, "git-url")).isEmpty();
	}
	
	@Test
	public void find_by_gitUrl_and_userId_success() {
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		task.setGitUrl("https://a.com/jack/repo");
		task.setSeq(1);
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(1);
		componentRepoPublishTaskService.save(task);
		
		assertThat(componentRepoPublishTaskService.findByGitUrlAndUserId(1, "https://a.com/jack/repo")).isPresent();
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
		task.setGitUrl("https://a.com/jack/repo-1");
		task.setSeq(1);
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(createUserId);
		componentRepoPublishTaskService.save(task);
		
		task = new ComponentRepoPublishTask();
		task.setGitUrl("https://a.com/jack/repo-2");
		task.setSeq(1);
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
		task.setGitUrl("https://a.com/jack/repo-1");
		task.setSeq(1);
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.INITED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(createUserId);
		componentRepoPublishTaskService.save(task);
		
		task = new ComponentRepoPublishTask();
		task.setGitUrl("https://a.com/jack/repo-2");
		task.setSeq(1);
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.FAILED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(createUserId);
		componentRepoPublishTaskService.save(task);
		
		task = new ComponentRepoPublishTask();
		task.setGitUrl("https://a.com/jack/repo-3");
		task.setSeq(1);
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.PASSED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(createUserId);
		componentRepoPublishTaskService.save(task);
		
		task = new ComponentRepoPublishTask();
		task.setGitUrl("https://a.com/jack/repo-4");
		task.setSeq(1);
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
		task.setGitUrl("https://a.com/jack/repo-1");
		task.setSeq(1);
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now().minusSeconds(1));
		task.setCreateUserId(createUserId);
		componentRepoPublishTaskService.save(task);
		
		task = new ComponentRepoPublishTask();
		task.setGitUrl("https://a.com/jack/repo-2");
		task.setSeq(1);
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(createUserId);
		componentRepoPublishTaskService.save(task);
		
		List<ComponentRepoPublishTask> result = componentRepoPublishTaskService.findUserPublishingTasks(createUserId);
		assertThat(result).hasSize(2).isSortedAccordingTo(Comparator.comparing(ComponentRepoPublishTask::getCreateTime).reversed());
	}
	
	@Test
	public void find_by_id_get_create_user_name() {
		UserInfo user = new UserInfo();
		user.setLoginName("jack");
		user.setAvatarUrl("avatar_url");
		user.setCreateTime(LocalDateTime.now());
		UserInfo savedUser = userDao.save(user);
		
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		task.setGitUrl("https://a.com/jack/repo-1");
		task.setSeq(1);
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now().minusSeconds(1));
		task.setCreateUserId(savedUser.getId());
		assertThat(componentRepoPublishTaskService.findById(componentRepoPublishTaskService.save(task).getId()).get().getCreateUserName()).isEqualTo("jack");
	}
}
