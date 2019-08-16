package com.blocklang.develop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.dao.ProjectBuildProfileDao;
import com.blocklang.develop.dao.ProjectDependenceDao;
import com.blocklang.develop.data.ProjectDependenceData;
import com.blocklang.develop.model.ProjectBuildProfile;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.develop.service.ProjectDependenceService;
import com.blocklang.marketplace.constant.Language;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;

public class ProjectDependenceServiceImplTest extends AbstractServiceTest{

	@Autowired
	private ProjectDependenceDao projectDependenceDao;
	@Autowired
	private ProjectDependenceService projectDependenceService;
	@Autowired
	private ProjectBuildProfileDao projectBuildProfileDao;
	@Autowired
	private ComponentRepoDao componentRepoDao;
	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;

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
		version.setGitTagName("v0.1.0");
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
		version.setGitTagName("v0.1.0");
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
		version.setGitTagName("v0.1.0");
		version.setApiRepoVersionId(3);
		version.setCreateUserId(11);
		version.setCreateTime(LocalDateTime.now());
		componentRepoVersionDao.save(version);
		
		version = new ComponentRepoVersion();
		version.setComponentRepoId(componentRepoId);
		version.setVersion("0.2.0");
		version.setGitTagName("v0.2.0");
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
	
	@Test
	public void find_project_dependence_no_dependence() {
		assertThat(projectDependenceService.findProjectDependences(1)).isEmpty();
	}
	
	// 因为当前只支持默认的 Profile，所以不用获取 Profile 信息
	@Test
	public void find_project_dependence_success() {
		Integer projectId = 1;
		// 创建对应的 API 仓库信息
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setGitRepoUrl("a");
		apiRepo.setGitRepoWebsite("b");
		apiRepo.setGitRepoOwner("c");
		apiRepo.setGitRepoName("d");
		apiRepo.setName("e");
		apiRepo.setVersion("f");
		apiRepo.setCategory(RepoCategory.CLIENT_API);
		apiRepo.setCreateUserId(1);
		apiRepo.setCreateTime(LocalDateTime.now());
		ApiRepo savedApiRepo = apiRepoDao.save(apiRepo);
		// 创建对应的 API 仓库版本信息
		ApiRepoVersion version = new ApiRepoVersion();
		version.setApiRepoId(savedApiRepo.getId());
		version.setVersion("0.1.0");
		version.setGitTagName("v0.1.0");
		version.setCreateUserId(1);
		version.setCreateTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(version);
		
		// 创建组件仓库信息
		// 1. dev
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(savedApiRepo.getId());
		repo.setGitRepoUrl("dev_url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("dev_name");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		repo.setIsIdeExtension(true);
		ComponentRepo savedDevRepo = componentRepoDao.save(repo);
		// 2. build
		repo = new ComponentRepo();
		repo.setApiRepoId(savedApiRepo.getId());
		repo.setGitRepoUrl("build_url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("build_name");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		ComponentRepo savedBuildRepo = componentRepoDao.save(repo);
		// 创建组件仓库版本信息
		// 1. dev
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(savedDevRepo.getId());
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion devRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 2. build
		componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(savedBuildRepo.getId());
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion buildRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 创建一个 dev 依赖（不需创建 Profile）
		ProjectDependence devDependence = new ProjectDependence();
		devDependence.setProjectId(projectId);
		devDependence.setComponentRepoVersionId(devRepoVersion.getId());
		devDependence.setCreateUserId(11);
		devDependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(devDependence);
		
		// 创建一个 build 依赖（需创建默认的 Profile）
		// 1. 为项目的 web 页面添加一个默认的 Profile
		ProjectBuildProfile profile = new ProjectBuildProfile();
		profile.setProjectId(projectId);
		profile.setAppType(AppType.WEB);
		profile.setName(ProjectBuildProfile.DEFAULT_PROFILE_NAME);
		profile.setCreateUserId(11);
		profile.setCreateTime(LocalDateTime.now());
		projectBuildProfileDao.save(profile);
		// 2. 为项目添加一个依赖
		ProjectDependence buildDependence = new ProjectDependence();
		buildDependence.setProjectId(projectId);
		buildDependence.setComponentRepoVersionId(buildRepoVersion.getId());
		buildDependence.setProfileId(profile.getId());
		buildDependence.setCreateUserId(11);
		buildDependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(buildDependence);
		
		List<ProjectDependenceData> dependences = projectDependenceService.findProjectDependences(projectId);
		assertThat(dependences).hasSize(2);
		assertThat(dependences).allMatch(dependence -> dependence.getId() != null && 
				dependence.getComponentRepo() != null &&
				dependence.getComponentRepoVersion() != null &&
				dependence.getApiRepo() != null &&
				dependence.getApiRepoVersion() != null
		);
	}
	
	// 注意，因为这些都是在一个事务中完成的，所以不要直接通过获取数据库表的记录数来断言，
	// 而是通过 jpa 的 findById 来断言，因为事务没有结束，数据库可能还没有执行真正做删除操作。
	@Test
	public void delete_success() {
		// 为项目添加一个依赖
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(1);
		dependence.setComponentRepoVersionId(2);
		dependence.setCreateUserId(11);
		dependence.setCreateTime(LocalDateTime.now());
		ProjectDependence savedDependence = projectDependenceDao.save(dependence);
		assertThat(projectDependenceDao.findById(savedDependence.getId())).isPresent();
		projectDependenceService.delete(savedDependence.getId());
		assertThat(projectDependenceDao.findById(savedDependence.getId())).isEmpty();
	}
	
	@Test
	public void update_success() {
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(1);
		dependence.setComponentRepoVersionId(2);
		dependence.setCreateUserId(11);
		dependence.setCreateTime(LocalDateTime.now());
		ProjectDependence savedDependence = projectDependenceDao.save(dependence);
		
		savedDependence.setComponentRepoVersionId(3);
		
		assertThat(projectDependenceService.update(savedDependence).getComponentRepoVersionId()).isEqualTo(3);
	}
	
	@Test
	public void find_by_id_no_data() {
		assertThat(projectDependenceService.findById(1)).isEmpty();
	}
	
	@Test
	public void find_by_id_success() {
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(1);
		dependence.setComponentRepoVersionId(2);
		dependence.setCreateUserId(11);
		dependence.setCreateTime(LocalDateTime.now());
		ProjectDependence savedDependence = projectDependenceDao.save(dependence);
		
		assertThat(projectDependenceService.findById(savedDependence.getId())).isPresent();
	}
}
