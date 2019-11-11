package com.blocklang.develop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jgit.lib.Constants;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.constant.Constant;
import com.blocklang.core.constant.GitFileStatus;
import com.blocklang.core.dao.UserDao;
import com.blocklang.core.git.exception.GitEmptyCommitException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.dao.ProjectCommitDao;
import com.blocklang.develop.dao.ProjectDependenceDao;
import com.blocklang.develop.dao.ProjectResourceDao;
import com.blocklang.develop.data.UncommittedFile;
import com.blocklang.develop.designer.data.AttachedWidget;
import com.blocklang.develop.designer.data.AttachedWidgetProperty;
import com.blocklang.develop.designer.data.PageModel;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectCommit;
import com.blocklang.develop.model.ProjectContext;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.develop.service.ProjectService;
import com.blocklang.marketplace.constant.ComponentAttrValueType;
import com.blocklang.marketplace.constant.Language;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.dao.ApiComponentAttrDao;
import com.blocklang.marketplace.dao.ApiComponentDao;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ApiComponent;
import com.blocklang.marketplace.model.ApiComponentAttr;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProjectResourceServiceImplTest extends AbstractServiceTest{

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	@Autowired
	private ProjectResourceService projectResourceService;
	@Autowired
	private ProjectResourceDao projectResourceDao;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserDao userDao;
	@Autowired
	private ProjectCommitDao projectCommitDao;
	@MockBean
	private PropertyService propertyService;
	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	@Autowired
	private ApiComponentDao apiComponentDao;
	@Autowired
	private ApiComponentAttrDao apiComponentAttrDao;
	@Autowired
	private ComponentRepoDao componentRepoDao;
	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	@Autowired
	private ProjectDependenceDao projectDependenceDao;
	
	@Test
	public void insert_if_not_set_seq() {
		Integer projectId = Integer.MAX_VALUE;
		
		Project project = new Project();
		project.setName("project");
		project.setCreateUserName("jack");
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setAppType(AppType.WEB);
		resource.setKey("key");
		resource.setName("name");
		resource.setResourceType(ProjectResourceType.PAGE);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		
		Integer id = projectResourceService.insert(project, resource).getId();
		
		Optional<ProjectResource> resourceOption = projectResourceDao.findById(id);
		assertThat(resourceOption).isPresent();
		assertThat(resourceOption.get().getSeq()).isEqualTo(1);
	}
	
	@Test
	public void insert_empty_page_with_a_root_widget() throws IOException {
		Integer userId = 1;
		// 创建一个标准库
		ApiRepo stdApiRepo = new ApiRepo();
		stdApiRepo.setCategory(RepoCategory.WIDGET);
		stdApiRepo.setGitRepoUrl("url");
		stdApiRepo.setGitRepoWebsite("website");
		stdApiRepo.setGitRepoOwner("owner");
		stdApiRepo.setGitRepoName("repo_name");
		stdApiRepo.setName("std-api-widget"); // 默认的标准库
		stdApiRepo.setVersion("0.0.1");
		stdApiRepo.setCreateUserId(userId);
		stdApiRepo.setCreateTime(LocalDateTime.now());
		Integer stdApiRepoId = apiRepoDao.save(stdApiRepo).getId();
		// 为标准库设置一个版本号
		// 创建对应的 API 仓库版本信息
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(stdApiRepoId);
		apiVersion.setVersion("0.0.1");
		apiVersion.setGitTagName("v0.0.1");
		apiVersion.setCreateUserId(userId);
		apiVersion.setCreateTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);

		// 在标准库中创建一个 Page 部件
		ApiComponent widget = new ApiComponent();
		String widgetCode = "0001";
		String widgetName = "Page";
		widget.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget.setCode(widgetCode);
		widget.setName(widgetName);
		widget.setCanHasChildren(true);
		widget.setCreateUserId(userId);
		widget.setCreateTime(LocalDateTime.now());
		ApiComponent savedWidget = apiComponentDao.save(widget);
		// 为 Page 部件添加一个属性
		ApiComponentAttr widgetProperty = new ApiComponentAttr();
		widgetProperty.setApiComponentId(savedWidget.getId());
		widgetProperty.setCode("0011");
		widgetProperty.setName("prop_name");
		widgetProperty.setDefaultValue("default_value");
		widgetProperty.setValueType(ComponentAttrValueType.STRING);
		apiComponentAttrDao.save(widgetProperty);
		
		// 创建一个 ide 版的组件库
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(stdApiRepoId);
		repo.setGitRepoUrl("url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("std-ide-widget");
		repo.setLabel("label");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		repo.setStd(true);
		repo.setIsIdeExtension(true);
		ComponentRepo savedComponentRepo = componentRepoDao.save(repo);
		// 创建一个 ide 版的组件库版本
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setComponentRepoId(savedComponentRepo.getId());
		version.setVersion("0.1.0");
		version.setGitTagName("v0.1.0");
		version.setApiRepoVersionId(savedApiRepoVersion.getId());
		version.setCreateUserId(1);
		version.setCreateTime(LocalDateTime.now());
		componentRepoVersionDao.save(version);
				
		Integer projectId = Integer.MAX_VALUE;
		
		Project project = new Project();
		project.setName("project");
		project.setCreateUserName("jack");
		
		ProjectResource resource = new ProjectResource();
		String pageKey = "key1";
		resource.setProjectId(projectId);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setAppType(AppType.WEB);
		resource.setKey(pageKey);
		resource.setName("name");
		resource.setResourceType(ProjectResourceType.PAGE);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		
		File rootFolder = tempFolder.newFolder();
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.getPath()));
		ProjectContext context = new ProjectContext("jack", "project", rootFolder.getPath());
		Files.createDirectories(context.getGitRepositoryDirectory());
		
		Integer pageId = projectResourceService.insert(project, resource).getId();
		
		PageModel actualPageModel = projectResourceService.getPageModel(projectId, pageId);
		
		assertThat(actualPageModel.getPageId()).isEqualTo(pageId);
		assertThat(actualPageModel.getWidgets()).hasSize(1);
		AttachedWidget actualRootWidget = actualPageModel.getWidgets().get(0);
		assertThat(actualRootWidget.getId()).hasSize(32);
		assertThat(actualRootWidget.getApiRepoId()).isEqualTo(stdApiRepoId);
		assertThat(actualRootWidget.getParentId()).isEqualTo(Constant.TREE_ROOT_ID.toString());
		// Page 部件的基本信息
		assertThat(actualRootWidget.getWidgetId()).isEqualTo(widget.getId());
		assertThat(actualRootWidget.getWidgetCode()).isEqualTo(widgetCode);
		assertThat(actualRootWidget.getWidgetName()).isEqualTo(widgetName);
		assertThat(actualRootWidget.getCanHasChildren()).isTrue();
		// Page 部件的属性信息
		assertThat(actualRootWidget.getProperties()).hasSize(1);
		AttachedWidgetProperty actualRootWidgetProperty1 = actualRootWidget.getProperties().get(0);
		assertThat(actualRootWidgetProperty1.getId()).hasSize(32);
		assertThat(actualRootWidgetProperty1.getCode()).isEqualTo("0011");
		assertThat(actualRootWidgetProperty1.getValueType()).isEqualTo(ComponentAttrValueType.STRING.getKey());
		assertThat(actualRootWidgetProperty1.getValue()).isEqualTo("default_value");
		assertThat(actualRootWidgetProperty1.getName()).isEqualTo("prop_name");
		
		// 校验 git 中的文件内容
		ObjectMapper mapper = new ObjectMapper();
		String mainPageJsonString = Files.readString(context.getGitRepositoryDirectory().resolve(pageKey + ".page.web.json"));
		assertThat(mapper.readValue(mainPageJsonString, PageModel.class)).usingRecursiveComparison().isEqualTo(actualPageModel);
	}
	
	@Test
	public void find_children_no_data() {
		List<ProjectResource> resources = projectResourceService.findChildren(null, 9999);
		assertThat(resources).isEmpty();
	}
	
	@Test
	public void find_children_at_root_has_two_project_success() {
		Project project = new Project();
		project.setCreateUserName("jack");
		project.setName("my-project");
		project.setId(1);
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(1);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		projectResourceDao.save(resource);
		
		resource = new ProjectResource();
		resource.setProjectId(2);
		resource.setKey("key2");
		resource.setName("name2");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		projectResourceDao.save(resource);
		
		List<ProjectResource> resources = projectResourceService.findChildren(project, Constant.TREE_ROOT_ID);
		assertThat(resources).hasSize(1);
	}
	
	@Test
	public void find_children_at_root_has_one_project_success() {
		Project project = new Project();
		project.setCreateUserName("jack");
		project.setName("my-project");
		project.setId(1);
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(1);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer savedResourceId = projectResourceDao.save(resource).getId();
		
		resource = new ProjectResource();
		resource.setProjectId(1);
		resource.setKey("key2");
		resource.setName("name2");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(savedResourceId);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		projectResourceDao.save(resource);
		
		List<ProjectResource> resources = projectResourceService.findChildren(project, Constant.TREE_ROOT_ID);
		assertThat(resources).hasSize(1);
	}
	
	@Test
	public void find_children_name_is_null_then_set_name_with_key() {
		Integer projectId = Integer.MAX_VALUE;
		
		Project project = new Project();
		project.setCreateUserName("jack");
		project.setName("my-project");
		project.setId(projectId);
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setKey("key1");
		resource.setName(null);
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		projectResourceDao.save(resource).getId();
		
		List<ProjectResource> resources = projectResourceService.findChildren(project, Constant.TREE_ROOT_ID);
		assertThat(resources).hasSize(1);
		assertThat(resources.get(0).getName()).isEqualTo("key1");
	}
	
	@Test
	public void find_children_at_sub_group() {
		Integer projectId = Integer.MAX_VALUE;
		
		Project project = new Project();
		project.setCreateUserName("jack");
		project.setName("my-project");
		project.setId(projectId);
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer savedResourceId = projectResourceDao.save(resource).getId();
		
		resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setKey("key2");
		resource.setName("name2");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(savedResourceId);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		projectResourceDao.save(resource);
		
		List<ProjectResource> resources = projectResourceService.findChildren(project, savedResourceId);
		assertThat(resources).hasSize(1);
		assertThat(resources.get(0).getKey()).isEqualTo("key2");
	}
	
	@Test
	public void find_parent_path_at_root() {
		String path = projectResourceService.findParentPath(-1);
		assertThat(path).isEmpty();
	}
	
	@Test
	public void find_parent_path_success() {
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(1);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer savedResourceId = projectResourceDao.save(resource).getId();
		
		resource = new ProjectResource();
		resource.setProjectId(1);
		resource.setKey("key2");
		resource.setName("name2");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(savedResourceId);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		savedResourceId = projectResourceDao.save(resource).getId();
		
		resource = new ProjectResource();
		resource.setProjectId(1);
		resource.setKey("key3");
		resource.setName("name3");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(savedResourceId);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		savedResourceId = projectResourceDao.save(resource).getId();
		
		String path = projectResourceService.findParentPath(savedResourceId);
		assertThat(path).isEqualTo("key1/key2/key3");
	}
	
	// 因为 getTitle 方法用到了 spring 的国际化帮助类，因为需要注入，所以将测试类放在 service 中
	@Test
	public void get_title_main() {
		ProjectResource resource = new ProjectResource();
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setResourceType(ProjectResourceType.PAGE);
		resource.setKey(ProjectResource.MAIN_KEY);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("首页");
	}
	
	@Test
	public void get_title_page() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.PAGE);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("页面");
	}
	
	@Test
	public void get_title_group() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("分组");
	}
	
	@Test
	public void get_title_templet() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.PAGE_TEMPLET);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("模板");
	}
	
	@Test
	public void get_title_readme() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.FILE);
		resource.setKey(ProjectResource.README_KEY);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("README");
	}
	
	@Test
	public void get_title_service() {
		ProjectResource resource = new ProjectResource();
		resource.setResourceType(ProjectResourceType.SERVICE);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("服务");
	}

	@Test
	public void find_by_id_no_data() {
		assertThat(projectResourceService.findById(Integer.MAX_VALUE)).isEmpty();
	}
	
	@Test
	public void find_by_id_success() {
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(1);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer savedResourceId = projectResourceDao.save(resource).getId();
		
		assertThat(projectResourceService.findById(savedResourceId)).isPresent();
	}
	
	@Test
	public void find_by_key_at_root_no_data() {
		assertThat(projectResourceService.findByKey(
				Integer.MAX_VALUE, 
				Constant.TREE_ROOT_ID, 
				ProjectResourceType.PAGE, 
				AppType.WEB,
				"not-exist-key")).isEmpty();
	}
	
	@Test
	public void find_by_key_at_root_success() {
		Integer projectId = Integer.MAX_VALUE;
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.PAGE);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		projectResourceDao.save(resource);
		
		assertThat(projectResourceService.findByKey(
				projectId, 
				Constant.TREE_ROOT_ID, 
				ProjectResourceType.PAGE, 
				AppType.WEB,
				"key1")).isPresent();
	}
	
	@Test
	public void find_by_name_at_root_no_data() {
		assertThat(projectResourceService.findByName(
				Integer.MAX_VALUE, 
				Constant.TREE_ROOT_ID, 
				ProjectResourceType.PAGE, 
				AppType.WEB,
				"not-exist-name")).isEmpty();
	}
	
	@Test
	public void find_by_name_at_root_success() {
		Integer projectId = Integer.MAX_VALUE;
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(ProjectResourceType.PAGE);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		projectResourceDao.save(resource);
		
		assertThat(projectResourceService.findByName(
				projectId, 
				Constant.TREE_ROOT_ID, 
				ProjectResourceType.PAGE, 
				AppType.WEB,
				"name1")).isPresent();
	}

	@Test
	public void find_parent_groups_by_parent_path_is_not_exist() {
		assertThat(projectResourceService.findParentGroupsByParentPath(null, null)).isEmpty();
		assertThat(projectResourceService.findParentGroupsByParentPath(null, "")).isEmpty();
	}
	
	@Test
	public void find_parent_id_by_parent_path_is_root_path() {
		assertThat(projectResourceService.findParentGroupsByParentPath(1, null)).isEmpty();
		assertThat(projectResourceService.findParentGroupsByParentPath(1, "")).isEmpty();
	}
	
	@Test
	public void find_parent_id_by_parent_path_is_one_level() {
		Integer projectId = Integer.MAX_VALUE;
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.UNKNOWN);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer resourceId = projectResourceDao.save(resource).getId();
		
		List<ProjectResource> groups = projectResourceService.findParentGroupsByParentPath(projectId, "key1");
		assertThat(groups).hasSize(1);
		assertThat(groups.get(0).getId()).isEqualTo(resourceId);
	}
	
	@Test
	public void find_parent_id_by_parent_path_is_two_level_success() {
		Integer projectId = Integer.MAX_VALUE;
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.UNKNOWN);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer resourceId = projectResourceDao.save(resource).getId();
		
		resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setKey("key2");
		resource.setName("name2");
		resource.setAppType(AppType.UNKNOWN);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(resourceId);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer resourceId2 = projectResourceDao.save(resource).getId();
		
		List<ProjectResource> groups = projectResourceService.findParentGroupsByParentPath(projectId, "key1/key2");
		assertThat(groups).hasSize(2);
		assertThat(groups.get(0).getId()).isEqualTo(resourceId);
		assertThat(groups.get(1).getId()).isEqualTo(resourceId2);
	}

	@Test
	public void find_parent_id_by_parent_path_is_two_level_not_exist() {
		Integer projectId = Integer.MAX_VALUE;
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.UNKNOWN);
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		projectResourceDao.save(resource).getId();
		
		assertThat(projectResourceService.findParentGroupsByParentPath(projectId, "key1/key2")).isEmpty();
	}

	@Test
	public void find_changes_success() throws IOException {
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
		
		Project savedProject = projectService.create(userInfo, project);
		
		ProjectResource resource = new ProjectResource();
		resource.setKey("page1");
		resource.setName("name1");
		resource.setResourceType(ProjectResourceType.PAGE);
		resource.setAppType(AppType.WEB);
		resource.setCreateTime(LocalDateTime.now());
		resource.setCreateUserId(userId);
		resource.setProjectId(savedProject.getId());
		resource.setParentId(Constant.TREE_ROOT_ID);
		projectResourceService.insert(savedProject, resource);
		
		// 有一个未跟踪的文件。
		List<UncommittedFile> changes = projectResourceService.findChanges(savedProject);
		
		assertThat(changes).hasSize(1);
		UncommittedFile file = changes.get(0);
		assertThat(file.getFullKeyPath()).isEqualTo("page1.page.web.json");
		assertThat(file.getGitStatus()).isEqualTo(GitFileStatus.UNTRACKED);
		assertThat(file.getIcon()).isEqualTo(AppType.WEB.getIcon());
		assertThat(file.getResourceName()).isEqualTo("name1");
		assertThat(file.getParentNamePath()).isBlank();
	}
	
	@Test
	public void find_changes_not_contain_group() throws IOException {
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
		
		Project savedProject = projectService.create(userInfo, project);
		
		// 有一个未跟踪的文件夹。
		ProjectResource resource = new ProjectResource();
		resource.setKey("group1");
		resource.setName("name1");
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setAppType(AppType.UNKNOWN);
		resource.setCreateTime(LocalDateTime.now());
		resource.setCreateUserId(userId);
		resource.setProjectId(savedProject.getId());
		resource.setParentId(Constant.TREE_ROOT_ID);
		projectResourceService.insert(savedProject, resource);
		
		List<UncommittedFile> changes = projectResourceService.findChanges(savedProject);
		
		assertThat(changes).isEmpty();
	}

	@Test
	public void find_changes_exist_two_level_parent_name_path_use_name() throws IOException {
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
		
		Project savedProject = projectService.create(userInfo, project);
		
		// 有一个未跟踪的文件夹。
		ProjectResource group1 = new ProjectResource();
		group1.setKey("group1");
		group1.setResourceType(ProjectResourceType.GROUP);
		group1.setAppType(AppType.UNKNOWN);
		group1.setCreateTime(LocalDateTime.now());
		group1.setCreateUserId(userId);
		group1.setProjectId(savedProject.getId());
		group1.setParentId(Constant.TREE_ROOT_ID);
		Integer group1Id = projectResourceService.insert(savedProject, group1).getId();
		
		ProjectResource group11 = new ProjectResource();
		group11.setKey("group11");
		group11.setResourceType(ProjectResourceType.GROUP);
		group11.setAppType(AppType.UNKNOWN);
		group11.setCreateTime(LocalDateTime.now());
		group11.setCreateUserId(userId);
		group11.setProjectId(savedProject.getId());
		group11.setParentId(group1Id);
		Integer group11Id = projectResourceService.insert(savedProject, group11).getId();
				
		ProjectResource page1 = new ProjectResource();
		page1.setKey("page111");
		page1.setResourceType(ProjectResourceType.PAGE);
		page1.setAppType(AppType.WEB);
		page1.setCreateTime(LocalDateTime.now());
		page1.setCreateUserId(userId);
		page1.setProjectId(savedProject.getId());
		page1.setParentId(group11Id);
		projectResourceService.insert(savedProject, page1);
		
		// 有一个未跟踪的文件。
		List<UncommittedFile> changes = projectResourceService.findChanges(savedProject);
		
		assertThat(changes).hasSize(1);
		UncommittedFile file = changes.get(0);
		assertThat(file.getFullKeyPath()).isEqualTo("group1/group11/page111.page.web.json");
		assertThat(file.getGitStatus()).isEqualTo(GitFileStatus.UNTRACKED);
		assertThat(file.getIcon()).isEqualTo(AppType.WEB.getIcon());
		assertThat(file.getResourceName()).isEqualTo("page111");
		// 最后以 / 结尾，表示是文件夹，而不是文件
		// 文件后没有 /
		assertThat(file.getParentNamePath()).isEqualTo("group1/group11/"); 
	}
	
	@Test
	public void stage_changes_success() throws IOException {
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
		
		Project savedProject = projectService.create(userInfo, project);
		
		ProjectResource resource = new ProjectResource();
		resource.setKey("page1");
		resource.setName("name1");
		resource.setResourceType(ProjectResourceType.PAGE);
		resource.setAppType(AppType.WEB);
		resource.setCreateTime(LocalDateTime.now());
		resource.setCreateUserId(userId);
		resource.setProjectId(savedProject.getId());
		resource.setParentId(Constant.TREE_ROOT_ID);
		projectResourceService.insert(savedProject, resource);
		
		projectResourceService.stageChanges(savedProject, new String[] {"page1.page.web.json"});
		// 有一个已跟踪，但未提交的文件。
		List<UncommittedFile> changes = projectResourceService.findChanges(savedProject);
		
		assertThat(changes).hasSize(1);
		
		UncommittedFile file = changes.get(0);
		assertThat(file.getFullKeyPath()).isEqualTo("page1.page.web.json");
		assertThat(file.getGitStatus()).isEqualTo(GitFileStatus.ADDED);
		assertThat(file.getIcon()).isEqualTo(AppType.WEB.getIcon());
		assertThat(file.getResourceName()).isEqualTo("name1");
		assertThat(file.getParentNamePath()).isBlank();
	}
	
	@Test
	public void unstage_changes_success() throws IOException {
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
		
		Project savedProject = projectService.create(userInfo, project);
		
		ProjectResource resource = new ProjectResource();
		resource.setKey("page1");
		resource.setName("name1");
		resource.setResourceType(ProjectResourceType.PAGE);
		resource.setAppType(AppType.WEB);
		resource.setCreateTime(LocalDateTime.now());
		resource.setCreateUserId(userId);
		resource.setProjectId(savedProject.getId());
		resource.setParentId(Constant.TREE_ROOT_ID);
		projectResourceService.insert(savedProject, resource);
		
		projectResourceService.stageChanges(savedProject, new String[] {"page1.page.web.json"});
		projectResourceService.unstageChanges(savedProject, new String[] {"page1.page.web.json"});
		// 有一个已跟踪，但未提交的文件。
		List<UncommittedFile> changes = projectResourceService.findChanges(savedProject);
		
		assertThat(changes).hasSize(1);
		
		UncommittedFile file = changes.get(0);
		assertThat(file.getFullKeyPath()).isEqualTo("page1.page.web.json");
		assertThat(file.getGitStatus()).isEqualTo(GitFileStatus.UNTRACKED);
		assertThat(file.getIcon()).isEqualTo(AppType.WEB.getIcon());
		assertThat(file.getResourceName()).isEqualTo("name1");
		assertThat(file.getParentNamePath()).isBlank();
	}
	
	@Test(expected = GitEmptyCommitException.class)
	public void commit_no_stage() throws IOException {
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
		
		Project savedProject = projectService.create(userInfo, project);
		
		List<UncommittedFile> changes = projectResourceService.findChanges(savedProject);
		assertThat(changes).isEmpty();
		
		projectResourceService.commit(userInfo, savedProject, "commit page1");
	}
	
	@Test
	public void commit_success() throws IOException {
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
		
		Project savedProject = projectService.create(userInfo, project);
		
		ProjectResource resource = new ProjectResource();
		resource.setKey("page1");
		resource.setName("name1");
		resource.setResourceType(ProjectResourceType.PAGE);
		resource.setAppType(AppType.WEB);
		resource.setCreateTime(LocalDateTime.now());
		resource.setCreateUserId(userId);
		resource.setProjectId(savedProject.getId());
		resource.setParentId(Constant.TREE_ROOT_ID);
		projectResourceService.insert(savedProject, resource);
		
		projectResourceService.stageChanges(savedProject, new String[] {"page1.page.web.json"});
		String commitId = projectResourceService.commit(userInfo, savedProject, "commit page1");
		// 有一个已跟踪，但未提交的文件。
		List<UncommittedFile> changes = projectResourceService.findChanges(savedProject);
		assertThat(changes).hasSize(0);
		
		Optional<ProjectCommit> commitOption = projectCommitDao.findByProjectIdAndBranchAndCommitId(savedProject.getId(), Constants.MASTER, commitId);
		assertThat(commitOption).isPresent();
	}

	// 按照数据类型测试，如测试
	// 1. 新增部件
	// 2. 新增属性
	// 3. 新增事件
	// 4. 新增数据
	// 5. 新增函数
	// 6. 新增服务
	
	// 新创建一个页面模型
	// 断言返回的页面模型与录入的页面模型，数据和顺序都完全一致。
	@Test
	public void update_page_model_widgets_new() {
		// 初始化数据
		// 1. 创建一个 API 仓库，类型为 Widget
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setGitRepoUrl("a");
		apiRepo.setGitRepoWebsite("b");
		apiRepo.setGitRepoOwner("c");
		apiRepo.setGitRepoName("d");
		apiRepo.setName("e");
		apiRepo.setVersion("f");
		apiRepo.setCategory(RepoCategory.WIDGET);
		apiRepo.setCreateUserId(1);
		apiRepo.setCreateTime(LocalDateTime.now());
		ApiRepo savedApiRepo = apiRepoDao.save(apiRepo);
		// 2. 创建一个 API 版本号
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);
		// 3. 在对应的 API 版本下添加两个部件，分别为每一个部件设置一个属性
		// 3.1 部件1
		ApiComponent widget1 = new ApiComponent();
		widget1.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget1.setCode("0001");
		widget1.setName("Widget1");
		widget1.setLabel("Widget 1");
		widget1.setDescription("Description1");
		widget1.setCanHasChildren(true);
		widget1.setCreateUserId(1);
		widget1.setCreateTime(LocalDateTime.now());
		ApiComponent savedWidget1 = apiComponentDao.save(widget1);
		// 3.1 属性1
		ApiComponentAttr widgetProperty11 = new ApiComponentAttr();
		widgetProperty11.setApiComponentId(savedWidget1.getId());
		widgetProperty11.setCode("0011");
		widgetProperty11.setDefaultValue("default_value_11");
		widgetProperty11.setDescription("description_11");
		widgetProperty11.setName("prop_name_11");
		widgetProperty11.setValueType(ComponentAttrValueType.STRING);
		apiComponentAttrDao.save(widgetProperty11);
		// 3.2 部件2
		ApiComponent widget2 = new ApiComponent();
		widget2.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget2.setCode("0002");
		widget2.setName("Widget2");
		widget2.setDescription("Description2");
		widget2.setCanHasChildren(false);
		widget2.setCreateUserId(1);
		widget2.setCreateTime(LocalDateTime.now());
		ApiComponent savedWidget2 = apiComponentDao.save(widget2);
		// 3.2  属性2
		ApiComponentAttr widgetProperty21 = new ApiComponentAttr();
		widgetProperty21.setApiComponentId(savedWidget2.getId());
		widgetProperty21.setCode("0021");
		widgetProperty21.setDefaultValue("default_value_21");
		widgetProperty21.setDescription("description_21");
		widgetProperty21.setName("prop_name_21");
		widgetProperty21.setLabel("prop_label_21");
		widgetProperty21.setValueType(ComponentAttrValueType.STRING);
		apiComponentAttrDao.save(widgetProperty21);
		// 4. 创建一个 ide 组件库，实现上述的 API 仓库
		//    因为在查数据时，可直接获得 ide 组件仓库的版本信息
		//    然后根据版本信息，可直接获取 API 组件库的版本信息
		//    所以准备数据时，可跳过这一步。
		// 5. 为 ide 组件库创建一个版本号，实现上述的 API 版本
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1); // 组件仓库 id 为 1
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 6. 创建一个项目
		//    为项目添加依赖时，直接使用项目标识即可
		//    所以准备数据时，可跳过这一步
		Integer projectId = 1;
		// 7. 将创建的 ide 组件库的一个版本添加为项目依赖
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(projectId);
		dependence.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependence.setCreateUserId(1);
		dependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(dependence);
		
		// 新增页面模型
		PageModel model = new PageModel();
		
		Integer pageId = 1;
		model.setPageId(pageId);
		
		AttachedWidget attachedWidget1 = new AttachedWidget();
		attachedWidget1.setId("1");
		attachedWidget1.setParentId(Constant.TREE_ROOT_ID.toString());
		attachedWidget1.setApiRepoId(savedApiRepo.getId());
		attachedWidget1.setWidgetCode("0001");
		attachedWidget1.setWidgetName("Widget 1"); // 如果 label 有值，则用 label 的值
		attachedWidget1.setWidgetId(savedWidget1.getId());
		attachedWidget1.setCanHasChildren(true);
		AttachedWidgetProperty attachedWidgetProperty11 = new AttachedWidgetProperty();
		attachedWidgetProperty11.setId("11");
		attachedWidgetProperty11.setCode("0011");
		attachedWidgetProperty11.setName("prop_name_11"); // 注意，不会直接在页面模型中存储该值，如果 label 有值，则优先取 label 值
		attachedWidgetProperty11.setValue("value11");
		attachedWidgetProperty11.setValueType(ComponentAttrValueType.STRING.getKey());
		attachedWidget1.setProperties(Collections.singletonList(attachedWidgetProperty11));
		
		AttachedWidget attachedWidget2 = new AttachedWidget();
		attachedWidget2.setId("2"); // id 是在前台生成的
		attachedWidget2.setParentId("1"); // 父部件是 widget1
		attachedWidget2.setApiRepoId(savedApiRepo.getId());
		attachedWidget2.setWidgetCode("0002");
		attachedWidget2.setWidgetName("Widget2"); // 如果 label 有值，则用 label 的值
		attachedWidget2.setWidgetId(savedWidget2.getId());
		attachedWidget2.setCanHasChildren(false);
		AttachedWidgetProperty attachedWidgetProperty21 = new AttachedWidgetProperty();
		attachedWidgetProperty21.setId("21"); // id 是在前台生成的
		attachedWidgetProperty21.setCode("0021");
		attachedWidgetProperty21.setName("prop_label_21");
		attachedWidgetProperty21.setValue("value21");
		attachedWidgetProperty21.setValueType(ComponentAttrValueType.STRING.getKey());
		attachedWidget2.setProperties(Collections.singletonList(attachedWidgetProperty21));
		
		model.setWidgets(Arrays.asList(attachedWidget1, attachedWidget2));

		projectResourceService.updatePageModel(null, null, model);
		
		PageModel savedModel = projectResourceService.getPageModel(pageId, projectId);
		
		assertThat(savedModel).usingRecursiveComparison().isEqualTo(model);
	}
	
	// 更新页面模型
	@Test
	public void update_page_model_widgets_update() {
		// 初始化数据
		// 1. 创建一个 API 仓库，类型为 Widget
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setGitRepoUrl("a");
		apiRepo.setGitRepoWebsite("b");
		apiRepo.setGitRepoOwner("c");
		apiRepo.setGitRepoName("d");
		apiRepo.setName("e");
		apiRepo.setVersion("f");
		apiRepo.setCategory(RepoCategory.WIDGET);
		apiRepo.setCreateUserId(1);
		apiRepo.setCreateTime(LocalDateTime.now());
		ApiRepo savedApiRepo = apiRepoDao.save(apiRepo);
		// 2. 创建一个 API 版本号
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);
		// 3. 在对应的 API 版本下添加两个部件，分别为每一个部件设置一个属性
		//  部件
		ApiComponent widget1 = new ApiComponent();
		widget1.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget1.setCode("0001");
		widget1.setName("Widget1");
		widget1.setLabel("Widget 1");
		widget1.setDescription("Description1");
		widget1.setCanHasChildren(true);
		widget1.setCreateUserId(1);
		widget1.setCreateTime(LocalDateTime.now());
		ApiComponent savedWidget1 = apiComponentDao.save(widget1);
		//  属性
		ApiComponentAttr widgetProperty11 = new ApiComponentAttr();
		widgetProperty11.setApiComponentId(savedWidget1.getId());
		widgetProperty11.setCode("0011");
		widgetProperty11.setDefaultValue("default_value_11");
		widgetProperty11.setDescription("description_11");
		widgetProperty11.setName("prop_name_11");
		widgetProperty11.setValueType(ComponentAttrValueType.STRING);
		apiComponentAttrDao.save(widgetProperty11);
		
		// 4. 创建一个 ide 组件库，实现上述的 API 仓库
		//    因为在查数据时，可直接获得 ide 组件仓库的版本信息
		//    然后根据版本信息，可直接获取 API 组件库的版本信息
		//    所以准备数据时，可跳过这一步。
		// 5. 为 ide 组件库创建一个版本号，实现上述的 API 版本
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1); // 组件仓库 id 为 1
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 6. 创建一个项目
		//    为项目添加依赖时，直接使用项目标识即可
		//    所以准备数据时，可跳过这一步
		Integer projectId = 1;
		// 7. 将创建的 ide 组件库的一个版本添加为项目依赖
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(projectId);
		dependence.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependence.setCreateUserId(1);
		dependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(dependence);
		
		// 新增页面模型
		PageModel model = new PageModel();
		
		Integer pageId = 1;
		model.setPageId(pageId);
		
		AttachedWidget attachedWidget1 = new AttachedWidget();
		attachedWidget1.setId("1");
		attachedWidget1.setParentId(Constant.TREE_ROOT_ID.toString());
		attachedWidget1.setApiRepoId(savedApiRepo.getId());
		attachedWidget1.setWidgetCode("0001");
		attachedWidget1.setWidgetName("Widget 1"); // 如果 label 有值，则用 label 的值
		attachedWidget1.setWidgetId(savedWidget1.getId());
		attachedWidget1.setCanHasChildren(true);
		AttachedWidgetProperty attachedWidgetProperty11 = new AttachedWidgetProperty();
		attachedWidgetProperty11.setId("11");
		attachedWidgetProperty11.setCode("0011");
		attachedWidgetProperty11.setName("prop_name_11"); // 注意，不会直接在页面模型中存储该值，如果 label 有值，则优先取 label 值
		attachedWidgetProperty11.setValue("value11");
		attachedWidgetProperty11.setValueType(ComponentAttrValueType.STRING.getKey());
		attachedWidget1.setProperties(Collections.singletonList(attachedWidgetProperty11));
		
		model.setWidgets(Collections.singletonList(attachedWidget1));
		
		projectResourceService.updatePageModel(null, null, model); // 第一次执行
		projectResourceService.updatePageModel(null, null, model); // 第二次执行
		
		PageModel savedModel = projectResourceService.getPageModel(pageId, projectId);
		
		assertThat(savedModel).usingRecursiveComparison().isEqualTo(model);
	}
	
	// 要测试，API 库的版本升级后，部件的属性增加的情况。
	// 此时 API 部件属性列表中有新增属性，但是之前存的页面模型中不包含新增属性
	// 则在查询时要包含新增属性
	@Test
	public void update_page_model_upgrade() {
		// 初始化数据
		// 1. 创建一个 API 仓库，类型为 Widget
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setGitRepoUrl("a");
		apiRepo.setGitRepoWebsite("b");
		apiRepo.setGitRepoOwner("c");
		apiRepo.setGitRepoName("d");
		apiRepo.setName("e");
		apiRepo.setVersion("f");
		apiRepo.setCategory(RepoCategory.WIDGET);
		apiRepo.setCreateUserId(1);
		apiRepo.setCreateTime(LocalDateTime.now());
		ApiRepo savedApiRepo = apiRepoDao.save(apiRepo);
		// 2. 创建一个 API 版本号
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);
		// 3. 在对应的 API 版本下添加两个部件，分别为每一个部件设置一个属性
		// 3.1 部件1
		ApiComponent widget1 = new ApiComponent();
		widget1.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget1.setCode("0001");
		widget1.setName("Widget1");
		widget1.setLabel("Widget 1");
		widget1.setDescription("Description1");
		widget1.setCanHasChildren(true);
		widget1.setCreateUserId(1);
		widget1.setCreateTime(LocalDateTime.now());
		ApiComponent savedWidget1 = apiComponentDao.save(widget1);
		// 3.1 属性1
		ApiComponentAttr widgetProperty11 = new ApiComponentAttr();
		widgetProperty11.setApiComponentId(savedWidget1.getId());
		widgetProperty11.setCode("0011");
		widgetProperty11.setDefaultValue("default_value_11");
		widgetProperty11.setDescription("description_11");
		widgetProperty11.setName("prop_name_11");
		widgetProperty11.setValueType(ComponentAttrValueType.STRING);
		apiComponentAttrDao.save(widgetProperty11);
		// 4. 创建一个 ide 组件库，实现上述的 API 仓库
		//    因为在查数据时，可直接获得 ide 组件仓库的版本信息
		//    然后根据版本信息，可直接获取 API 组件库的版本信息
		//    所以准备数据时，可跳过这一步。
		// 5. 为 ide 组件库创建一个版本号，实现上述的 API 版本
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1); // 组件仓库 id 为 1
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 6. 创建一个项目
		//    为项目添加依赖时，直接使用项目标识即可
		//    所以准备数据时，可跳过这一步
		Integer projectId = 1;
		// 7. 将创建的 ide 组件库的一个版本添加为项目依赖
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(projectId);
		dependence.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependence.setCreateUserId(1);
		dependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(dependence);
		
		// 新增页面模型
		PageModel model = new PageModel();
		
		Integer pageId = 1;
		model.setPageId(pageId);
		
		AttachedWidget attachedWidget1 = new AttachedWidget();
		attachedWidget1.setId("1");
		attachedWidget1.setParentId(Constant.TREE_ROOT_ID.toString());
		attachedWidget1.setApiRepoId(savedApiRepo.getId());
		attachedWidget1.setWidgetCode("0001");
		attachedWidget1.setWidgetName("Widget 1"); // 如果 label 有值，则用 label 的值
		attachedWidget1.setWidgetId(savedWidget1.getId());
		attachedWidget1.setCanHasChildren(true);
		
		model.setWidgets(Collections.singletonList(attachedWidget1));
		
		projectResourceService.updatePageModel(null, null, model);
		
		PageModel result = projectResourceService.getPageModel(pageId, projectId);
		
		assertThat(result.getWidgets().get(0).getProperties()).hasSize(1);
		
		AttachedWidgetProperty actualAttachedWidgetProperty = result.getWidgets().get(0).getProperties().get(0);
		
		assertThat(actualAttachedWidgetProperty.getCode()).isEqualTo("0011");
		assertThat(actualAttachedWidgetProperty.getName()).isEqualTo("prop_name_11");
		assertThat(actualAttachedWidgetProperty.getValueType()).isEqualTo("string");
		
		assertThat(actualAttachedWidgetProperty.getId()).hasSize(32); // uuid
		assertThat(actualAttachedWidgetProperty.getValue()).isEqualTo("default_value_11");
	}

	@Test
	public void update_page_model_widgets_read_page_file_in_git() throws IOException {
		// 初始化数据
		// 1. 创建一个 API 仓库，类型为 Widget
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setGitRepoUrl("a");
		apiRepo.setGitRepoWebsite("b");
		apiRepo.setGitRepoOwner("c");
		apiRepo.setGitRepoName("d");
		apiRepo.setName("e");
		apiRepo.setVersion("f");
		apiRepo.setCategory(RepoCategory.WIDGET);
		apiRepo.setCreateUserId(1);
		apiRepo.setCreateTime(LocalDateTime.now());
		ApiRepo savedApiRepo = apiRepoDao.save(apiRepo);
		// 2. 创建一个 API 版本号
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);
		// 3. 在对应的 API 版本下添加两个部件，分别为每一个部件设置一个属性
		// 3.1 部件1
		ApiComponent widget1 = new ApiComponent();
		widget1.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget1.setCode("0001");
		widget1.setName("Widget1");
		widget1.setLabel("Widget 1");
		widget1.setDescription("Description1");
		widget1.setCanHasChildren(true);
		widget1.setCreateUserId(1);
		widget1.setCreateTime(LocalDateTime.now());
		ApiComponent savedWidget1 = apiComponentDao.save(widget1);
		// 3.1 属性1
		ApiComponentAttr widgetProperty11 = new ApiComponentAttr();
		widgetProperty11.setApiComponentId(savedWidget1.getId());
		widgetProperty11.setCode("0011");
		widgetProperty11.setDefaultValue("default_value_11");
		widgetProperty11.setDescription("description_11");
		widgetProperty11.setName("prop_name_11");
		widgetProperty11.setValueType(ComponentAttrValueType.STRING);
		apiComponentAttrDao.save(widgetProperty11);
		// 3.2 部件2
		ApiComponent widget2 = new ApiComponent();
		widget2.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget2.setCode("0002");
		widget2.setName("Widget2");
		widget2.setDescription("Description2");
		widget2.setCanHasChildren(false);
		widget2.setCreateUserId(1);
		widget2.setCreateTime(LocalDateTime.now());
		ApiComponent savedWidget2 = apiComponentDao.save(widget2);
		// 3.2  属性2
		ApiComponentAttr widgetProperty21 = new ApiComponentAttr();
		widgetProperty21.setApiComponentId(savedWidget2.getId());
		widgetProperty21.setCode("0021");
		widgetProperty21.setDefaultValue("default_value_21");
		widgetProperty21.setDescription("description_21");
		widgetProperty21.setName("prop_name_21");
		widgetProperty21.setLabel("prop_label_21");
		widgetProperty21.setValueType(ComponentAttrValueType.STRING);
		apiComponentAttrDao.save(widgetProperty21);
		// 4. 创建一个 ide 组件库，实现上述的 API 仓库
		//    因为在查数据时，可直接获得 ide 组件仓库的版本信息
		//    然后根据版本信息，可直接获取 API 组件库的版本信息
		//    所以准备数据时，可跳过这一步。
		// 5. 为 ide 组件库创建一个版本号，实现上述的 API 版本
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1); // 组件仓库 id 为 1
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 6. 创建一个项目
		//    为项目添加依赖时，直接使用项目标识即可
		//    所以准备数据时，可跳过这一步
		Integer projectId = 1;
		// 7. 将创建的 ide 组件库的一个版本添加为项目依赖
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(projectId);
		dependence.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependence.setCreateUserId(1);
		dependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(dependence);
		
		// 新增页面模型
		PageModel model = new PageModel();
		
		Integer pageId = 1;
		model.setPageId(pageId);
		
		AttachedWidget attachedWidget1 = new AttachedWidget();
		attachedWidget1.setId("1");
		attachedWidget1.setParentId(Constant.TREE_ROOT_ID.toString());
		attachedWidget1.setApiRepoId(savedApiRepo.getId());
		attachedWidget1.setWidgetCode("0001");
		attachedWidget1.setWidgetName("Widget 1"); // 如果 label 有值，则用 label 的值
		attachedWidget1.setWidgetId(savedWidget1.getId());
		attachedWidget1.setCanHasChildren(true);
		AttachedWidgetProperty attachedWidgetProperty11 = new AttachedWidgetProperty();
		attachedWidgetProperty11.setId("11");
		attachedWidgetProperty11.setCode("0011");
		attachedWidgetProperty11.setName("prop_name_11"); // 注意，不会直接在页面模型中存储该值，如果 label 有值，则优先取 label 值
		attachedWidgetProperty11.setValue("value11");
		attachedWidgetProperty11.setValueType(ComponentAttrValueType.STRING.getKey());
		attachedWidget1.setProperties(Collections.singletonList(attachedWidgetProperty11));
		
		AttachedWidget attachedWidget2 = new AttachedWidget();
		attachedWidget2.setId("2"); // id 是在前台生成的
		attachedWidget2.setParentId("1"); // 父部件是 widget1
		attachedWidget2.setApiRepoId(savedApiRepo.getId());
		attachedWidget2.setWidgetCode("0002");
		attachedWidget2.setWidgetName("Widget2"); // 如果 label 有值，则用 label 的值
		attachedWidget2.setWidgetId(savedWidget2.getId());
		attachedWidget2.setCanHasChildren(false);
		AttachedWidgetProperty attachedWidgetProperty21 = new AttachedWidgetProperty();
		attachedWidgetProperty21.setId("21"); // id 是在前台生成的
		attachedWidgetProperty21.setCode("0021");
		attachedWidgetProperty21.setName("prop_label_21");
		attachedWidgetProperty21.setValue("value21");
		attachedWidgetProperty21.setValueType(ComponentAttrValueType.STRING.getKey());
		attachedWidget2.setProperties(Collections.singletonList(attachedWidgetProperty21));
		
		model.setWidgets(Arrays.asList(attachedWidget1, attachedWidget2));

		Project project = new Project();
		project.setName("project");
		project.setCreateUserName("jack");
		
		ProjectResource resource = new ProjectResource();
		String pageKey = "key1";
		resource.setProjectId(projectId);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setAppType(AppType.WEB);
		resource.setKey(pageKey);
		resource.setName("name");
		resource.setResourceType(ProjectResourceType.PAGE);
		
		File rootFolder = tempFolder.newFolder();
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.getPath()));
		ProjectContext context = new ProjectContext("jack", "project", rootFolder.getPath());
		Files.createDirectories(context.getGitRepositoryDirectory());
		
		projectResourceService.updatePageModel(project, resource, model);
		
		ObjectMapper mapper = new ObjectMapper();
		String mainPageJsonString = Files.readString(context.getGitRepositoryDirectory().resolve(pageKey + ".page.web.json"));
		assertThat(mapper.readValue(mainPageJsonString, PageModel.class)).usingRecursiveComparison().isEqualTo(model);
	}
}
