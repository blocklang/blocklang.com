package com.blocklang.develop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.constant.Constant;
import com.blocklang.core.dao.UserDao;
import com.blocklang.core.git.GitUtils;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.RepositoryResourceType;
import com.blocklang.develop.dao.PageWidgetAttrValueDao;
import com.blocklang.develop.dao.PageWidgetDao;
import com.blocklang.develop.dao.RepositoryAuthorizationDao;
import com.blocklang.develop.dao.RepositoryCommitDao;
import com.blocklang.develop.dao.RepositoryDao;
import com.blocklang.develop.dao.RepositoryFileDao;
import com.blocklang.develop.dao.RepositoryResourceDao;
import com.blocklang.develop.data.GitCommitInfo;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryAuthorization;
import com.blocklang.develop.model.RepositoryContext;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.RepositoryResourceService;
import com.blocklang.develop.service.RepositoryService;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ApiWidgetDao;
import com.blocklang.marketplace.dao.ApiWidgetPropertyDao;
import com.blocklang.release.dao.AppDao;

public class RepositoryServiceImplTest extends AbstractServiceTest{
	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryDao repositoryDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private AppDao appDao;
	@Autowired
	private RepositoryResourceService repositoryResourceService;
	@Autowired
	private RepositoryResourceDao repositoryResourceDao;
	@Autowired
	private PageWidgetDao pageWidgetDao;
	@Autowired
	private PageWidgetAttrValueDao pageWidgetAttrValueDao;
	@Autowired
	private RepositoryFileDao repositoryFileDao;
	@Autowired
	private RepositoryAuthorizationDao repositoryAuthorizationDao;
	@Autowired
	private RepositoryCommitDao repositoryCommitDao;
	@MockBean
	private PropertyService propertyService;
	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	@Autowired
	private ApiWidgetDao apiComponentDao;
	@Autowired
	private ApiWidgetPropertyDao apiComponentAttrDao;
	
	@Test
	public void find_no_data() {
		Optional<Repository> projectOption = repositoryService.find("not-exist-owner", "not-exist-name");
		assertThat(projectOption).isEmpty();
	}
	
	@Test
	public void find_success() {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		
		Repository repository = new Repository();
		repository.setName("project_name");
		repository.setIsPublic(true);
		repository.setLastActiveTime(LocalDateTime.now());
		repository.setCreateUserId(userId);
		repository.setCreateTime(LocalDateTime.now());
		
		repositoryDao.save(repository);
		
		Optional<Repository> repositoryOption = repositoryService.find("user_name", "project_name");
		assertThat(repositoryOption).isPresent();
		assertThat(repositoryOption.get().getCreateUserName()).isEqualTo("user_name");
	}
	
	@Test
	public void createRepository_success(@TempDir Path rootFolder) throws IOException {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		
		Repository repository = new Repository();
		repository.setName("project_name");
		repository.setIsPublic(true);
		repository.setDescription("description");
		repository.setLastActiveTime(LocalDateTime.now());
		repository.setCreateUserId(userId);
		repository.setCreateTime(LocalDateTime.now());
		repository.setCreateUserName("user_name");
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		
		Integer repositoryId = repositoryService.createRepository(userInfo, repository).getId();
		
		// 断言
		// 项目基本信息已保存
		assertThat(repositoryDao.findById(repositoryId).get()).hasNoNullFieldsOrPropertiesExcept("lastUpdateUserId", "lastUpdateTime", "avatarUrl", "accessLevel");
		
		// 项目授权信息已保存，项目创建者具有 admin 权限
		assertThat(repositoryAuthorizationDao.findAllByUserId(userId)).hasSize(1).allMatch(projectAuth -> {
			return projectAuth.getAccessLevel() == AccessLevel.ADMIN && projectAuth.getCreateUserId() == userId;
		});
		
		// 已在项目资源表中登记 README.md 文件
		RepositoryResource readmeResource = repositoryResourceDao.findByRepositoryIdAndParentIdAndResourceTypeAndAppTypeAndKeyIgnoreCase(
				repositoryId, 
				Constant.TREE_ROOT_ID, 
				RepositoryResourceType.FILE,
				AppType.UNKNOWN,
				"ReAdMe").get();
		assertThat(readmeResource).hasNoNullFieldsOrPropertiesExcept(
				"lastUpdateUserId", 
				"lastUpdateTime", 
				"description",
				"latestCommitId",
				"latestShortMessage",
				"latestFullMessage",
				"latestCommitTime",
				"messageSource",
				"gitStatus");
		
		// 已在项目文件表中保存 README.md 文件
		assertThat(repositoryFileDao.findByRepositoryResourceId(readmeResource.getId()).get()).hasNoNullFieldsOrProperties();
		
		// 已在项目资源表中登记 BUILD.json 文件
		RepositoryResource buildConfigResource = repositoryResourceDao.findByRepositoryIdAndParentIdAndResourceTypeAndAppTypeAndKeyIgnoreCase(
				repositoryId, 
				Constant.TREE_ROOT_ID, 
				RepositoryResourceType.BUILD,
				AppType.UNKNOWN,
				"Build").get();
		assertThat(buildConfigResource).hasNoNullFieldsOrPropertiesExcept(
				"lastUpdateUserId", 
				"lastUpdateTime", 
				"description",
				"latestCommitId",
				"latestShortMessage",
				"latestFullMessage",
				"latestCommitTime",
				"messageSource",
				"gitStatus");
		
		// git 仓库已创建
		// 测试时将 projectsRootPath 导向到 junit 的临时文件夹
		RepositoryContext context = new RepositoryContext("user_name", "project_name", rootFolder.toString());
		assertThat(GitUtils.isGitRepo(context.getGitRepositoryDirectory())).isTrue();
		
		// 在创建一个仓库时，会执行一个 git commit，此操作也保存在 project_commit 中
		assertThat(repositoryCommitDao.findAllByRepositoryIdAndBranchOrderByCommitTimeDesc(repositoryId, "master").get(0)).hasNoNullFieldsOrPropertiesExcept("fullMessage");
		
		// 确认 git 仓库中有 README.md 文件，并比较其内容
		String expectedReadmeContext = "# project_name\r\n\r\n**TODO: 在这里添加详细介绍，帮助感兴趣的人快速了解您的仓库。**";
		assertThat(Files.readString(context.getGitRepositoryDirectory().resolve("README.md"))).isEqualTo(expectedReadmeContext);
		
		// 确认 git 仓库中有 BUILD.json 文件，内容为空 json 对象
		assertThat(Files.readString(context.getGitRepositoryDirectory().resolve("BUILD.json"))).isEqualTo("{}");
	}
	
	// TODO: 该测试用例调整为 createWebProject_success
//	@Test
//	public void create_success(@TempDir Path rootFolder) throws IOException {
//		UserInfo userInfo = new UserInfo();
//		userInfo.setLoginName("user_name");
//		userInfo.setAvatarUrl("avatar_url");
//		userInfo.setEmail("email");
//		userInfo.setMobile("mobile");
//		userInfo.setCreateTime(LocalDateTime.now());
//		Integer userId = userDao.save(userInfo).getId();
//		
//		// 创建一个标准库
//		String stdApiRepoUrl = "std-api-widget-url";
//		ApiRepo stdApiRepo = new ApiRepo();
//		stdApiRepo.setCategory(RepoCategory.WIDGET);
//		stdApiRepo.setGitRepoUrl(stdApiRepoUrl);
//		stdApiRepo.setGitRepoWebsite("website");
//		stdApiRepo.setGitRepoOwner("owner");
//		stdApiRepo.setGitRepoName("repo_name");
//		stdApiRepo.setCreateUserId(1);
//		stdApiRepo.setCreateTime(LocalDateTime.now());
//		Integer stdApiRepoId = apiRepoDao.save(stdApiRepo).getId();
//		// 为标准库设置一个版本号
//		// 创建对应的 API 仓库版本信息
//		ApiRepoVersion apiVersion = new ApiRepoVersion();
//		apiVersion.setApiRepoId(stdApiRepoId);
//		apiVersion.setName("a api version");
//		apiVersion.setVersion("0.0.1");
//		apiVersion.setGitTagName("v0.0.1");
//		apiVersion.setCreateUserId(1);
//		apiVersion.setCreateTime(LocalDateTime.now());
//		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);
//
//		// 在标准库中创建一个 Page 部件
//		ApiWidget widget = new ApiWidget();
//		String widgetCode = "0001";
//		String widgetName = "Page";
//		widget.setApiRepoVersionId(savedApiRepoVersion.getId());
//		widget.setCode(widgetCode);
//		widget.setName(widgetName);
//		widget.setCreateUserId(1);
//		widget.setCreateTime(LocalDateTime.now());
//		ApiWidget savedWidget = apiComponentDao.save(widget);
//		// 为 Page 部件添加一个属性
//		ApiWidgetProperty widgetProperty = new ApiWidgetProperty();
//		widgetProperty.setApiRepoVersionId(savedApiRepoVersion.getId());
//		widgetProperty.setApiWidgetId(savedWidget.getId());
//		widgetProperty.setCode("0011");
//		widgetProperty.setName("prop_name");
//		widgetProperty.setDefaultValue("default_value");
//		widgetProperty.setValueType(WidgetPropertyValueType.STRING);
//		apiComponentAttrDao.save(widgetProperty);
//		// 并在配置文件中引用该标准库
//		// 项目应直接依赖于 ide 版本的仓库
//		// FIXME: 虽然在项目依赖中不配置标准库，但是在读取项目依赖的信息时，也要包括标准库
//		
//		// 默认引用最新版本，所以只需指定 ApiRepo 的 id
//		
//		Project project = new Project();
//		project.setName("project_name");
//		project.setIsPublic(true);
//		project.setDescription("description");
//		project.setLastActiveTime(LocalDateTime.now());
//		project.setCreateUserId(userId);
//		project.setCreateTime(LocalDateTime.now());
//		project.setCreateUserName("user_name");
//		
//		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
//		
//		when(propertyService.findStringValue(CmPropKey.STD_WIDGET_API_GIT_URL, "")).thenReturn(stdApiRepoUrl);
//		when(propertyService.findIntegerValue(CmPropKey.STD_WIDGET_REGISTER_USERID, 1)).thenReturn(1);
//		when(propertyService.findStringValue(CmPropKey.STD_WIDGET_ROOT_NAME, "Page")).thenReturn("Page");
//		
//		Integer projectId = projectService.create(userInfo, project).getId();
//		
//		// 断言
//		// 项目基本信息已保存
//		assertThat(projectDao.findById(projectId).get()).hasNoNullFieldsOrPropertiesExcept("lastUpdateUserId", "lastUpdateTime", "avatarUrl", "accessLevel");
//		
//		// 项目授权信息已保存，项目创建者具有 admin 权限
//		assertThat(projectAuthorizationDao.findAllByUserId(userId)).hasSize(1).allMatch(projectAuth -> {
//			return projectAuth.getAccessLevel() == AccessLevel.ADMIN && projectAuth.getCreateUserId() == userId;
//		});
//		
//		// APP 基本信息已保存
//		assertThat(appDao.findByProjectId(projectId).get()).hasNoNullFieldsOrPropertiesExcept("lastUpdateUserId", "lastUpdateTime");
//		
//		// 已创建入口程序模块
//		// 传入 Main，首字母大写是为了测试忽略大小写
//		ProjectResource mainPage = projectResourceDao.findByProjectIdAndParentIdAndResourceTypeAndAppTypeAndKeyIgnoreCase(
//				projectId, 
//				Constant.TREE_ROOT_ID, 
//				ProjectResourceType.PAGE,
//				AppType.WEB,
//				"Main").get();
//		assertThat(mainPage)
//			.hasNoNullFieldsOrPropertiesExcept(
//					"lastUpdateUserId", 
//					"lastUpdateTime", 
//					"description",
//					"latestCommitId",
//					"latestShortMessage",
//					"latestFullMessage",
//					"latestCommitTime",
//					"messageSource",
//					"gitStatus");
//		// 断言页面中默认包含一个部件
//		List<PageWidget> widgets = pageWidgetDao.findAllByPageIdOrderBySeq(mainPage.getId());
//		assertThat(widgets).hasSize(1);
//		PageWidget rootWidget = widgets.get(0);
//		assertThat(rootWidget.getApiRepoId()).isEqualTo(stdApiRepoId);
//		assertThat(rootWidget.getId()).hasSize(32);
//		assertThat(rootWidget.getParentId()).isEqualTo(Constant.TREE_ROOT_ID.toString());
//		assertThat(rootWidget.getSeq()).isEqualTo(1); // 是从 1 开始的，不是从 0 开始
//		assertThat(rootWidget.getWidgetCode()).isEqualTo("0001");
//		
//		// 断言部件中包含一个属性
//		List<PageWidgetAttrValue> rootWidgetProperties = pageWidgetAttrValueDao.findAllByPageWidgetId(rootWidget.getId());
//		assertThat(rootWidgetProperties).hasSize(1);
//		PageWidgetAttrValue rootWidgetAttrValue1 = rootWidgetProperties.get(0);
//		assertThat(rootWidgetAttrValue1.getId()).hasSize(32);
//		assertThat(rootWidgetAttrValue1.getWidgetAttrCode()).isEqualTo("0011");
//		assertThat(rootWidgetAttrValue1.getAttrValue()).isEqualTo("default_value");
//		
//		// 已在项目资源表中登记 README.md 文件
//		ProjectResource readmeResource = projectResourceDao.findByProjectIdAndParentIdAndResourceTypeAndAppTypeAndKeyIgnoreCase(
//				projectId, 
//				Constant.TREE_ROOT_ID, 
//				ProjectResourceType.FILE,
//				AppType.UNKNOWN,
//				"ReAdMe").get();
//		assertThat(readmeResource).hasNoNullFieldsOrPropertiesExcept(
//				"lastUpdateUserId", 
//				"lastUpdateTime", 
//				"description",
//				"latestCommitId",
//				"latestShortMessage",
//				"latestFullMessage",
//				"latestCommitTime",
//				"messageSource",
//				"gitStatus");
//		
//		// 已在项目文件表中保存 README.md 文件
//		assertThat(projectFileDao.findByProjectResourceId(readmeResource.getId()).get()).hasNoNullFieldsOrProperties();
//		
//		// 已在项目资源表中登记 DEPENDENCY.json 文件
//		ProjectResource dependenceResource = projectResourceDao.findByProjectIdAndParentIdAndResourceTypeAndAppTypeAndKeyIgnoreCase(
//				projectId, 
//				Constant.TREE_ROOT_ID, 
//				ProjectResourceType.DEPENDENCY, 
//				AppType.UNKNOWN, 
//				ProjectResource.DEPENDENCY_KEY).get();
//		assertThat(dependenceResource).hasNoNullFieldsOrPropertiesExcept(
//				"lastUpdateUserId", 
//				"lastUpdateTime", 
//				"description",
//				"latestCommitId",
//				"latestShortMessage",
//				"latestFullMessage",
//				"latestCommitTime",
//				"messageSource",
//				"gitStatus");
//		
//		// git 仓库已创建
//		// 测试时将 projectsRootPath 导向到 junit 的临时文件夹
//		RepositoryContext context = new RepositoryContext("user_name", "project_name", rootFolder.toString());
//		assertThat(GitUtils.isGitRepo(context.getGitRepositoryDirectory())).isTrue();
//		
//		// 在创建一个仓库时，会执行一个 git commit，此操作也保存在 project_commit 中
//		assertThat(projectCommitDao.findAllByProjectIdAndBranchOrderByCommitTimeDesc(projectId, "master").get(0)).hasNoNullFieldsOrPropertiesExcept("fullMessage");
//		
//		// TODO: 确认已将应用的模板也保存到了 git 仓库中
//		// 为了便于测试，可能要将 applyTemplate 方法单独提取出来
//		
//		// 确认 git 仓库中有 main.json 文件，并比较其内容
//		ObjectMapper mapper = new ObjectMapper();
//		String mainPageJsonString = Files.readString(context.getGitRepositoryDirectory().resolve("main.page.web.json"));
//		PageModel actualPageModel = mapper.readValue(mainPageJsonString, PageModel.class);
//		assertThat(actualPageModel.getPageId()).isEqualTo(mainPage.getId());
//		assertThat(actualPageModel.getWidgets()).hasSize(1);
//		AttachedWidget actualRootWidget = actualPageModel.getWidgets().get(0);
//		assertThat(actualRootWidget.getId()).isEqualTo(rootWidget.getId());
//		assertThat(actualRootWidget.getApiRepoId()).isEqualTo(rootWidget.getApiRepoId());
//		assertThat(actualRootWidget.getParentId()).isEqualTo(rootWidget.getParentId());
//		// Page 部件的基本信息
//		assertThat(actualRootWidget.getWidgetId()).isEqualTo(widget.getId());
//		assertThat(actualRootWidget.getWidgetCode()).isEqualTo(widgetCode);
//		assertThat(actualRootWidget.getWidgetName()).isEqualTo(widgetName);
//		// Page 部件的属性信息
//		assertThat(actualRootWidget.getProperties()).hasSize(1);
//		AttachedWidgetProperty actualRootWidgetProperty1 = actualRootWidget.getProperties().get(0);
//		assertThat(actualRootWidgetProperty1.getId()).isEqualTo(rootWidgetAttrValue1.getId());
//		assertThat(actualRootWidgetProperty1.getCode()).isEqualTo(rootWidgetAttrValue1.getWidgetAttrCode());
//		assertThat(actualRootWidgetProperty1.getValueType()).isEqualTo(WidgetPropertyValueType.STRING.getKey());
//		assertThat(actualRootWidgetProperty1.getValue()).isEqualTo(rootWidgetAttrValue1.getAttrValue());
//		assertThat(actualRootWidgetProperty1.getName()).isEqualTo("prop_name");
//		
//		// 确认 git 仓库中有 README.md 文件，并比较其内容
//		String expectedReadmeContext = "# project_name\r\n\r\n**TODO: 在这里添加项目介绍，帮助感兴趣的人快速了解您的项目。**";
//		assertThat(Files.readString(context.getGitRepositoryDirectory().resolve("README.md"))).isEqualTo(expectedReadmeContext);
//		
//		// 确认 git 仓库中有 DEPENDENCY.json 文件，内容为空 json 对象
//		// 为了便于用户阅读，json 做了美化排版
//		assertThat(Files.readString(context.getGitRepositoryDirectory().resolve("DEPENDENCY.json"))).isEqualTo("{ }");
//	}
	
	@Test
	public void find_can_access_projects_created_no_data() {
		List<Repository> repos = repositoryService.findCanAccessRepositoriesByUserId(1);
		assertThat(repos).isEmpty();
	}
	
	@Test
	public void find_can_access_projects_created_success() {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		
		Repository repository = new Repository();
		repository.setName("project_name");
		repository.setIsPublic(true);
		repository.setDescription("description");
		repository.setLastActiveTime(LocalDateTime.now());
		repository.setCreateUserId(userId);
		repository.setCreateTime(LocalDateTime.now());
		
		Integer savedRepositoryId = repositoryDao.save(repository).getId();
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setRepositoryId(savedRepositoryId);
		auth.setUserId(userId);
		auth.setAccessLevel(AccessLevel.ADMIN); // 项目创建者具有管理员权限
		auth.setCreateTime(LocalDateTime.now());
		auth.setCreateUserId(userId);
		repositoryAuthorizationDao.save(auth);
		
		List<Repository> repos = repositoryService.findCanAccessRepositoriesByUserId(userId);
		assertThat(repos).hasSize(1).allMatch(each -> each.getCreateUserName().equals("user_name"));
		
		repos = repositoryService.findCanAccessRepositoriesByUserId(userId + 1);
		assertThat(repos).isEmpty();
	}
	
	@Test
	public void find_can_access_projects_order_by_last_active_time_desc() {
		// 第一条记录
		Repository repository = new Repository();
		repository.setName("project_name");
		repository.setIsPublic(true);
		repository.setDescription("description");
		repository.setLastActiveTime(LocalDateTime.now());
		repository.setCreateUserId(1);
		repository.setCreateTime(LocalDateTime.now());
		repository.setCreateUserName("user_name");
		
		Integer savedRepositoryId = repositoryDao.save(repository).getId();
		
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setRepositoryId(savedRepositoryId);
		auth.setUserId(1);
		auth.setAccessLevel(AccessLevel.ADMIN);
		auth.setCreateTime(LocalDateTime.now());
		auth.setCreateUserId(1);
		repositoryAuthorizationDao.save(auth);
		
		// 第二条记录
		repository = new Repository();
		repository.setName("project_name_2");
		repository.setIsPublic(true);
		repository.setDescription("description");
		repository.setLastActiveTime(LocalDateTime.now().plusSeconds(1));
		repository.setCreateUserId(1);
		repository.setCreateTime(LocalDateTime.now());
		repository.setCreateUserName("user_name");
		
		savedRepositoryId = repositoryDao.save(repository).getId();
		
		auth = new RepositoryAuthorization();
		auth.setRepositoryId(savedRepositoryId);
		auth.setUserId(1);
		auth.setAccessLevel(AccessLevel.ADMIN);
		auth.setCreateTime(LocalDateTime.now());
		auth.setCreateUserId(1);
		repositoryAuthorizationDao.save(auth);
	
		List<Repository> projects = repositoryService.findCanAccessRepositoriesByUserId(1);
		assertThat(projects).hasSize(2).isSortedAccordingTo(Comparator.comparing(Repository::getLastActiveTime).reversed());
	}

	@Test
	public void find_latest_commit_success(@TempDir Path rootFolder) throws IOException {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		
		Repository repository = new Repository();
		repository.setName("project_name");
		repository.setIsPublic(true);
		repository.setDescription("description");
		repository.setLastActiveTime(LocalDateTime.now());
		repository.setCreateUserId(userId);
		repository.setCreateTime(LocalDateTime.now());
		repository.setCreateUserName("user_name");
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		
		Repository savedRepository = repositoryService.createRepository(userInfo, repository);
		
		GitCommitInfo commitInfo = repositoryService.findLatestCommitInfo(savedRepository, null).get();
		assertThat(commitInfo.getShortMessage()).isEqualTo("First Commit");
		assertThat(commitInfo.getUserName()).isEqualTo("user_name");
	}
	
	@Test
	public void find_latest_commit_for_untracked_and_empty_folder(@TempDir Path rootFolder) throws IOException {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		
		Repository repository = new Repository();
		repository.setName("project_name");
		repository.setIsPublic(true);
		repository.setDescription("description");
		repository.setLastActiveTime(LocalDateTime.now());
		repository.setCreateUserId(userId);
		repository.setCreateTime(LocalDateTime.now());
		repository.setCreateUserName("user_name");
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		
		Repository savedRepository = repositoryService.createRepository(userInfo, repository);
		
		RepositoryResource resource = new RepositoryResource();
		resource.setKey("group1");
		resource.setResourceType(RepositoryResourceType.GROUP);
		resource.setAppType(AppType.UNKNOWN);
		resource.setCreateTime(LocalDateTime.now());
		resource.setCreateUserId(userId);
		resource.setRepositoryId(savedRepository.getId());
		resource.setParentId(Constant.TREE_ROOT_ID);
		repositoryResourceService.insert(savedRepository, resource);
		
		assertThat(repositoryService.findLatestCommitInfo(savedRepository, "group1")).isEmpty();
	}

	@Test
	public void find_by_id_no_data() {
		Optional<Repository> repositoryOption = repositoryService.findById(1);
		assertThat(repositoryOption).isEmpty();
	}
	
	@Test
	public void find_by_id_success() {
		Repository repository = new Repository();
		repository.setName("project_name");
		repository.setIsPublic(true);
		repository.setLastActiveTime(LocalDateTime.now());
		repository.setCreateUserId(1);
		repository.setCreateTime(LocalDateTime.now());
		
		Repository savedRepository = repositoryDao.save(repository);
		
		Optional<Repository> repositoryOption = repositoryService.findById(savedRepository.getId());
		assertThat(repositoryOption).isPresent();
	}
}
