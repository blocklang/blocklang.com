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
import com.blocklang.develop.constant.AppType;
import com.blocklang.marketplace.constant.Language;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.data.ComponentRepoInfo;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.service.ComponentRepoService;

public class ComponentRepoServiceImplTest extends AbstractServiceTest{

	@Autowired
	private ComponentRepoService componentRepoService;
	@Autowired
	private ComponentRepoDao componentRepoDao;
	
	@Test
	public void find_all_by_name_or_label_query_is_empty_no_data() {
		Page<ComponentRepoInfo> result = componentRepoService.findAllByNameOrLabel("", PageRequest.of(0, 10));
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalPages()).isEqualTo(0);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	// 如果没有设置 publishTime, 也要能查出来
	@Test
	public void find_all_by_name_or_label_query_is_empty_not_include_unpublished_repo() {
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("name");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		componentRepoDao.save(repo);
		
		Page<ComponentRepoInfo> result = componentRepoService.findAllByNameOrLabel("", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	// 如果没有设置 publishTime, 也要能查出来
	@Test
	public void find_all_by_name_or_label_query_is_not_empty_not_include_unpublished_repo() {
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("name");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		componentRepoDao.save(repo);
		
		Page<ComponentRepoInfo> result = componentRepoService.findAllByNameOrLabel("name", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	// 如果没有设置 publishTime, 也要能查出来
	@Test
	public void find_all_by_name_or_label_label_is_not_empty_not_include_unpublished_repo() {
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("name");
		repo.setLabel("label");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		componentRepoDao.save(repo);
		
		Page<ComponentRepoInfo> result = componentRepoService.findAllByNameOrLabel("label", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_query_is_empty_include_published_repo() {
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("name");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		componentRepoDao.save(repo);
		
		Page<ComponentRepoInfo> result = componentRepoService.findAllByNameOrLabel("", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_match_name_include_published_repo() {
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("name");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		componentRepoDao.save(repo);
		
		Page<ComponentRepoInfo> result = componentRepoService.findAllByNameOrLabel("name", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_match_name_ignore_case_include_published_repo() {
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("name");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		componentRepoDao.save(repo);
		
		Page<ComponentRepoInfo> result = componentRepoService.findAllByNameOrLabel("NAME", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_match_label_include_published_repo() {
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("name");
		repo.setVersion("version");
		repo.setLabel("label");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		componentRepoDao.save(repo);
		
		Page<ComponentRepoInfo> result = componentRepoService.findAllByNameOrLabel("label", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_match_label_ignore_case_include_published_repo() {
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("name");
		repo.setLabel("label");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		componentRepoDao.save(repo);
		
		Page<ComponentRepoInfo> result = componentRepoService.findAllByNameOrLabel("LABEL", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_match_name_like() {
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("name");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		componentRepoDao.save(repo);
		
		Page<ComponentRepoInfo> result = componentRepoService.findAllByNameOrLabel("am", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_exclude_std_repo() {
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("name");
		repo.setLabel("label");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		repo.setStd(true); // 排除标准库
		componentRepoDao.save(repo);
		
		Page<ComponentRepoInfo> result = componentRepoService.findAllByNameOrLabel("name", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(0);
		assertThat(result.getTotalPages()).isEqualTo(0);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
		
		result = componentRepoService.findAllByNameOrLabel("label", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(0);
		assertThat(result.getTotalPages()).isEqualTo(0);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
		
		result = componentRepoService.findAllByNameOrLabel("", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(0);
		assertThat(result.getTotalPages()).isEqualTo(0);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_order_by_last_publish_time_desc() {
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("nameb");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now().minusSeconds(2));
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		componentRepoDao.save(repo);
		
		repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("url1");
		repo.setGitRepoWebsite("website1");
		repo.setGitRepoOwner("jack1");
		repo.setGitRepoName("repo1");
		repo.setName("namea");
		repo.setVersion("version1");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		componentRepoDao.save(repo);
		
		Page<ComponentRepoInfo> result = componentRepoService.findAllByNameOrLabel("name", PageRequest.of(0, 10, Sort.by(Direction.ASC, "name")));
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
		
		assertThat(result.getContent().get(0).getComponentRepo().getName()).isEqualTo("namea");
	}

	@Test
	public void find_user_component_repos_no_data() {
		List<ComponentRepoInfo> componentRepos = componentRepoService.findUserComponentRepos(1);
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
		repo.setAppType(AppType.WEB);
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
		repo.setAppType(AppType.WEB);
		componentRepoDao.save(repo);
		
		List<ComponentRepoInfo> componentRepos = componentRepoService.findUserComponentRepos(1);
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
		repo.setAppType(AppType.WEB);
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
		repo.setAppType(AppType.WEB);
		componentRepoDao.save(repo);
		
		List<ComponentRepoInfo> componentRepos = componentRepoService.findUserComponentRepos(createUserId);
		assertThat(componentRepos).hasSize(2).isSortedAccordingTo(Comparator.comparing(item -> item.getComponentRepo().getName()));
	}
	
	@Test
	public void find_user_component_repos_include_std() {
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
		repo.setAppType(AppType.WEB);
		repo.setStd(true); // 包含标准库
		componentRepoDao.save(repo);
		
		List<ComponentRepoInfo> componentRepos = componentRepoService.findUserComponentRepos(createUserId);
		assertThat(componentRepos).hasSize(1);
	}

	@Test
	public void exists_by_userId_and_gitRepoUrl_no_data() {
		assertThat(componentRepoService.existsByCreateUserIdAndGitRepoUrl(1, "git-url")).isFalse();
	}
	
	@Test
	public void exists_by_userId_and_gitRepoUrl_success() {
		Integer createUserId = 1;
		
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("https://a.com/jack/repo");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("b");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(createUserId);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		componentRepoDao.save(repo);
		
		assertThat(componentRepoService.existsByCreateUserIdAndGitRepoUrl(1, "https://a.com/jack/repo")).isTrue();
	}
	
	@Test
	public void find_by_id_no_data() {
		assertThat(componentRepoService.findById(1)).isEmpty();
	}
	
	@Test
	public void find_by_id_no_success() {
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(1);
		repo.setGitRepoUrl("https://a.com/jack/repo");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("b");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		ComponentRepo savedRepo = componentRepoDao.save(repo);
		
		assertThat(componentRepoService.findById(savedRepo.getId())).isPresent();
	}
}
