package com.blocklang.marketplace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.constant.RepoType;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.data.ComponentRepoInfo;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.service.ComponentRepoService;

public class ComponentRepoServiceImplTest extends AbstractServiceTest{

	@Autowired
	private ComponentRepoService componentRepoService;
	@Autowired
	private ComponentRepoDao componentRepoDao;
	@MockBean
	private PropertyService propertyService;
	
	@Test
	public void findAllByGitRepoName_query_is_empty_no_data() {
		Page<ComponentRepoInfo> result = componentRepoService.findAllByGitRepoName("", PageRequest.of(0, 10));
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalPages()).isEqualTo(0);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void findAllByGitRepoName_match_ignore_case() {
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl("https://github.con/you/you-repo.git");
		repo.setGitRepoWebsite("github.com");
		repo.setGitRepoOwner("you");
		repo.setGitRepoName("you-repo");
		repo.setRepoType(RepoType.IDE);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		componentRepoDao.save(repo);
		
		Page<ComponentRepoInfo> result = componentRepoService.findAllByGitRepoName("YOU-REPO", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void findAllByGitRepoName_like() {
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl("https://github.con/you/you-repo.git");
		repo.setGitRepoWebsite("github.com");
		repo.setGitRepoOwner("you");
		repo.setGitRepoName("you-repo");
		repo.setRepoType(RepoType.IDE);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		componentRepoDao.save(repo);
		
		Page<ComponentRepoInfo> result = componentRepoService.findAllByGitRepoName("re", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void findAllByGitRepoName_exclude_std_repo() {
		String gitRepoUrl = "https://github.com/you/you-repo.git";
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl(gitRepoUrl);
		repo.setGitRepoWebsite("github.com");
		repo.setGitRepoOwner("you");
		repo.setGitRepoName("you-repo");
		repo.setRepoType(RepoType.IDE);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		componentRepoDao.save(repo);
		
		when(propertyService.findStringValue(eq(CmPropKey.STD_WIDGET_IDE_GIT_URL), any())).thenReturn(gitRepoUrl);
		
		Page<ComponentRepoInfo> result = componentRepoService.findAllByGitRepoName("you-repo", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(0);
		assertThat(result.getTotalPages()).isEqualTo(0);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
		
		result = componentRepoService.findAllByGitRepoName("", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(0);
		assertThat(result.getTotalPages()).isEqualTo(0);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void findAllByGitRepoName_order_by_owner_repo_asc() {
		String gitRepoUrl = "https://github.com/you/you-repo-a.git";
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl(gitRepoUrl);
		repo.setGitRepoWebsite("github.com");
		repo.setGitRepoOwner("you");
		repo.setGitRepoName("you-repo-a");
		repo.setRepoType(RepoType.IDE);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		componentRepoDao.save(repo);
		
		String gitRepoUrl1 = "https://github.com/you/you-repo-b.git";
		repo = new ComponentRepo();
		repo.setGitRepoUrl(gitRepoUrl1);
		repo.setGitRepoWebsite("github.com");
		repo.setGitRepoOwner("you");
		repo.setGitRepoName("you-repo-b");
		repo.setRepoType(RepoType.IDE);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		componentRepoDao.save(repo);
		
		Pageable pageable = PageRequest.of(0, 10, Sort.by(Direction.ASC, "gitRepoOwner", "gitRepoName"));
		Page<ComponentRepoInfo> result = componentRepoService.findAllByGitRepoName("repo", pageable);
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
		
		assertThat(result.getContent().get(0).getComponentRepo().getGitRepoName()).isEqualTo("you-repo-a");
	}

	@Test
	public void findUserComponentRepos_no_data() {
		Integer createUserId = 1;
		List<ComponentRepoInfo> componentRepos = componentRepoService.findUserComponentRepos(createUserId);
		assertThat(componentRepos).isEmpty();
	}
	
	@Test
	public void findUserComponentRepos_success() {
		Integer createUserId = 1;
		
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl("https://github.con/you/you-repo.git");
		repo.setGitRepoWebsite("github.com");
		repo.setGitRepoOwner("you");
		repo.setGitRepoName("you-repo");
		repo.setRepoType(RepoType.IDE);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(createUserId);
		repo.setCreateTime(LocalDateTime.now());
		componentRepoDao.save(repo);
		
		repo = new ComponentRepo();
		repo.setGitRepoUrl("https://github.con/you-2/you-repo-2.git");
		repo.setGitRepoWebsite("github.com");
		repo.setGitRepoOwner("you-2");
		repo.setGitRepoName("you-repo-2");
		repo.setRepoType(RepoType.IDE);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(2);
		repo.setCreateTime(LocalDateTime.now());
		componentRepoDao.save(repo);
		
		List<ComponentRepoInfo> componentRepos = componentRepoService.findUserComponentRepos(createUserId);
		assertThat(componentRepos).hasSize(1);
	}
	
	@Test
	public void findUserComponentRepos_order_by_owner_repo_asc() {
		Integer createUserId = 1;
		
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl("https://github.con/you/you-repo.git");
		repo.setGitRepoWebsite("github.com");
		repo.setGitRepoOwner("you");
		repo.setGitRepoName("you-repo");
		repo.setRepoType(RepoType.IDE);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(createUserId);
		repo.setCreateTime(LocalDateTime.now());
		componentRepoDao.save(repo);
		
		repo = new ComponentRepo();
		repo.setGitRepoUrl("https://github.con/you/you-repo-2.git");
		repo.setGitRepoWebsite("github.com");
		repo.setGitRepoOwner("you");
		repo.setGitRepoName("you-repo-2");
		repo.setRepoType(RepoType.IDE);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(createUserId);
		repo.setCreateTime(LocalDateTime.now());
		componentRepoDao.save(repo);
		
		List<ComponentRepoInfo> componentRepos = componentRepoService.findUserComponentRepos(createUserId);
		assertThat(componentRepos)
			.hasSize(2)
			.isSortedAccordingTo(Comparator.comparing(item -> item.getComponentRepo().getGitRepoName()));
	}
	
	@Test
	public void findUserComponentRepos_include_std() {
		Integer createUserId = 1;
		
		String gitRepoUrl = "https://github.com/you/you-repo.git";
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl(gitRepoUrl);
		repo.setGitRepoWebsite("github.com");
		repo.setGitRepoOwner("you");
		repo.setGitRepoName("you-repo");
		repo.setRepoType(RepoType.IDE);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(createUserId);
		repo.setCreateTime(LocalDateTime.now());
		componentRepoDao.save(repo);
		
		when(propertyService.findStringValue(eq(CmPropKey.STD_WIDGET_IDE_GIT_URL), any())).thenReturn(gitRepoUrl);
		
		List<ComponentRepoInfo> componentRepos = componentRepoService.findUserComponentRepos(createUserId);
		assertThat(componentRepos).hasSize(1);
	}

	@Test
	public void existsByCreateUserIdAndGitRepoUrl_not_exist() {
		assertThat(componentRepoService.existsByCreateUserIdAndGitRepoUrl(1, "git-url")).isFalse();
	}
	
	@Test
	public void existsByCreateUserIdAndGitRepoUrl_exist() {
		Integer createUserId = 1;
		String gitRepoUrl = "https://github.com/you/you-repo.git";
		
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl(gitRepoUrl);
		repo.setGitRepoWebsite("github.com");
		repo.setGitRepoOwner("you");
		repo.setGitRepoName("you-repo");
		repo.setRepoType(RepoType.IDE);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(createUserId);
		repo.setCreateTime(LocalDateTime.now());
		componentRepoDao.save(repo);
		
		assertThat(componentRepoService.existsByCreateUserIdAndGitRepoUrl(createUserId, gitRepoUrl)).isTrue();
	}
	
	@Test
	public void find_by_id_no_data() {
		assertThat(componentRepoService.findById(1)).isEmpty();
	}
	
	@Test
	public void find_by_id_success() {
		Integer createUserId = 1;
		String gitRepoUrl = "https://github.com/you/you-repo.git";
		
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl(gitRepoUrl);
		repo.setGitRepoWebsite("github.com");
		repo.setGitRepoOwner("you");
		repo.setGitRepoName("you-repo");
		repo.setRepoType(RepoType.IDE);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(createUserId);
		repo.setCreateTime(LocalDateTime.now());
		ComponentRepo savedRepo = componentRepoDao.save(repo);
		
		assertThat(componentRepoService.findById(savedRepo.getId())).isPresent();
	}
}
