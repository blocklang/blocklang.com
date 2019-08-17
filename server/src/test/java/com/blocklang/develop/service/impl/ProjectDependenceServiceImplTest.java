package com.blocklang.develop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.dao.UserDao;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.dao.ProjectBuildProfileDao;
import com.blocklang.develop.dao.ProjectDependenceDao;
import com.blocklang.develop.data.ProjectDependenceData;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectBuildProfile;
import com.blocklang.develop.model.ProjectContext;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectDependenceService;
import com.blocklang.develop.service.ProjectService;
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
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProjectDependenceServiceImplTest extends AbstractServiceTest{
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	@MockBean
	private PropertyService propertyService;
	
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
	@Autowired
	private UserDao userDao;
	@Autowired
	private ProjectService projectService;

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
	public void save_success() throws IOException {
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
		
		File rootFolder = tempFolder.newFolder();
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.getPath()));
		
		ProjectDependence savedDependence = projectDependenceService.save(projectId, componentRepo, userId);
		
		assertThat(savedDependence.getProjectId()).isEqualTo(projectId);
		// 获取最新版本号
		assertThat(savedDependence.getComponentRepoVersionId()).isEqualTo(savedComponentRepoVersion2.getId());
		assertThat(savedDependence.getProfileId()).isNotNull();
		
		assertThat(countRowsInTable("PROJECT_BUILD_PROFILE")).isEqualTo(1);
		assertThat(countRowsInTable("PROJECT_DEPENDENCE")).isEqualTo(1);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void save_dependence_json_file() throws IOException {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		
		Project project = new Project();
		project.setName("project_name");
		project.setIsPublic(true);
		project.setDescription("description");
		project.setLastActiveTime(LocalDateTime.now());
		project.setCreateUserId(userId);
		project.setCreateTime(LocalDateTime.now());
		project.setCreateUserName("user_name");
		
		File rootFolder = tempFolder.newFolder();
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.getPath()));
		ProjectContext context = new ProjectContext("user_name", "project_name", rootFolder.getPath());
		
		Project savedProject = projectService.create(userInfo, project);
		
		// 依赖一个 dev 仓库
		ComponentRepo devRepo = new ComponentRepo();
		devRepo.setApiRepoId(1);
		devRepo.setGitRepoUrl("url1");
		devRepo.setGitRepoWebsite("website1");
		devRepo.setGitRepoOwner("jack1");
		devRepo.setGitRepoName("repo1");
		devRepo.setName("name1");
		devRepo.setVersion("version1");
		devRepo.setCategory(RepoCategory.WIDGET);
		devRepo.setCreateUserId(1);
		devRepo.setCreateTime(LocalDateTime.now());
		devRepo.setLanguage(Language.TYPESCRIPT);
		devRepo.setAppType(AppType.WEB);
		devRepo.setIsIdeExtension(true);
		ComponentRepo savedDevRepo = componentRepoDao.save(devRepo);

		ComponentRepoVersion devRepoVersion = new ComponentRepoVersion();
		devRepoVersion.setComponentRepoId(savedDevRepo.getId());
		devRepoVersion.setVersion("0.1.0");
		devRepoVersion.setGitTagName("v0.1.1");
		devRepoVersion.setApiRepoVersionId(3);
		devRepoVersion.setCreateUserId(11);
		devRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersionDao.save(devRepoVersion);
		
		projectDependenceService.save(savedProject.getId(), devRepo, userId);
		
		// 依赖一个 build 仓库
		ComponentRepo buildRepo = new ComponentRepo();
		buildRepo.setApiRepoId(1);
		buildRepo.setGitRepoUrl("url");
		buildRepo.setGitRepoWebsite("website");
		buildRepo.setGitRepoOwner("jack");
		buildRepo.setGitRepoName("repo");
		buildRepo.setName("name");
		buildRepo.setVersion("version");
		buildRepo.setCategory(RepoCategory.WIDGET);
		buildRepo.setCreateUserId(1);
		buildRepo.setCreateTime(LocalDateTime.now());
		buildRepo.setLanguage(Language.TYPESCRIPT);
		buildRepo.setAppType(AppType.WEB);
		ComponentRepo savedBuildRepo = componentRepoDao.save(buildRepo);
		
		ComponentRepoVersion buildRepoVersion = new ComponentRepoVersion();
		buildRepoVersion.setComponentRepoId(savedBuildRepo.getId());
		buildRepoVersion.setVersion("0.1.0");
		buildRepoVersion.setGitTagName("v0.1.0");
		buildRepoVersion.setApiRepoVersionId(3);
		buildRepoVersion.setCreateUserId(11);
		buildRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersionDao.save(buildRepoVersion);
		
		projectDependenceService.save(savedProject.getId(), buildRepo, userId);
		
		// 在 git 仓库中获取 DEPENDENCE.json 文件
		String dependenceFileContent = Files.readString(context.getGitRepositoryDirectory().resolve(ProjectResource.DEPENDENCE_NAME));
		ObjectMapper objectMapper = new ObjectMapper();
		Map jsonObject = objectMapper.readValue(dependenceFileContent, Map.class);
		assertThat(jsonObject).isNotEmpty();
		
		Map dev = (Map)((Map)((Map)jsonObject.get("dev")).get("web")).get("website1/jack1/repo1");
		assertThat(dev.get("git")).isEqualTo("url1");
		assertThat(dev.get("tag")).isEqualTo("v0.1.1");
		
		Map build = (Map)((Map)((Map)((Map)jsonObject.get("build")).get("web")).get("default")).get("website/jack/repo");
		assertThat(build.get("git")).isEqualTo("url");
		assertThat(build.get("tag")).isEqualTo("v0.1.0");
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
		assertThat(dependences).allMatch(dependence -> dependence != null && 
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
