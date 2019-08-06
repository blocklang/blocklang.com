package com.blocklang.develop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.dao.ProjectBuildProfileDao;
import com.blocklang.develop.dao.ProjectDependenceDao;
import com.blocklang.develop.model.ProjectBuildProfile;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.develop.service.ProjectDependenceService;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;

public class ProjectDependenceServiceImplTest extends AbstractServiceTest{

	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	@Autowired
	private ProjectDependenceDao projectDependenceDao;
	@Autowired
	private ProjectDependenceService projectDependenceService;
	@Autowired
	private ProjectBuildProfileDao projectBuildProfileDao;

	@Test
	public void dev_dependence_exists_that_not_exists() {
		assertThat(projectDependenceService.devDependenceExists(1, 1)).isFalse();
	}
	
	@Test
	public void dev_dependence_exists_that_exists() {
		Integer projectId = 1;
		Integer componentRepoId = 2;
		// 创建组件仓库的版本信息
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setComponentRepoId(componentRepoId);
		version.setVersion("0.1.0");
		version.setApiRepoVersionId(3);
		version.setCreateUserId(11);
		version.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(version);
		// 为项目添加一个依赖
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(projectId);
		dependence.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependence.setCreateUserId(11);
		dependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(dependence);
		
		assertThat(projectDependenceService.devDependenceExists(projectId, componentRepoId)).isTrue();
	}
	
	@Test
	public void build_dependence_exists_that_not_exists() {
		assertThat(projectDependenceService.buildDependenceExists(1, 1, AppType.WEB, ProjectBuildProfile.DEFAULT_PROFILE_NAME)).isFalse();
	}
	
	@Test
	public void build_dependence_exists_that_exists() {
		Integer projectId = 1;
		Integer componentRepoId = 2;
		// 创建组件仓库的版本信息
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setComponentRepoId(componentRepoId);
		version.setVersion("0.1.0");
		version.setApiRepoVersionId(3);
		version.setCreateUserId(11);
		version.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(version);
		// 为项目的 web 页面添加一个默认的 Profile
		ProjectBuildProfile profile = new ProjectBuildProfile();
		profile.setProjectId(projectId);
		profile.setAppType(AppType.WEB);
		profile.setName(ProjectBuildProfile.DEFAULT_PROFILE_NAME);
		profile.setCreateUserId(11);
		profile.setCreateTime(LocalDateTime.now());
		projectBuildProfileDao.save(profile);
		
		// 为项目添加一个依赖
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(projectId);
		dependence.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependence.setProfileId(profile.getId());
		dependence.setCreateUserId(11);
		dependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(dependence);
		
		assertThat(projectDependenceService.buildDependenceExists(projectId, componentRepoId, AppType.WEB, ProjectBuildProfile.DEFAULT_PROFILE_NAME)).isTrue();
	}
	
	@Test
	public void save_failed_component_repo_versions_not_found() {
		Integer projectId = 1;
		Integer componentRepoId = 2;
		Integer userId = 3;
		
		ComponentRepo componentRepo = new ComponentRepo();
		componentRepo.setId(componentRepoId);
		componentRepo.setAppType(AppType.WEB);
		
		assertThat(countRowsInTable("PROJECT_BUILD_PROFILE")).isEqualTo(0);
		assertThat(countRowsInTable("PROJECT_DEPENDENCE")).isEqualTo(0);
		
		ProjectDependence savedDependence = projectDependenceService.save(projectId, componentRepo, userId);
		
		assertThat(savedDependence).isNull();
		
		assertThat(countRowsInTable("PROJECT_BUILD_PROFILE")).isEqualTo(0);
		assertThat(countRowsInTable("PROJECT_DEPENDENCE")).isEqualTo(0);
	}
	
	@Test
	public void save_success() {
		Integer projectId = 1;
		Integer componentRepoId = 2;
		Integer userId = 3;
		
		ComponentRepo componentRepo = new ComponentRepo();
		componentRepo.setId(componentRepoId);
		componentRepo.setAppType(AppType.WEB);
		
		// 创建组件仓库的版本信息
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setComponentRepoId(componentRepoId);
		version.setVersion("0.1.0");
		version.setApiRepoVersionId(3);
		version.setCreateUserId(11);
		version.setCreateTime(LocalDateTime.now());
		componentRepoVersionDao.save(version);
		
		version = new ComponentRepoVersion();
		version.setComponentRepoId(componentRepoId);
		version.setVersion("0.2.0");
		version.setApiRepoVersionId(3);
		version.setCreateUserId(11);
		version.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion2 = componentRepoVersionDao.save(version);
		
		assertThat(countRowsInTable("PROJECT_BUILD_PROFILE")).isEqualTo(0);
		assertThat(countRowsInTable("PROJECT_DEPENDENCE")).isEqualTo(0);
		
		ProjectDependence savedDependence = projectDependenceService.save(projectId, componentRepo, userId);
		
		assertThat(savedDependence.getProjectId()).isEqualTo(projectId);
		// 获取最新版本号
		assertThat(savedDependence.getComponentRepoVersionId()).isEqualTo(savedComponentRepoVersion2.getId());
		assertThat(savedDependence.getProfileId()).isNotNull();
		
		assertThat(countRowsInTable("PROJECT_BUILD_PROFILE")).isEqualTo(1);
		assertThat(countRowsInTable("PROJECT_DEPENDENCE")).isEqualTo(1);
	}
	
}
