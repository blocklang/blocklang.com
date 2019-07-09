package com.blocklang.marketplace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.marketplace.constant.Language;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.service.ComponentRepoService;

public class ComponentRepoServiceImplTest extends AbstractServiceTest{

	@Autowired
	private ComponentRepoService componentRepoService;
	@Autowired
	private ComponentRepoDao componentRepoDao;
	
	@Test
	public void find_all_by_name_or_label_query_is_empty_no_data() {
		Page<ComponentRepo> result = componentRepoService.findAllByNameOrLabel("", PageRequest.of(0, 10));
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalPages()).isEqualTo(0);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_query_is_empty_not_include_unpublished_repo() {
		ComponentRepo registry = new ComponentRepo();
		registry.setApiRepoId(1);
		registry.setGitRepoUrl("url");
		registry.setGitRepoWebsite("website");
		registry.setGitRepoOwner("jack");
		registry.setGitRepoName("repo");
		registry.setName("name");
		registry.setVersion("version");
		registry.setCategory(RepoCategory.WIDGET);
		registry.setCreateUserId(1);
		registry.setCreateTime(LocalDateTime.now());
		registry.setLanguage(Language.TYPESCRIPT);
		componentRepoDao.save(registry);
		
		Page<ComponentRepo> result = componentRepoService.findAllByNameOrLabel("", PageRequest.of(0, 10));
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalPages()).isEqualTo(0);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_query_is_not_empty_not_include_unpublished_repo() {
		ComponentRepo registry = new ComponentRepo();
		registry.setApiRepoId(1);
		registry.setGitRepoUrl("url");
		registry.setGitRepoWebsite("website");
		registry.setGitRepoOwner("jack");
		registry.setGitRepoName("repo");
		registry.setName("name");
		registry.setVersion("version");
		registry.setCategory(RepoCategory.WIDGET);
		registry.setCreateUserId(1);
		registry.setCreateTime(LocalDateTime.now());
		registry.setLanguage(Language.TYPESCRIPT);
		componentRepoDao.save(registry);
		
		Page<ComponentRepo> result = componentRepoService.findAllByNameOrLabel("name", PageRequest.of(0, 10));
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalPages()).isEqualTo(0);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_label_is_not_empty_not_include_unpublished_repo() {
		ComponentRepo registry = new ComponentRepo();
		registry.setApiRepoId(1);
		registry.setGitRepoUrl("url");
		registry.setGitRepoWebsite("website");
		registry.setGitRepoOwner("jack");
		registry.setGitRepoName("repo");
		registry.setName("name");
		registry.setLabel("label");
		registry.setVersion("version");
		registry.setCategory(RepoCategory.WIDGET);
		registry.setCreateUserId(1);
		registry.setCreateTime(LocalDateTime.now());
		registry.setLanguage(Language.TYPESCRIPT);
		componentRepoDao.save(registry);
		
		Page<ComponentRepo> result = componentRepoService.findAllByNameOrLabel("label", PageRequest.of(0, 10));
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalPages()).isEqualTo(0);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_query_is_empty_include_published_repo() {
		ComponentRepo registry = new ComponentRepo();
		registry.setApiRepoId(1);
		registry.setGitRepoUrl("url");
		registry.setGitRepoWebsite("website");
		registry.setGitRepoOwner("jack");
		registry.setGitRepoName("repo");
		registry.setName("name");
		registry.setVersion("version");
		registry.setCategory(RepoCategory.WIDGET);
		registry.setCreateUserId(1);
		registry.setCreateTime(LocalDateTime.now());
		registry.setLastPublishTime(LocalDateTime.now());
		registry.setLanguage(Language.TYPESCRIPT);
		componentRepoDao.save(registry);
		
		Page<ComponentRepo> result = componentRepoService.findAllByNameOrLabel("", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_match_name_include_published_repo() {
		ComponentRepo registry = new ComponentRepo();
		registry.setApiRepoId(1);
		registry.setGitRepoUrl("url");
		registry.setGitRepoWebsite("website");
		registry.setGitRepoOwner("jack");
		registry.setGitRepoName("repo");
		registry.setName("name");
		registry.setVersion("version");
		registry.setCategory(RepoCategory.WIDGET);
		registry.setCreateUserId(1);
		registry.setCreateTime(LocalDateTime.now());
		registry.setLastPublishTime(LocalDateTime.now());
		registry.setLanguage(Language.TYPESCRIPT);
		componentRepoDao.save(registry);
		
		Page<ComponentRepo> result = componentRepoService.findAllByNameOrLabel("name", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_match_name_ignore_case_include_published_repo() {
		ComponentRepo registry = new ComponentRepo();
		registry.setApiRepoId(1);
		registry.setGitRepoUrl("url");
		registry.setGitRepoWebsite("website");
		registry.setGitRepoOwner("jack");
		registry.setGitRepoName("repo");
		registry.setName("name");
		registry.setVersion("version");
		registry.setCategory(RepoCategory.WIDGET);
		registry.setCreateUserId(1);
		registry.setCreateTime(LocalDateTime.now());
		registry.setLastPublishTime(LocalDateTime.now());
		registry.setLanguage(Language.TYPESCRIPT);
		componentRepoDao.save(registry);
		
		Page<ComponentRepo> result = componentRepoService.findAllByNameOrLabel("NAME", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_match_label_include_published_repo() {
		ComponentRepo registry = new ComponentRepo();
		registry.setApiRepoId(1);
		registry.setGitRepoUrl("url");
		registry.setGitRepoWebsite("website");
		registry.setGitRepoOwner("jack");
		registry.setGitRepoName("repo");
		registry.setName("name");
		registry.setVersion("version");
		registry.setLabel("label");
		registry.setCategory(RepoCategory.WIDGET);
		registry.setCreateUserId(1);
		registry.setCreateTime(LocalDateTime.now());
		registry.setLastPublishTime(LocalDateTime.now());
		registry.setLanguage(Language.TYPESCRIPT);
		componentRepoDao.save(registry);
		
		Page<ComponentRepo> result = componentRepoService.findAllByNameOrLabel("label", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_match_label_ignore_case_include_published_repo() {
		ComponentRepo registry = new ComponentRepo();
		registry.setApiRepoId(1);
		registry.setGitRepoUrl("url");
		registry.setGitRepoWebsite("website");
		registry.setGitRepoOwner("jack");
		registry.setGitRepoName("repo");
		registry.setName("name");
		registry.setLabel("label");
		registry.setVersion("version");
		registry.setCategory(RepoCategory.WIDGET);
		registry.setCreateUserId(1);
		registry.setCreateTime(LocalDateTime.now());
		registry.setLastPublishTime(LocalDateTime.now());
		registry.setLanguage(Language.TYPESCRIPT);
		componentRepoDao.save(registry);
		
		Page<ComponentRepo> result = componentRepoService.findAllByNameOrLabel("LABEL", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_match_name_like() {
		ComponentRepo registry = new ComponentRepo();
		registry.setApiRepoId(1);
		registry.setGitRepoUrl("url");
		registry.setGitRepoWebsite("website");
		registry.setGitRepoOwner("jack");
		registry.setGitRepoName("repo");
		registry.setName("name");
		registry.setVersion("version");
		registry.setCategory(RepoCategory.WIDGET);
		registry.setCreateUserId(1);
		registry.setCreateTime(LocalDateTime.now());
		registry.setLastPublishTime(LocalDateTime.now());
		registry.setLanguage(Language.TYPESCRIPT);
		componentRepoDao.save(registry);
		
		Page<ComponentRepo> result = componentRepoService.findAllByNameOrLabel("am", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_order_by_last_publish_time_desc() {
		ComponentRepo registry = new ComponentRepo();
		registry.setApiRepoId(1);
		registry.setGitRepoUrl("url");
		registry.setGitRepoWebsite("website");
		registry.setGitRepoOwner("jack");
		registry.setGitRepoName("repo");
		registry.setName("name");
		registry.setVersion("version");
		registry.setCategory(RepoCategory.WIDGET);
		registry.setCreateUserId(1);
		registry.setCreateTime(LocalDateTime.now());
		registry.setLastPublishTime(LocalDateTime.now().minusSeconds(2));
		registry.setLanguage(Language.TYPESCRIPT);
		componentRepoDao.save(registry);
		
		registry = new ComponentRepo();
		registry.setApiRepoId(1);
		registry.setGitRepoUrl("url1");
		registry.setGitRepoWebsite("website1");
		registry.setGitRepoOwner("jack1");
		registry.setGitRepoName("repo1");
		registry.setName("name1");
		registry.setVersion("version1");
		registry.setCategory(RepoCategory.WIDGET);
		registry.setCreateUserId(1);
		registry.setCreateTime(LocalDateTime.now());
		registry.setLastPublishTime(LocalDateTime.now());
		registry.setLanguage(Language.TYPESCRIPT);
		componentRepoDao.save(registry);
		
		Page<ComponentRepo> result = componentRepoService.findAllByNameOrLabel("name", PageRequest.of(0, 10, Sort.by(Direction.DESC, "lastPublishTime")));
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
		
		assertThat(result.getContent().get(0).getName()).isEqualTo("name1");
	}

	@Test
	public void find_user_component_repos_no_data() {
		List<ComponentRepo> componentRepos = componentRepoService.findUserComponentRepos(1);
		assertThat(componentRepos).isEmpty();
	}
	
	@Test
	public void find_user_component_repos_success() {
		Integer createUserId = 1;
		
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
		
		repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("url-2");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("name-2");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(2);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		componentRepoDao.save(repo);
		
		List<ComponentRepo> componentRepos = componentRepoService.findUserComponentRepos(1);
		assertThat(componentRepos).hasSize(1);
	}
	
	@Test
	public void find_user_component_repos_sort_by_name_asc() {
		Integer createUserId = 1;
		
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("b");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(createUserId);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		componentRepoDao.save(repo);
		
		repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("url-2");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("a");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(createUserId);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		componentRepoDao.save(repo);
		
		List<ComponentRepo> componentRepos = componentRepoService.findUserComponentRepos(createUserId);
		assertThat(componentRepos).hasSize(2).isSortedAccordingTo(Comparator.comparing(ComponentRepo::getName));
	}

}
