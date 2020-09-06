package com.blocklang.develop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.StopWatch;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.BuildTarget;
import com.blocklang.develop.dao.ProjectBuildProfileDao;
import com.blocklang.develop.dao.ProjectDependencyDao;
import com.blocklang.develop.data.ProjectDependencyData;
import com.blocklang.develop.designer.data.RepoWidgetList;
import com.blocklang.develop.designer.data.Widget;
import com.blocklang.develop.designer.data.WidgetCategory;
import com.blocklang.develop.designer.data.WidgetProperty;
import com.blocklang.develop.model.ProjectBuildProfile;
import com.blocklang.develop.model.ProjectDependency;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryContext;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.ProjectDependencyService;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.constant.RepoType;
import com.blocklang.marketplace.constant.WidgetPropertyValueType;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ApiWidgetDao;
import com.blocklang.marketplace.dao.ApiWidgetPropertyDao;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ApiWidget;
import com.blocklang.marketplace.model.ApiWidgetProperty;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;

public class ProjectDependencyServiceImplTest extends AbstractServiceTest{

	@MockBean
	private PropertyService propertyService;
	
	@Autowired
	private ProjectDependencyDao projectDependencyDao;
	@Autowired
	private ProjectDependencyService projectDependencyService;
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
	@Autowired
	private ApiWidgetDao apiComponentDao;
	@Autowired
	private ApiWidgetPropertyDao apiComponentAttrDao;
	
	@Test
	public void devDependencyExistsThatNotExists() {
		Integer projectId = 1;
		Integer componentRepoId = 1;
		assertThat(projectDependencyService.devDependencyExists(projectId, componentRepoId)).isFalse();
	}
	
	@Test
	public void devDependencyExistsThatExists() {
		Integer projectId = 1;
		Integer componentRepoId = 2;
		// 创建组件仓库的版本信息
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setComponentRepoId(componentRepoId);
		version.setName("name");
		version.setVersion("0.1.0");
		version.setGitTagName("v0.1.0");
		version.setApiRepoVersionId(3);
		version.setAppType(AppType.WEB);
		version.setBuild("dojo");
		version.setCreateUserId(11);
		version.setCreateTime(LocalDateTime.now());
		version.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(version);
		// 为项目添加一个依赖
		Integer repositoryId = 3;
		ProjectDependency dependency = new ProjectDependency();
		dependency.setRepositoryId(repositoryId);
		dependency.setProjectId(projectId);
		dependency.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependency.setCreateUserId(11);
		dependency.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(dependency);
		
		assertThat(projectDependencyService.devDependencyExists(projectId, componentRepoId)).isTrue();
	}
	
	@Test
	public void buildDependencyExistsThatNotExists() {
		Integer projectId = 1;
		Integer buildProfileId = 2;
		Integer componentRepoId = 3;
		
		assertThat(projectDependencyService.buildDependencyExists(projectId, buildProfileId, componentRepoId)).isFalse();
	}
	
	@Test
	public void buildDependencyExistsThatExists() {
		Integer repositoryId = 1;
		Integer projectId = 2;
		Integer componentRepoId = 3;
		
		// 创建组件仓库的版本信息
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setComponentRepoId(componentRepoId);
		version.setName("name");
		version.setVersion("0.1.0");
		version.setGitTagName("v0.1.0");
		version.setApiRepoVersionId(3);
		version.setAppType(AppType.WEB);
		version.setBuild("dojo");
		version.setLastPublishTime(LocalDateTime.now());
		version.setCreateUserId(11);
		version.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(version);
		// 为项目的 web 页面添加一个默认的 Profile
		ProjectBuildProfile profile = new ProjectBuildProfile();
		profile.setRepositoryId(repositoryId);
		profile.setProjectId(projectId);
		profile.setAppType(AppType.WEB);
		profile.setBuildTarget(BuildTarget.QQ);
		profile.setName(ProjectBuildProfile.DEFAULT_PROFILE_NAME);
		profile.setCreateUserId(11);
		profile.setCreateTime(LocalDateTime.now());
		Integer buildProfileId = projectBuildProfileDao.save(profile).getId();
		
		// 为项目添加一个依赖
		ProjectDependency dependency = new ProjectDependency();
		dependency.setRepositoryId(repositoryId);
		dependency.setProjectId(projectId);
		dependency.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependency.setProfileId(buildProfileId);
		dependency.setCreateUserId(11);
		dependency.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(dependency);
		
		assertThat(projectDependencyService.buildDependencyExists(projectId, buildProfileId, componentRepoId)).isTrue();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void saveIdeDependencySuccess(@TempDir Path rootFolder) throws IOException {
		Integer repositoryId = 1;
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer projectId = 2;
		Integer profileId = 4;
		Integer userId = 5;
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		repository.setCreateUserName(owner);
		repository.setName(repoName);
		
		RepositoryResource project = new RepositoryResource();
		project.setId(projectId);
		project.setName(projectName);
		
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl("https://website1.com/jack1/repo1.git");
		repo.setGitRepoWebsite("website1.com");
		repo.setGitRepoOwner("jack1");
		repo.setGitRepoName("repo1");
		repo.setRepoType(RepoType.IDE);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(userId);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now());
		ComponentRepo savedRepo = componentRepoDao.save(repo);
		
		// 创建组件仓库的版本信息
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setComponentRepoId(savedRepo.getId());
		version.setName("name");
		version.setVersion("master");
		version.setGitTagName("refs/heads/master");
		version.setApiRepoVersionId(3);
		version.setAppType(AppType.WEB);
		version.setBuild("dojo");
		version.setCreateUserId(11);
		version.setCreateTime(LocalDateTime.now());
		version.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(version);
		
		ProjectDependency dependency = new ProjectDependency();
		dependency.setRepositoryId(repositoryId);
		dependency.setProjectId(projectId);
		dependency.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependency.setProfileId(profileId);
		dependency.setCreateUserId(userId);
		dependency.setCreateTime(LocalDateTime.now());
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		RepositoryContext repositoryContext = new RepositoryContext(owner, repoName, rootFolder.toString());
		Path projectPath = repositoryContext.getGitRepositoryDirectory().resolve(projectName);
		Files.createDirectories(projectPath);
		
		ProjectDependency savedDependence = projectDependencyService.save(repository, project, dependency);
		assertThat(savedDependence.getRepositoryId()).isEqualTo(repositoryId);
		assertThat(savedDependence.getProjectId()).isEqualTo(projectId);
		assertThat(savedDependence.getComponentRepoVersionId()).isEqualTo(savedComponentRepoVersion.getId());
		assertThat(savedDependence.getProfileId()).isEqualTo(profileId);
	
		Path projectDependencyPath = projectPath.resolve(RepositoryResource.DEPENDENCY_NAME);
		assertThat(projectDependencyPath).exists();
		
		String content = Files.readString(projectDependencyPath);
		Map<String, Object> json = JsonUtil.fromJsonObject(content, Map.class);
		Map<String, Object> git = (Map<String, Object>) ((Map<String, Object>)json.get("dev")).get("website1.com/jack1/repo1");
		assertThat(git.get("git")).isEqualTo("https://website1.com/jack1/repo1.git");
		assertThat(git.get("tag")).isEqualTo("refs/heads/master");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void saveProdDependencySuccess(@TempDir Path rootFolder) throws IOException {
		Integer repositoryId = 1;
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer projectId = 2;
		Integer userId = 5;
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		repository.setCreateUserName(owner);
		repository.setName(repoName);
		
		RepositoryResource project = new RepositoryResource();
		project.setId(projectId);
		project.setName(projectName);
		
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl("https://website1.com/jack1/repo1.git");
		repo.setGitRepoWebsite("website1.com");
		repo.setGitRepoOwner("jack1");
		repo.setGitRepoName("repo1");
		repo.setRepoType(RepoType.PROD);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(userId);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now());
		ComponentRepo savedRepo = componentRepoDao.save(repo);
		
		// 创建组件仓库的版本信息
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setComponentRepoId(savedRepo.getId());
		version.setName("name");
		version.setVersion("master");
		version.setGitTagName("refs/heads/master");
		version.setApiRepoVersionId(3);
		version.setAppType(AppType.MINI_PROGRAM);
		version.setBuild("dojo");
		version.setCreateUserId(11);
		version.setCreateTime(LocalDateTime.now());
		version.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(version);
		
		ProjectBuildProfile profile = new ProjectBuildProfile();
		profile.setRepositoryId(repositoryId);
		profile.setProjectId(projectId);
		profile.setAppType(AppType.MINI_PROGRAM);
		profile.setBuildTarget(BuildTarget.WEAPP);
		profile.setName(ProjectBuildProfile.DEFAULT_PROFILE_NAME);
		profile.setCreateUserId(11);
		profile.setCreateTime(LocalDateTime.now());
		Integer buildProfileId = projectBuildProfileDao.save(profile).getId();
		
		ProjectDependency dependency = new ProjectDependency();
		dependency.setRepositoryId(repositoryId);
		dependency.setProjectId(projectId);
		dependency.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependency.setProfileId(buildProfileId);
		dependency.setCreateUserId(userId);
		dependency.setCreateTime(LocalDateTime.now());
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		RepositoryContext repositoryContext = new RepositoryContext(owner, repoName, rootFolder.toString());
		Path projectPath = repositoryContext.getGitRepositoryDirectory().resolve(projectName);
		Files.createDirectories(projectPath);
		
		ProjectDependency savedDependence = projectDependencyService.save(repository, project, dependency);
		assertThat(savedDependence.getRepositoryId()).isEqualTo(repositoryId);
		assertThat(savedDependence.getProjectId()).isEqualTo(projectId);
		assertThat(savedDependence.getComponentRepoVersionId()).isEqualTo(savedComponentRepoVersion.getId());
		assertThat(savedDependence.getProfileId()).isEqualTo(buildProfileId);
	
		Path projectDependencyPath = projectPath.resolve(RepositoryResource.DEPENDENCY_NAME);
		assertThat(projectDependencyPath).exists();
		
		String content = Files.readString(projectDependencyPath);
		System.out.println(content);
		Map<String, Object> json = JsonUtil.fromJsonObject(content, Map.class);
		Map<String, Object> git = (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>)((Map<String, Object>)json.get("build")).get("weapp")).get("default")).get("website1.com/jack1/repo1");
		assertThat(git.get("git")).isEqualTo("https://website1.com/jack1/repo1.git");
		assertThat(git.get("tag")).isEqualTo("refs/heads/master");
	}
	
	@Test
	public void findAllConfigDependenciesNoDependices() {
		assertThat(projectDependencyService.findAllConfigDependencies(1)).isEmpty();
	}

	// 因为当前只支持默认的 Profile，所以不用获取 Profile 信息
	@Test
	public void findAllConfigDependenciesSuccess() {
		Integer repositoryId = 1;
		Integer projectId = 2;
		// 创建对应的 API 仓库信息
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setGitRepoUrl("a");
		apiRepo.setGitRepoWebsite("b");
		apiRepo.setGitRepoOwner("c");
		apiRepo.setGitRepoName("d");
		apiRepo.setCategory(RepoCategory.CLIENT_API);
		apiRepo.setCreateUserId(1);
		apiRepo.setCreateTime(LocalDateTime.now());
		apiRepo.setLastPublishTime(LocalDateTime.now());
		ApiRepo savedApiRepo = apiRepoDao.save(apiRepo);
		// 创建对应的 API 仓库版本信息
		ApiRepoVersion version = new ApiRepoVersion();
		version.setName("api_repo_version_name");
		version.setApiRepoId(savedApiRepo.getId());
		version.setVersion("0.1.0");
		version.setGitTagName("v0.1.0");
		version.setCreateUserId(1);
		version.setCreateTime(LocalDateTime.now());
		version.setLastPublishTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(version);
		
		// 创建组件仓库信息
		// 1. dev
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl("dev_url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setRepoType(RepoType.IDE);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now());
		ComponentRepo savedDevRepo = componentRepoDao.save(repo);
		// 2. build
		repo = new ComponentRepo();
		repo.setGitRepoUrl("build_url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setRepoType(RepoType.PROD);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now());
		ComponentRepo savedBuildRepo = componentRepoDao.save(repo);
		// 创建组件仓库版本信息
		// 1. dev
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(savedDevRepo.getId());
		componentRepoVersion.setName("ide repo");
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setAppType(AppType.WEB);
		componentRepoVersion.setBuild("dojo");
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersion.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion devRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 2. build
		componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(savedBuildRepo.getId());
		componentRepoVersion.setName("prod repo");
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setAppType(AppType.WEB);
		componentRepoVersion.setBuild("dojo");
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersion.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion buildRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 创建一个 dev 依赖（不需创建 Profile）
		ProjectDependency devDependence = new ProjectDependency();
		devDependence.setRepositoryId(repositoryId);
		devDependence.setProjectId(projectId);
		devDependence.setComponentRepoVersionId(devRepoVersion.getId());
		devDependence.setCreateUserId(11);
		devDependence.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(devDependence);
		
		// 创建一个 build 依赖（需创建默认的 Profile）
		// 1. 为项目的 web 页面添加一个默认的 Profile
		ProjectBuildProfile profile = new ProjectBuildProfile();
		profile.setRepositoryId(repositoryId);
		profile.setProjectId(projectId);
		profile.setAppType(AppType.WEB);
		profile.setBuildTarget(BuildTarget.WEAPP);
		profile.setName(ProjectBuildProfile.DEFAULT_PROFILE_NAME);
		profile.setCreateUserId(11);
		profile.setCreateTime(LocalDateTime.now());
		projectBuildProfileDao.save(profile);
		// 2. 为项目添加一个依赖
		ProjectDependency buildDependency = new ProjectDependency();
		buildDependency.setRepositoryId(repositoryId);
		buildDependency.setProjectId(projectId);
		buildDependency.setComponentRepoVersionId(buildRepoVersion.getId());
		buildDependency.setProfileId(profile.getId());
		buildDependency.setCreateUserId(11);
		buildDependency.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(buildDependency);

		List<ProjectDependencyData> dependencies = projectDependencyService.findAllConfigDependencies(projectId);
		
		assertThat(dependencies).hasSize(2);
		assertThat(dependencies).allMatch(dependency -> dependency != null && 
				dependency.getComponentRepo() != null &&
				dependency.getComponentRepoVersion() != null &&
				dependency.getApiRepo() != null &&
				dependency.getApiRepoVersion() != null
		);
	}

	@Test
	public void findIdeDependenciesNoDependencies() {
		Integer projectId = 1;
		assertThat(projectDependencyService.findDevDependencies(projectId)).isEmpty();
	}
	
	@Test
	public void findIdeDependenciesSuccess() {
		Integer repositoryId = 1;
		Integer projectId = 2;
		
		// 创建对应的 API 仓库信息
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setGitRepoUrl("a");
		apiRepo.setGitRepoWebsite("b");
		apiRepo.setGitRepoOwner("c");
		apiRepo.setGitRepoName("d");
		apiRepo.setCategory(RepoCategory.CLIENT_API);
		apiRepo.setCreateUserId(1);
		apiRepo.setCreateTime(LocalDateTime.now());
		apiRepo.setLastPublishTime(LocalDateTime.now());
		ApiRepo savedApiRepo = apiRepoDao.save(apiRepo);
		// 创建对应的 API 仓库版本信息
		ApiRepoVersion version = new ApiRepoVersion();
		version.setName("api_repo_version_name");
		version.setApiRepoId(savedApiRepo.getId());
		version.setVersion("0.1.0");
		version.setGitTagName("v0.1.0");
		version.setCreateUserId(1);
		version.setCreateTime(LocalDateTime.now());
		version.setLastPublishTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(version);
		
		// 创建组件仓库信息
		// 1. dev
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl("dev_url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setRepoType(RepoType.IDE);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now());
		ComponentRepo savedDevRepo = componentRepoDao.save(repo);
		// 2. build
		repo = new ComponentRepo();
		repo.setGitRepoUrl("build_url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setRepoType(RepoType.PROD);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now());
		ComponentRepo savedBuildRepo = componentRepoDao.save(repo);
		// 创建组件仓库版本信息
		// 1. dev
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(savedDevRepo.getId());
		componentRepoVersion.setName("ide repo");
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setAppType(AppType.WEB);
		componentRepoVersion.setBuild("dojo");
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersion.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion devRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 2. build
		componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(savedBuildRepo.getId());
		componentRepoVersion.setName("prod repo");
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setAppType(AppType.WEB);
		componentRepoVersion.setBuild("dojo");
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersion.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion buildRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 创建一个 IDE 依赖（不需创建 Profile）
		ProjectDependency devDependence = new ProjectDependency();
		devDependence.setRepositoryId(repositoryId);
		devDependence.setProjectId(projectId);
		devDependence.setComponentRepoVersionId(devRepoVersion.getId());
		devDependence.setCreateUserId(11);
		devDependence.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(devDependence);
		
		// 创建一个 PROD 依赖（需创建默认的 Profile）
		// 1. 为项目的 web 页面添加一个默认的 Profile
		ProjectBuildProfile profile = new ProjectBuildProfile();
		profile.setRepositoryId(repositoryId);
		profile.setProjectId(projectId);
		profile.setAppType(AppType.WEB);
		profile.setBuildTarget(BuildTarget.WEAPP);
		profile.setName(ProjectBuildProfile.DEFAULT_PROFILE_NAME);
		profile.setCreateUserId(11);
		profile.setCreateTime(LocalDateTime.now());
		projectBuildProfileDao.save(profile);
		// 2. 为项目添加一个依赖
		ProjectDependency buildDependency = new ProjectDependency();
		buildDependency.setRepositoryId(repositoryId);
		buildDependency.setProjectId(projectId);
		buildDependency.setComponentRepoVersionId(buildRepoVersion.getId());
		buildDependency.setProfileId(profile.getId());
		buildDependency.setCreateUserId(11);
		buildDependency.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(buildDependency);

		List<ProjectDependencyData> dependencies = projectDependencyService.findDevDependencies(projectId);
		
		assertThat(dependencies).hasSize(1);
		assertThat(dependencies).allMatch(dependency -> dependency != null && 
				dependency.getComponentRepo() != null &&
				dependency.getComponentRepoVersion() != null &&
				dependency.getApiRepo() != null &&
				dependency.getApiRepoVersion() != null
		);
	}
	
	@Test
	public void findStdDevDependenciesSuccess() {
		Integer userId = 1;
		Integer projectId = 2;
		String stdDevGitUrl = "valid_git_url";
		AppType appType = AppType.MINI_PROGRAM;
		
		// 创建一个标准库
		ApiRepo stdApiRepo = new ApiRepo();
		stdApiRepo.setCategory(RepoCategory.WIDGET);
		stdApiRepo.setGitRepoUrl("url");
		stdApiRepo.setGitRepoWebsite("website");
		stdApiRepo.setGitRepoOwner("owner");
		stdApiRepo.setGitRepoName("repo_name");
		stdApiRepo.setCreateUserId(userId);
		stdApiRepo.setCreateTime(LocalDateTime.now());
		stdApiRepo.setLastPublishTime(LocalDateTime.now());
		Integer stdApiRepoId = apiRepoDao.save(stdApiRepo).getId();
		// 为标准库设置一个版本号
		// 创建对应的 API 仓库版本信息
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(stdApiRepoId);
		apiVersion.setName("name");
		apiVersion.setVersion("0.0.1");
		apiVersion.setGitTagName("v0.0.1");
		apiVersion.setCreateUserId(userId);
		apiVersion.setCreateTime(LocalDateTime.now());
		apiVersion.setLastPublishTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);
		
		// 创建一个 ide 版的组件库
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl(stdDevGitUrl);
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setRepoType(RepoType.IDE);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(userId);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now());
		ComponentRepo savedComponentRepo = componentRepoDao.save(repo);
		// 创建一个 ide 版的组件库版本
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setName("name");
		version.setComponentRepoId(savedComponentRepo.getId());
		version.setVersion("master");
		version.setGitTagName("refs/heads/master");
		version.setApiRepoVersionId(savedApiRepoVersion.getId());
		version.setAppType(appType);
		version.setBuild("dojo");
		version.setCreateUserId(1);
		version.setCreateTime(LocalDateTime.now());
		version.setLastPublishTime(LocalDateTime.now());
		componentRepoVersionDao.save(version);
		
		when(propertyService.findStringValue(CmPropKey.STD_MINI_PROGRAM_COMPONENT_IDE_GIT_URL, "")).thenReturn(stdDevGitUrl);
		when(propertyService.findIntegerValue(CmPropKey.STD_REPO_REGISTER_USER_ID, 1)).thenReturn(1);
		// 注意，所有项目都会默认包含标准库
		List<ProjectDependencyData> dependencies = projectDependencyService.findStdDevDependencies(projectId, appType);
		
		assertThat(dependencies).hasSize(1);
	}
	
	@Test
	public void findStdBuildDependenciesSuccess() {
		Integer userId = 1;
		Integer projectId = 2;
		String stdBuildGitUrl = "valid_git_url";
		AppType appType = AppType.MINI_PROGRAM;
		
		// 创建一个标准库
		ApiRepo stdApiRepo = new ApiRepo();
		stdApiRepo.setCategory(RepoCategory.WIDGET);
		stdApiRepo.setGitRepoUrl("url");
		stdApiRepo.setGitRepoWebsite("website");
		stdApiRepo.setGitRepoOwner("owner");
		stdApiRepo.setGitRepoName("repo_name");
		stdApiRepo.setCreateUserId(userId);
		stdApiRepo.setCreateTime(LocalDateTime.now());
		stdApiRepo.setLastPublishTime(LocalDateTime.now());
		Integer stdApiRepoId = apiRepoDao.save(stdApiRepo).getId();
		// 为标准库设置一个版本号
		// 创建对应的 API 仓库版本信息
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(stdApiRepoId);
		apiVersion.setName("name");
		apiVersion.setVersion("0.0.1");
		apiVersion.setGitTagName("v0.0.1");
		apiVersion.setCreateUserId(userId);
		apiVersion.setCreateTime(LocalDateTime.now());
		apiVersion.setLastPublishTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);
		
		// 创建一个 prod 版的组件库
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl(stdBuildGitUrl);
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setRepoType(RepoType.PROD);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(userId);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now());
		ComponentRepo savedComponentRepo = componentRepoDao.save(repo);
		// 创建一个 ide 版的组件库版本
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setName("name");
		version.setComponentRepoId(savedComponentRepo.getId());
		version.setVersion("master");
		version.setGitTagName("refs/heads/master");
		version.setApiRepoVersionId(savedApiRepoVersion.getId());
		version.setAppType(appType);
		version.setBuild("dojo");
		version.setCreateUserId(1);
		version.setCreateTime(LocalDateTime.now());
		version.setLastPublishTime(LocalDateTime.now());
		componentRepoVersionDao.save(version);
		
		when(propertyService.findStringValue(CmPropKey.STD_MINI_PROGRAM_COMPONENT_PROD_GIT_URL, "")).thenReturn(stdBuildGitUrl);
		when(propertyService.findIntegerValue(CmPropKey.STD_REPO_REGISTER_USER_ID, 1)).thenReturn(1);
		// 注意，所有项目都会默认包含标准库
		List<ProjectDependencyData> dependencies = projectDependencyService.findStdBuildDependencies(projectId, appType);
		
		assertThat(dependencies).hasSize(1);
	}
	
	// 注意，因为这些都是在一个事务中完成的，所以不要直接通过获取数据库表的记录数来断言，
	// 而是通过 jpa 的 findById 来断言，因为事务没有结束，数据库可能还没有执行真正的删除操作。
	@Test
	public void deleteSuccess(@TempDir Path rootFolder) throws IOException {
		Integer repositoryId = 1;
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer projectId = 2;
		Integer profileId = 4;
		Integer userId = 5;
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		repository.setCreateUserName(owner);
		repository.setName(repoName);
		
		RepositoryResource project = new RepositoryResource();
		project.setId(projectId);
		project.setName(projectName);
		
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl("https://website1.com/jack1/repo1.git");
		repo.setGitRepoWebsite("website1.com");
		repo.setGitRepoOwner("jack1");
		repo.setGitRepoName("repo1");
		repo.setRepoType(RepoType.IDE);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(userId);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now());
		ComponentRepo savedRepo = componentRepoDao.save(repo);
		
		// 创建组件仓库的版本信息
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setComponentRepoId(savedRepo.getId());
		version.setName("name");
		version.setVersion("master");
		version.setGitTagName("refs/heads/master");
		version.setApiRepoVersionId(3);
		version.setAppType(AppType.WEB);
		version.setBuild("dojo");
		version.setCreateUserId(11);
		version.setCreateTime(LocalDateTime.now());
		version.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(version);
		
		ProjectDependency dependency = new ProjectDependency();
		dependency.setRepositoryId(repositoryId);
		dependency.setProjectId(projectId);
		dependency.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependency.setProfileId(profileId);
		dependency.setCreateUserId(userId);
		dependency.setCreateTime(LocalDateTime.now());
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		RepositoryContext repositoryContext = new RepositoryContext(owner, repoName, rootFolder.toString());
		Path projectPath = repositoryContext.getGitRepositoryDirectory().resolve(projectName);
		Files.createDirectories(projectPath);
		
		ProjectDependency savedDependence = projectDependencyService.save(repository, project, dependency);
		
		// 删除添加的依赖
		projectDependencyService.delete(repository, project, savedDependence.getId());
		
		assertThat(projectDependencyDao.findById(savedDependence.getId())).isEmpty();
		
		// 断言 DEPENDENCE.json 文件的内容为 “{ }”
		// 不直接转换为 java 对象判断，而是判断文本，这样可以断言美化后的 json
		// 在 git 仓库中获取 DEPENDENCE.json 文件
		Path projectDependencyPath = projectPath.resolve(RepositoryResource.DEPENDENCY_NAME);
		String dependencyFileContent = Files.readString(projectDependencyPath);
		assertThat(dependencyFileContent).isEqualTo("{ }");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateSuccess(@TempDir Path rootFolder) throws IOException {
		Integer repositoryId = 1;
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer projectId = 2;
		Integer profileId = 4;
		Integer userId = 5;
		
		String componentRepoGitUrl = "https://website1.com/jack1/repo1.git";
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		repository.setCreateUserName(owner);
		repository.setName(repoName);
		
		RepositoryResource project = new RepositoryResource();
		project.setId(projectId);
		project.setName(projectName);
		
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl(componentRepoGitUrl);
		repo.setGitRepoWebsite("website1.com");
		repo.setGitRepoOwner("jack1");
		repo.setGitRepoName("repo1");
		repo.setRepoType(RepoType.IDE);
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(userId);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLastPublishTime(LocalDateTime.now());
		ComponentRepo savedRepo = componentRepoDao.save(repo);
		
		// 创建组件仓库的版本信息
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setComponentRepoId(savedRepo.getId());
		version.setName("name");
		version.setVersion("master");
		version.setGitTagName("refs/heads/master");
		version.setApiRepoVersionId(3);
		version.setAppType(AppType.WEB);
		version.setBuild("dojo");
		version.setCreateUserId(11);
		version.setCreateTime(LocalDateTime.now());
		version.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(version);
		
		ProjectDependency dependency = new ProjectDependency();
		dependency.setRepositoryId(repositoryId);
		dependency.setProjectId(projectId);
		dependency.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependency.setProfileId(profileId);
		dependency.setCreateUserId(userId);
		dependency.setCreateTime(LocalDateTime.now());
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		RepositoryContext repositoryContext = new RepositoryContext(owner, repoName, rootFolder.toString());
		Path projectPath = repositoryContext.getGitRepositoryDirectory().resolve(projectName);
		Files.createDirectories(projectPath);
		
		ProjectDependency savedDependency = projectDependencyService.save(repository, project, dependency);
		
		// 修改添加的依赖
		ComponentRepoVersion newDevRepoVersion = new ComponentRepoVersion();
		newDevRepoVersion.setComponentRepoId(savedRepo.getId());
		newDevRepoVersion.setApiRepoVersionId(3);
		newDevRepoVersion.setName("name");
		newDevRepoVersion.setVersion("0.2.0");
		newDevRepoVersion.setGitTagName("refs/tags/v0.2.0");
		newDevRepoVersion.setAppType(AppType.WEB);
		newDevRepoVersion.setBuild("build");
		newDevRepoVersion.setCreateUserId(11);
		newDevRepoVersion.setCreateTime(LocalDateTime.now());
		newDevRepoVersion.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedNewDevRepoVersion = componentRepoVersionDao.save(newDevRepoVersion);
		
		savedDependency.setComponentRepoVersionId(savedNewDevRepoVersion.getId());
		
		projectDependencyService.update(repository, project, savedDependency);
		
		// 断言 DEPENDENCE.json 文件的内容
		// 在 git 仓库中获取 DEPENDENCY.json 文件
		String dependencyFileContent = Files.readString(projectPath.resolve(RepositoryResource.DEPENDENCY_NAME));
		Map jsonObject = JsonUtil.fromJsonObject(dependencyFileContent, Map.class);
		assertThat(jsonObject).isNotEmpty();
		
		Map dev = (Map)((Map)jsonObject.get("dev")).get("website1.com/jack1/repo1");
		assertThat(dev.get("git")).isEqualTo(componentRepoGitUrl);
		assertThat(dev.get("tag")).isEqualTo("refs/tags/v0.2.0");
	}
	
	@Test
	public void findByIdNoData() {
		Integer dependencyId = 1;
		assertThat(projectDependencyService.findById(dependencyId)).isEmpty();
	}
	
	@Test
	public void findByIdSuccess() {
		ProjectDependency dependency = new ProjectDependency();
		dependency.setRepositoryId(1);
		dependency.setProjectId(2);
		dependency.setComponentRepoVersionId(3);
		dependency.setCreateUserId(11);
		dependency.setCreateTime(LocalDateTime.now());
		ProjectDependency savedDependence = projectDependencyDao.save(dependency);
		
		assertThat(projectDependencyService.findById(savedDependence.getId())).isPresent();
	}
	
	@Test
	public void findAllWidgetsNoData() {
		Integer projectId = 1;
		List<RepoWidgetList> result = projectDependencyService.findAllWidgets(projectId);
		assertThat(result).isEmpty();
	}
	
	@Test
	public void findAllWidgetsUncategory() {
		Integer repositoryId = 1;
		Integer projectId = 2;
		
		// 创建一个 API 仓库，类型是  Widget
		String apiRepoUrl = "a";
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setGitRepoUrl(apiRepoUrl);
		apiRepo.setGitRepoWebsite("b");
		apiRepo.setGitRepoOwner("c");
		apiRepo.setGitRepoName("d");
		apiRepo.setCategory(RepoCategory.WIDGET);
		apiRepo.setCreateUserId(1);
		apiRepo.setCreateTime(LocalDateTime.now());
		apiRepo.setLastPublishTime(LocalDateTime.now());
		ApiRepo savedApiRepo = apiRepoDao.save(apiRepo);
		
		// 创建对应的 API 仓库版本信息
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setName("api repo");
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		apiVersion.setLastPublishTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);

		// 在版本下创建一个部件，没有分类
		ApiWidget widget = new ApiWidget();
		String widgetCode = "0001";
		String widgetName = "Widget1";
		widget.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget.setCode(widgetCode);
		widget.setName(widgetName);
		widget.setLabel("Wiget 1");
		widget.setDescription("Description");
		widget.setCreateUserId(1);
		widget.setCreateTime(LocalDateTime.now());
		ApiWidget savedWidget = apiComponentDao.save(widget);
		
		// 为部件设置一个属性
		ApiWidgetProperty widgetProperty = new ApiWidgetProperty();
		widgetProperty.setApiRepoVersionId(savedApiRepoVersion.getId());
		widgetProperty.setApiWidgetId(savedWidget.getId());
		widgetProperty.setCode("0011");
		widgetProperty.setName("prop_name");
		widgetProperty.setValueType(WidgetPropertyValueType.STRING.getKey());
		apiComponentAttrDao.save(widgetProperty);
		
		// 在组件仓库版本信息中创建一条记录，引用 api 版本信息
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1);
		componentRepoVersion.setName("comp repo");
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setAppType(AppType.WEB);
		componentRepoVersion.setBuild("dojo");
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersion.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		
		// 将组件仓库添加为项目依赖
		ProjectDependency dependency = new ProjectDependency();
		dependency.setRepositoryId(repositoryId);
		dependency.setProjectId(projectId);
		dependency.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependency.setCreateUserId(1);
		dependency.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(dependency);
		
		StopWatch watch = new StopWatch();
		watch.start();
		List<RepoWidgetList> result = projectDependencyService.findAllWidgets(projectId);
		watch.stop();
		System.out.println("毫秒：" + watch.getTotalTimeMillis());
		
		assertThat(result).hasSize(1);
		// 测试未分组的情况
		RepoWidgetList repo = result.get(0);
		assertThat(repo.getApiRepoId()).isEqualTo(savedApiRepo.getId());
		assertThat(repo.getApiRepoName()).isEqualTo(apiRepoUrl);
		assertThat(repo.getWidgetCategories()).hasSize(1);
		
		WidgetCategory category1 = repo.getWidgetCategories().get(0);
		assertThat(category1.getName()).isEqualTo("_");
		assertThat(category1.getWidgets()).hasSize(1);
		
		Widget widget1 = category1.getWidgets().get(0);
		// Widget 中的 apiRepoId 是一个冗余字段
		assertThat(widget1.getApiRepoId()).isEqualTo(savedApiRepo.getId());
		assertThat(widget1.getWidgetId()).isEqualTo(savedWidget.getId());
		assertThat(widget1.getWidgetCode()).isEqualTo(widgetCode);
		assertThat(widget1.getWidgetName()).isEqualTo(widgetName);
		
		WidgetProperty property = widget1.getProperties().get(0);
		assertThat(property.getCode()).isEqualTo("0011");
		assertThat(property.getName()).isEqualTo("prop_name");
		assertThat(property.getValueType()).isEqualTo("string");
		assertThat(property.getDefaultValue()).isNull();
	}
	
	// 项目依赖两个组件库，但两个组件库实现同一个 api
	@Test
	public void findAllWidgetsTwoComponentRepoImplOneApiRepo() {
		Integer repositoryId = 1;
		Integer projectId = 2;
		
		// 创建一个 API 仓库，类型是 Widget
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setGitRepoUrl("a");
		apiRepo.setGitRepoWebsite("b");
		apiRepo.setGitRepoOwner("c");
		apiRepo.setGitRepoName("d");
		apiRepo.setCategory(RepoCategory.WIDGET);
		apiRepo.setCreateUserId(1);
		apiRepo.setCreateTime(LocalDateTime.now());
		apiRepo.setLastPublishTime(LocalDateTime.now());
		ApiRepo savedApiRepo = apiRepoDao.save(apiRepo);
		
		// 创建对应的 API 仓库版本信息
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setName("name");
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		apiVersion.setLastPublishTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);

		// 在版本下创建一个部件，没有分类
		ApiWidget widget = new ApiWidget();
		widget.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget.setCode("0001");
		widget.setName("Widget1");
		widget.setLabel("Wiget 1");
		widget.setDescription("Description");
		widget.setCreateUserId(1);
		widget.setCreateTime(LocalDateTime.now());
		apiComponentDao.save(widget);
		
		// 第一个依赖
		// 1. 在组件仓库版本信息中创建一条记录，引用 api 版本信息
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1); // 组件仓库 id 为 1
		componentRepoVersion.setName("name1");
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setAppType(AppType.WEB);
		componentRepoVersion.setBuild("build");
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersion.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		
		// 2. 将组件仓库添加为项目依赖
		ProjectDependency dependency = new ProjectDependency();
		dependency.setRepositoryId(repositoryId);
		dependency.setProjectId(projectId);
		dependency.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependency.setCreateUserId(1);
		dependency.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(dependency);
		
		// 第二个依赖
		// 1. 在组件仓库版本信息中创建一条记录，引用 api 版本信息
		componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(2); // 组件仓库 id 为 2
		componentRepoVersion.setName("name2");
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setAppType(AppType.WEB);
		componentRepoVersion.setBuild("build");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId()); // 实现的是同一个 api repo version
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersion.setLastPublishTime(LocalDateTime.now());
		savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		
		// 2. 将组件仓库添加为项目依赖
		dependency = new ProjectDependency();
		dependency.setRepositoryId(repositoryId);
		dependency.setProjectId(projectId);
		dependency.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependency.setCreateUserId(1);
		dependency.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(dependency);
		
		StopWatch watch = new StopWatch();
		watch.start();
		List<RepoWidgetList> result = projectDependencyService.findAllWidgets(projectId);
		watch.stop();
		System.out.println("毫秒：" + watch.getTotalTimeMillis());
		assertThat(result).hasSize(1);
	}
	
	// 只过滤出 Widget 类型的 API REPO
	@Test
	public void findAllWidgetsOnlyFilterWidgetApiRepo() {
		Integer repositoryId = 1;
		Integer projectId = 2;
		
		// 创建一个 API 仓库，类型是 Widget
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setGitRepoUrl("a");
		apiRepo.setGitRepoWebsite("b");
		apiRepo.setGitRepoOwner("c");
		apiRepo.setGitRepoName("d");
		apiRepo.setCategory(RepoCategory.CLIENT_API); // 不是 Widget 仓库
		apiRepo.setCreateUserId(1);
		apiRepo.setCreateTime(LocalDateTime.now());
		apiRepo.setLastPublishTime(LocalDateTime.now());
		ApiRepo savedApiRepo = apiRepoDao.save(apiRepo);
		
		// 创建对应的 API 仓库版本信息
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setName("api repo");
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		apiVersion.setLastPublishTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);

		// 在组件仓库版本信息中创建一条记录，引用 api 版本信息
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1);
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setName("comp repo");
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setAppType(AppType.WEB);
		componentRepoVersion.setBuild("dojo");
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersion.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		
		// 将组件仓库添加为项目依赖
		ProjectDependency dependency = new ProjectDependency();
		dependency.setRepositoryId(repositoryId);
		dependency.setProjectId(projectId);
		dependency.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependency.setCreateUserId(1);
		dependency.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(dependency);
		
		List<RepoWidgetList> result = projectDependencyService.findAllWidgets(projectId);
		assertThat(result).hasSize(0);
	}

	// 测试一个 API 仓库中有两个分组的情况
	// 尚不支持 category 字段
	@Disabled
	@Test
	public void findAllWidgetsTwoCategory() {
		Integer repositoryId = 1;
		Integer projectId = 2;
		
		// 创建一个 API 仓库，类型是  Widget
		String apiRepoName = "api_repo_name";
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setGitRepoUrl("a");
		apiRepo.setGitRepoWebsite("b");
		apiRepo.setGitRepoOwner("c");
		apiRepo.setGitRepoName("d");
		apiRepo.setCategory(RepoCategory.WIDGET);
		apiRepo.setCreateUserId(1);
		apiRepo.setCreateTime(LocalDateTime.now());
		apiRepo.setLastPublishTime(LocalDateTime.now());
		ApiRepo savedApiRepo = apiRepoDao.save(apiRepo);
		
		// 创建对应的 API 仓库版本信息
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		apiVersion.setLastPublishTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);

		// 在版本下创建一个部件，没有分类
		ApiWidget widget = new ApiWidget();
		String widgetCode = "0001";
		String widgetName = "Widget1";
		widget.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget.setCode(widgetCode);
		widget.setName(widgetName);
		widget.setLabel("Wiget 1");
		widget.setDescription("Description");
		widget.setCreateUserId(1);
		widget.setCreateTime(LocalDateTime.now());
		ApiWidget savedWidget = apiComponentDao.save(widget);
		
		// TODO: 添加两个部件，分别属于不同种类。
		
		// 在组件仓库版本信息中创建一条记录，引用 api 版本信息
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1);
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersion.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		
		// 将组件仓库添加为项目依赖
		ProjectDependency dependency = new ProjectDependency();
		dependency.setRepositoryId(repositoryId);
		dependency.setProjectId(projectId);
		dependency.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependency.setCreateUserId(1);
		dependency.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(dependency);
		
		StopWatch watch = new StopWatch();
		watch.start();
		List<RepoWidgetList> result = projectDependencyService.findAllWidgets(projectId);
		watch.stop();
		System.out.println("毫秒：" + watch.getTotalTimeMillis());
		
		assertThat(result).hasSize(1);
		// 测试未分组的情况
		RepoWidgetList repo = result.get(0);
		assertThat(repo.getApiRepoId()).isEqualTo(savedApiRepo.getId());
		assertThat(repo.getApiRepoName()).isEqualTo(apiRepoName);
		assertThat(repo.getWidgetCategories()).hasSize(1);
		
		WidgetCategory category1 = repo.getWidgetCategories().get(0);
		assertThat(category1.getName()).isEqualTo("_");
		assertThat(category1.getWidgets()).hasSize(1);
		
		Widget widget1 = category1.getWidgets().get(0);
		// Widget 中的 apiRepoId 是一个冗余字段
		assertThat(widget1.getApiRepoId()).isEqualTo(savedApiRepo.getId());
		assertThat(widget1.getWidgetId()).isEqualTo(savedWidget.getId());
		assertThat(widget1.getWidgetCode()).isEqualTo(widgetCode);
		assertThat(widget1.getWidgetName()).isEqualTo(widgetName);
	}
}
