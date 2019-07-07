package com.blocklang.marketplace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.marketplace.constant.Language;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.data.ComponentRepoResult;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.service.ComponentRepoPublishTaskService;
import com.blocklang.release.constant.ReleaseResult;

public class ComponentRepoPublishTaskServiceImplTest extends AbstractServiceTest{

	@Autowired
	private ComponentRepoPublishTaskService componentRepoPublishTaskService;
	@Autowired
	private ComponentRepoDao componentRepoDao;
	
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
	public void find_component_repos_task_not_save_and_repo_not_save() {
		Integer anotherUserId = 2;
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		task.setGitUrl("git-url");
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(anotherUserId);
		componentRepoPublishTaskService.save(task);
		
		List<ComponentRepoResult> componentRepoResultList = componentRepoPublishTaskService.findComponentRepos(1);
		assertThat(componentRepoResultList).isEmpty();
	}
	
	@Test
	public void find_component_repos_task_saved_and_repo_not_save() {
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		task.setGitUrl("git-url");
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(1);
		componentRepoPublishTaskService.save(task);
		
		List<ComponentRepoResult> componentRepoResultList = componentRepoPublishTaskService.findComponentRepos(1);
		assertThat(componentRepoResultList).hasSize(1);
		
		ComponentRepoResult first = componentRepoResultList.get(0);
		assertThat(first.getPublishTask()).isNotNull();
		assertThat(first.getComponentRepo()).isNull();
	}
	
	@Test
	public void find_component_repos_task_saved_and_repo_saved_but_not_map() {
		Integer createUserId = 1;
		
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		task.setGitUrl("git-url");
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(createUserId);
		componentRepoPublishTaskService.save(task);
		
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("name");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(createUserId);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		componentRepoDao.save(repo);
		
		List<ComponentRepoResult> componentRepoResultList = componentRepoPublishTaskService.findComponentRepos(1);
		assertThat(componentRepoResultList).hasSize(1);
		
		ComponentRepoResult first = componentRepoResultList.get(0);
		assertThat(first.getPublishTask()).isNotNull();
		assertThat(first.getComponentRepo()).isNull();
	}
	
	@Test
	public void find_component_repos_task_saved_and_repo_saved_success() {
		Integer createUserId = 1;
		
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		task.setGitUrl("git-url");
		task.setStartTime(LocalDateTime.now());
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(createUserId);
		componentRepoPublishTaskService.save(task);
		
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("git-url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("name");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(createUserId);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		componentRepoDao.save(repo);
		
		List<ComponentRepoResult> componentRepoResultList = componentRepoPublishTaskService.findComponentRepos(1);
		assertThat(componentRepoResultList).hasSize(1);
		
		ComponentRepoResult first = componentRepoResultList.get(0);
		assertThat(first.getPublishTask()).isNotNull();
		assertThat(first.getComponentRepo()).isNotNull();
	}
}
