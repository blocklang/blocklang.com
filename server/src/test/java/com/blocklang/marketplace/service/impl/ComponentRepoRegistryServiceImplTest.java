package com.blocklang.marketplace.service.impl;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.marketplace.constant.Language;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.dao.ComponentRepoRegistryDao;
import com.blocklang.marketplace.model.ComponentRepoRegistry;
import com.blocklang.marketplace.service.ComponentRepoRegistryService;

public class ComponentRepoRegistryServiceImplTest extends AbstractServiceTest{

	@Autowired
	private ComponentRepoRegistryService componentRepoRegistryService;
	@Autowired
	private ComponentRepoRegistryDao componentRepoRegistryDao;
	
	@Test
	public void find_all_by_name_or_label_query_is_empty_no_data() {
		Page<ComponentRepoRegistry> result = componentRepoRegistryService.findAllByNameOrLabel("", PageRequest.of(0, 10));
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalPages()).isEqualTo(0);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_query_is_empty_not_include_unpublished_repo() {
		ComponentRepoRegistry registry = new ComponentRepoRegistry();
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
		componentRepoRegistryDao.save(registry);
		
		Page<ComponentRepoRegistry> result = componentRepoRegistryService.findAllByNameOrLabel("", PageRequest.of(0, 10));
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalPages()).isEqualTo(0);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_query_is_not_empty_not_include_unpublished_repo() {
		ComponentRepoRegistry registry = new ComponentRepoRegistry();
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
		componentRepoRegistryDao.save(registry);
		
		Page<ComponentRepoRegistry> result = componentRepoRegistryService.findAllByNameOrLabel("name", PageRequest.of(0, 10));
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalPages()).isEqualTo(0);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_label_is_not_empty_not_include_unpublished_repo() {
		ComponentRepoRegistry registry = new ComponentRepoRegistry();
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
		componentRepoRegistryDao.save(registry);
		
		Page<ComponentRepoRegistry> result = componentRepoRegistryService.findAllByNameOrLabel("label", PageRequest.of(0, 10));
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalPages()).isEqualTo(0);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_query_is_empty_include_published_repo() {
		ComponentRepoRegistry registry = new ComponentRepoRegistry();
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
		componentRepoRegistryDao.save(registry);
		
		Page<ComponentRepoRegistry> result = componentRepoRegistryService.findAllByNameOrLabel("", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_match_name_include_published_repo() {
		ComponentRepoRegistry registry = new ComponentRepoRegistry();
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
		componentRepoRegistryDao.save(registry);
		
		Page<ComponentRepoRegistry> result = componentRepoRegistryService.findAllByNameOrLabel("name", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_match_name_ignore_case_include_published_repo() {
		ComponentRepoRegistry registry = new ComponentRepoRegistry();
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
		componentRepoRegistryDao.save(registry);
		
		Page<ComponentRepoRegistry> result = componentRepoRegistryService.findAllByNameOrLabel("NAME", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_match_label_include_published_repo() {
		ComponentRepoRegistry registry = new ComponentRepoRegistry();
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
		componentRepoRegistryDao.save(registry);
		
		Page<ComponentRepoRegistry> result = componentRepoRegistryService.findAllByNameOrLabel("label", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_match_label_ignore_case_include_published_repo() {
		ComponentRepoRegistry registry = new ComponentRepoRegistry();
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
		componentRepoRegistryDao.save(registry);
		
		Page<ComponentRepoRegistry> result = componentRepoRegistryService.findAllByNameOrLabel("LABEL", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_match_name_like() {
		ComponentRepoRegistry registry = new ComponentRepoRegistry();
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
		componentRepoRegistryDao.save(registry);
		
		Page<ComponentRepoRegistry> result = componentRepoRegistryService.findAllByNameOrLabel("am", PageRequest.of(0, 10));
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
	}
	
	@Test
	public void find_all_by_name_or_label_order_by_last_publish_time_desc() {
		ComponentRepoRegistry registry = new ComponentRepoRegistry();
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
		componentRepoRegistryDao.save(registry);
		
		registry = new ComponentRepoRegistry();
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
		componentRepoRegistryDao.save(registry);
		
		Page<ComponentRepoRegistry> result = componentRepoRegistryService.findAllByNameOrLabel("name", PageRequest.of(0, 10, Sort.by(Direction.DESC, "lastPublishTime")));
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.hasNext()).isFalse();
		
		assertThat(result.getContent().get(0).getName()).isEqualTo("name1");
	}
}
