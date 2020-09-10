package com.blocklang.develop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jgit.lib.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
import com.blocklang.core.service.UserService;
import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.NodeCategory;
import com.blocklang.develop.constant.NodeLayout;
import com.blocklang.develop.constant.RepositoryResourceType;
import com.blocklang.develop.dao.ProjectDependencyDao;
import com.blocklang.develop.dao.RepositoryCommitDao;
import com.blocklang.develop.dao.RepositoryResourceDao;
import com.blocklang.develop.data.UncommittedFile;
import com.blocklang.develop.designer.data.AttachedWidget;
import com.blocklang.develop.designer.data.AttachedWidgetProperty;
import com.blocklang.develop.designer.data.DataPort;
import com.blocklang.develop.designer.data.EventArgument;
import com.blocklang.develop.designer.data.InputDataPort;
import com.blocklang.develop.designer.data.InputSequencePort;
import com.blocklang.develop.designer.data.NodeConnection;
import com.blocklang.develop.designer.data.OutputSequencePort;
import com.blocklang.develop.designer.data.PageEventHandler;
import com.blocklang.develop.designer.data.PageModel;
import com.blocklang.develop.designer.data.VisualNode;
import com.blocklang.develop.model.PageDataItem;
import com.blocklang.develop.model.ProjectDependency;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryCommit;
import com.blocklang.develop.model.RepositoryContext;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.RepositoryResourceService;
import com.blocklang.develop.service.RepositoryService;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.constant.RepoType;
import com.blocklang.marketplace.constant.WidgetPropertyValueType;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ApiWidgetDao;
import com.blocklang.marketplace.dao.ApiWidgetEventArgDao;
import com.blocklang.marketplace.dao.ApiWidgetPropertyDao;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ApiWidget;
import com.blocklang.marketplace.model.ApiWidgetEventArg;
import com.blocklang.marketplace.model.ApiWidgetProperty;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;

public class RepositoryResourceServiceImplTest extends AbstractServiceTest{

	@Autowired
	private RepositoryResourceService repositoryResourceService;
	@Autowired
	private RepositoryResourceDao repositortResourceDao;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private UserDao userDao;
	@MockBean
	private UserService userService;
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
	@Autowired
	private ApiWidgetEventArgDao apiComponentAttrFunArgDao;
	@Autowired
	private ComponentRepoDao componentRepoDao;
	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	@Autowired
	private ProjectDependencyDao projectDependencyDao;
	
	@Test
	public void insertIfNotSetSeq() {
		Integer repositoryId = Integer.MAX_VALUE;
		
		Repository repository = new Repository();
		repository.setName("repo1");
		repository.setCreateUserName("jack1");
		
		RepositoryResource resource = new RepositoryResource();
		resource.setRepositoryId(repositoryId);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setAppType(AppType.WEB);
		resource.setKey("key");
		resource.setName("name");
		resource.setResourceType(RepositoryResourceType.PAGE);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		
		Integer resourceId = repositoryResourceService.insert(repository, resource).getId();
		
		Optional<RepositoryResource> resourceOption = repositortResourceDao.findById(resourceId);
		assertThat(resourceOption.get().getSeq()).isEqualTo(1);
	}
	
	@Test
	public void insertEmptyPageWithARootWidget(@TempDir Path rootFolder) throws IOException {
		Integer userId = 1;
		// 创建一个标准库
		String stdApiRepoUrl = "std-api-widget-url";
		ApiRepo stdApiRepo = new ApiRepo();
		stdApiRepo.setCategory(RepoCategory.WIDGET);
		stdApiRepo.setGitRepoUrl(stdApiRepoUrl);
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

		// 在标准库中创建一个 Page 部件
		ApiWidget widget = new ApiWidget();
		String widgetCode = "0001";
		String widgetName = "Page";
		widget.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget.setCode(widgetCode);
		widget.setName(widgetName);
		widget.setCreateUserId(userId);
		widget.setCreateTime(LocalDateTime.now());
		ApiWidget savedWidget = apiComponentDao.save(widget);
		// 为 Page 部件添加一个属性
		ApiWidgetProperty widgetProperty = new ApiWidgetProperty();
		widgetProperty.setApiRepoVersionId(savedApiRepoVersion.getId());
		widgetProperty.setApiWidgetId(savedWidget.getId());
		widgetProperty.setCode("0011");
		widgetProperty.setName("prop_name");
		widgetProperty.setDefaultValue("default_value");
		widgetProperty.setValueType(WidgetPropertyValueType.STRING.getKey());
		apiComponentAttrDao.save(widgetProperty);
		
		// 创建一个 ide 版的组件库
		String stdIdeRepoUrl = "std-ide-widget-url";
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl(stdIdeRepoUrl);
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(userId);
		repo.setCreateTime(LocalDateTime.now());
		repo.setRepoType(RepoType.IDE);
		repo.setLastPublishTime(LocalDateTime.now());
		ComponentRepo savedComponentRepo = componentRepoDao.save(repo);
		// 创建一个 ide 版的组件库版本
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setComponentRepoId(savedComponentRepo.getId());
		version.setApiRepoVersionId(savedApiRepoVersion.getId());
		version.setName("name");
		version.setVersion("master"); // 标准库只依赖 master 分支
		version.setGitTagName("refs/heads/master");
		version.setAppType(AppType.WEB);
		version.setBuild("dojo");
		version.setCreateUserId(1);
		version.setCreateTime(LocalDateTime.now());
		version.setLastPublishTime(LocalDateTime.now());
		componentRepoVersionDao.save(version);
				
		Integer repositoryId = 2;
		String owner = "jack";
		String repoName = "repo1";
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		repository.setName(repoName);
		repository.setCreateUserName(owner);
		
		String projectKey = "project1";
		RepositoryResource project = new RepositoryResource();
		project.setRepositoryId(repositoryId);
		project.setParentId(Constant.TREE_ROOT_ID);
		project.setAppType(AppType.WEB);
		project.setKey(projectKey);
		project.setName("project 1");
		project.setResourceType(RepositoryResourceType.PROJECT);
		project.setCreateUserId(userId);
		project.setCreateTime(LocalDateTime.now());
		project.setSeq(1);
		Integer projectId = repositortResourceDao.save(project).getId();
		
		RepositoryResource page = new RepositoryResource();
		String pageKey = "key1";
		page.setRepositoryId(repositoryId);
		page.setParentId(projectId);
		page.setAppType(AppType.WEB);
		page.setKey(pageKey);
		page.setName("name");
		page.setResourceType(RepositoryResourceType.PAGE);
		page.setCreateUserId(1);
		page.setCreateTime(LocalDateTime.now());
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		RepositoryContext context = new RepositoryContext(owner, repoName, rootFolder.toString());
		Path projectPath = context.getGitRepositoryDirectory().resolve(projectKey);
		Files.createDirectories(projectPath);
		
		when(propertyService.findStringValue(CmPropKey.STD_WIDGET_API_GIT_URL, "")).thenReturn(stdApiRepoUrl);
		when(propertyService.findStringValue(CmPropKey.STD_WIDGET_IDE_GIT_URL, "")).thenReturn(stdIdeRepoUrl);
		when(propertyService.findIntegerValue(CmPropKey.STD_REPO_REGISTER_USER_ID, 1)).thenReturn(1);
		when(propertyService.findStringValue(CmPropKey.STD_WIDGET_ROOT_NAME, "Page")).thenReturn("Page");
		
		RepositoryResource pageResource = repositoryResourceService.insert(repository, page);
		
		PageModel actualPageModel = repositoryResourceService.getPageModel(pageResource);
		
		assertThat(actualPageModel.getPageId()).isEqualTo(pageResource.getId());
		assertThat(actualPageModel.getWidgets()).hasSize(1);
		AttachedWidget actualRootWidget = actualPageModel.getWidgets().get(0);
		assertThat(actualRootWidget.getId()).hasSize(32);
		assertThat(actualRootWidget.getApiRepoId()).isEqualTo(stdApiRepoId);
		assertThat(actualRootWidget.getParentId()).isEqualTo(Constant.TREE_ROOT_ID.toString());
		// Page 部件的基本信息
		assertThat(actualRootWidget.getWidgetId()).isEqualTo(widget.getId());
		assertThat(actualRootWidget.getWidgetCode()).isEqualTo(widgetCode);
		assertThat(actualRootWidget.getWidgetName()).isEqualTo(widgetName);
		// Page 部件的属性信息
		assertThat(actualRootWidget.getProperties()).hasSize(1);
		AttachedWidgetProperty actualRootWidgetProperty1 = actualRootWidget.getProperties().get(0);
		assertThat(actualRootWidgetProperty1.getId()).hasSize(32);
		assertThat(actualRootWidgetProperty1.getCode()).isEqualTo("0011");
		assertThat(actualRootWidgetProperty1.getValueType()).isEqualTo(WidgetPropertyValueType.STRING.getKey());
		assertThat(actualRootWidgetProperty1.getValue()).isEqualTo("default_value");
		assertThat(actualRootWidgetProperty1.getName()).isEqualTo("prop_name");
		
		// 校验 git 中的文件内容
		String mainPageJsonString = Files.readString(context.getGitRepositoryDirectory().resolve(projectKey).resolve(pageKey + ".page.web.json"));
		assertThat(JsonUtil.fromJsonObject(mainPageJsonString, PageModel.class)).usingRecursiveComparison().isEqualTo(actualPageModel);
	}
	
	@Test
	public void findChildrenNoData() {
		List<RepositoryResource> resources = repositoryResourceService.findChildren(null, 1);
		assertThat(resources).isEmpty();
	}
	
	@Test
	public void findChildrenAtRootHasTwoGroupsSuccess() {
		Integer repositoryId = 1;
		String owner = "jack";
		String repoName = "repo1";
		Repository repository = new Repository();
		repository.setCreateUserName(owner);
		repository.setName(repoName);
		repository.setId(repositoryId);
		
		RepositoryResource resource = new RepositoryResource();
		resource.setRepositoryId(repositoryId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(RepositoryResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		repositortResourceDao.save(resource);
		
		resource = new RepositoryResource();
		resource.setRepositoryId(2);
		resource.setKey("key2");
		resource.setName("name2");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(RepositoryResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		repositortResourceDao.save(resource);
		
		List<RepositoryResource> resources = repositoryResourceService.findChildren(repository, Constant.TREE_ROOT_ID);
		assertThat(resources).hasSize(1);
	}
	
	@Test
	public void findChildrenAtRootHasOneGroupSuccess() {
		Integer repositoryId = 1;
		String owner = "jack";
		String repoName = "repo1";
		Repository repository = new Repository();
		repository.setCreateUserName(owner);
		repository.setName(repoName);
		repository.setId(repositoryId);
		
		RepositoryResource resource = new RepositoryResource();
		resource.setRepositoryId(repositoryId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(RepositoryResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer savedResourceId = repositortResourceDao.save(resource).getId();
		
		resource = new RepositoryResource();
		resource.setRepositoryId(repositoryId);
		resource.setKey("key2");
		resource.setName("name2");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(RepositoryResourceType.GROUP);
		resource.setParentId(savedResourceId);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		repositortResourceDao.save(resource);
		
		List<RepositoryResource> resources = repositoryResourceService.findChildren(repository, Constant.TREE_ROOT_ID);
		assertThat(resources).hasSize(1);
	}
	
	@Test
	public void findChildrenNameIsNullThenSetNameWithKey() {
		Integer repositoryId = 1;
		String owner = "jack";
		String repoName = "repo1";
		
		Repository repository = new Repository();
		repository.setCreateUserName(owner);
		repository.setName(repoName);
		repository.setId(repositoryId);
		
		RepositoryResource resource = new RepositoryResource();
		resource.setRepositoryId(repositoryId);
		resource.setKey("key1");
		resource.setName(null);
		resource.setAppType(AppType.WEB);
		resource.setResourceType(RepositoryResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		repositortResourceDao.save(resource).getId();
		
		List<RepositoryResource> resources = repositoryResourceService.findChildren(repository, Constant.TREE_ROOT_ID);
		assertThat(resources).hasSize(1);
		assertThat(resources.get(0).getName()).isEqualTo("key1");
	}
	
	@Test
	public void findChildrenAtSubGroup() {
		Integer repositoryId = 1;
		String owner = "jack";
		String repoName = "repo1";
		
		Repository repository = new Repository();
		repository.setCreateUserName(owner);
		repository.setName(repoName);
		repository.setId(repositoryId);
		
		RepositoryResource resource = new RepositoryResource();
		resource.setRepositoryId(repositoryId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(RepositoryResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer savedResourceId = repositortResourceDao.save(resource).getId();
		
		resource = new RepositoryResource();
		resource.setRepositoryId(repositoryId);
		resource.setKey("key2");
		resource.setName("name2");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(RepositoryResourceType.GROUP);
		resource.setParentId(savedResourceId);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		repositortResourceDao.save(resource);
		
		List<RepositoryResource> resources = repositoryResourceService.findChildren(repository, savedResourceId);
		assertThat(resources).hasSize(1);
		assertThat(resources.get(0).getKey()).isEqualTo("key2");
	}
	
	@Test
	public void findParentPathAtRoot() {
		List<String> pathes = repositoryResourceService.findParentPathes(Constant.TREE_ROOT_ID);
		assertThat(pathes).isEmpty();
	}
	
	@Test
	public void findParentPathSuccess() {
		RepositoryResource resource = new RepositoryResource();
		resource.setRepositoryId(1);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(RepositoryResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer savedResourceId = repositortResourceDao.save(resource).getId();
		
		resource = new RepositoryResource();
		resource.setRepositoryId(1);
		resource.setKey("key2");
		resource.setName("name2");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(RepositoryResourceType.GROUP);
		resource.setParentId(savedResourceId);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		savedResourceId = repositortResourceDao.save(resource).getId();
		
		resource = new RepositoryResource();
		resource.setRepositoryId(1);
		resource.setKey("key3");
		resource.setName("name3");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(RepositoryResourceType.GROUP);
		resource.setParentId(savedResourceId);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		savedResourceId = repositortResourceDao.save(resource).getId();
		
		List<String> pathes = repositoryResourceService.findParentPathes(savedResourceId);
		assertThat(String.join("/", pathes)).isEqualTo("key1/key2/key3");
	}
	
	// 因为 getTitle 方法用到了 spring 的国际化帮助类，因为需要注入，所以将测试类放在 service 中
	@Test
	public void getTitleMain() {
		RepositoryResource resource = new RepositoryResource();
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setAppType(AppType.WEB);
		resource.setResourceType(RepositoryResourceType.PAGE);
		resource.setKey(RepositoryResource.MAIN_KEY);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("首页");
	}
	
	@Test
	public void getTitlePage() {
		RepositoryResource resource = new RepositoryResource();
		resource.setResourceType(RepositoryResourceType.PAGE);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("页面");
	}
	
	@Test
	public void getTitleGroup() {
		RepositoryResource resource = new RepositoryResource();
		resource.setResourceType(RepositoryResourceType.GROUP);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("分组");
	}
	
	@Test
	public void getTitleProject() {
		RepositoryResource resource = new RepositoryResource();
		resource.setResourceType(RepositoryResourceType.PROJECT);
		resource.setAppType(AppType.MINI_PROGRAM);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("小程序项目");
		
		resource.setAppType(AppType.WEB);
		assertThat(resource.getTitle()).isEqualTo("Web项目");
	}
	
	@Test
	public void getTitleTemplet() {
		RepositoryResource resource = new RepositoryResource();
		resource.setResourceType(RepositoryResourceType.PAGE_TEMPLET);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("模板");
	}
	
	@Test
	public void getTitleReadme() {
		RepositoryResource resource = new RepositoryResource();
		resource.setResourceType(RepositoryResourceType.FILE);
		resource.setKey(RepositoryResource.README_KEY);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("README");
	}
	
	@Test
	public void getTitleService() {
		RepositoryResource resource = new RepositoryResource();
		resource.setResourceType(RepositoryResourceType.SERVICE);
		resource.setMessageSource(messageSource);
		
		assertThat(resource.getTitle()).isEqualTo("服务");
	}

	@Test
	public void findByIdNoData() {
		Integer resourceId = 1;
		assertThat(repositoryResourceService.findById(resourceId)).isEmpty();
	}
	
	@Test
	public void findByIdSuccess() {
		RepositoryResource resource = new RepositoryResource();
		resource.setRepositoryId(1);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(RepositoryResourceType.GROUP);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer savedResourceId = repositortResourceDao.save(resource).getId();
		
		assertThat(repositoryResourceService.findById(savedResourceId)).isPresent();
	}
	
	@Test
	public void findByKeyAtRootNoData() {
		Integer repositoryId = 1;
		assertThat(repositoryResourceService.findByKey(
				repositoryId, 
				Constant.TREE_ROOT_ID, 
				RepositoryResourceType.PAGE, 
				AppType.WEB,
				"not-exist-key")).isEmpty();
	}
	
	@Test
	public void findByKeyAtRootSuccess() {
		Integer repositoryId = 1;
		
		RepositoryResource resource = new RepositoryResource();
		resource.setRepositoryId(repositoryId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(RepositoryResourceType.PAGE);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		repositortResourceDao.save(resource);
		
		assertThat(repositoryResourceService.findByKey(
				repositoryId, 
				Constant.TREE_ROOT_ID, 
				RepositoryResourceType.PAGE, 
				AppType.WEB,
				"key1")).isPresent();
	}
	
	@Test
	public void findByNameAtRootNoData() {
		Integer repositoryId = 1;
		assertThat(repositoryResourceService.findByName(
				repositoryId, 
				Constant.TREE_ROOT_ID, 
				RepositoryResourceType.PAGE, 
				AppType.WEB,
				"not-exist-name")).isEmpty();
	}
	
	@Test
	public void findByNameAtRootSuccess() {
		Integer repositoryId = 1;
		
		RepositoryResource resource = new RepositoryResource();
		resource.setRepositoryId(repositoryId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(RepositoryResourceType.PAGE);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		repositortResourceDao.save(resource);
		
		assertThat(repositoryResourceService.findByName(
				repositoryId, 
				Constant.TREE_ROOT_ID, 
				RepositoryResourceType.PAGE, 
				AppType.WEB,
				"name1")).isPresent();
	}

	@Test
	public void findAllPagesWebTypeButNoData() {
		Integer repositoryId = 1;
		assertThat(repositoryResourceService.findAllPages(repositoryId, AppType.WEB)).isEmpty();
	}
	
	@Test
	public void findAllPagesWebTypeSuccess() {
		Integer repositoryId = 1;
		
		RepositoryResource resource = new RepositoryResource();
		resource.setRepositoryId(repositoryId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.WEB);
		resource.setResourceType(RepositoryResourceType.PAGE);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		repositortResourceDao.save(resource);
		
		assertThat(repositoryResourceService.findAllPages(repositoryId, AppType.WEB)).hasSize(1);
	}
	
	@Test
	public void findParentGroupsByParentPathIsNotExist() {
		assertThat(repositoryResourceService.findParentGroupsByParentPath(null, null)).isEmpty();
		assertThat(repositoryResourceService.findParentGroupsByParentPath(null, "")).isEmpty();
	}
	
	@Test
	public void findParentGroupsByParentPathIsRootPath() {
		assertThat(repositoryResourceService.findParentGroupsByParentPath(1, null)).isEmpty();
		assertThat(repositoryResourceService.findParentGroupsByParentPath(1, "")).isEmpty();
	}
	
	@Test
	public void findParentGroupsByParentPathIsOneLevel() {
		Integer repositoryId = 1;
		
		RepositoryResource resource = new RepositoryResource();
		resource.setRepositoryId(repositoryId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.UNKNOWN);
		resource.setResourceType(RepositoryResourceType.PROJECT);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer resourceId = repositortResourceDao.save(resource).getId();
		
		List<RepositoryResource> groups = repositoryResourceService.findParentGroupsByParentPath(repositoryId, "key1");
		assertThat(groups).hasSize(1);
		assertThat(groups.get(0).getId()).isEqualTo(resourceId);
	}
	
	@Test
	public void findParentGroupsByParentPathIsTwoLevelSuccess() {
		Integer repositoryId = 1;
		
		RepositoryResource resource = new RepositoryResource();
		resource.setRepositoryId(repositoryId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.UNKNOWN);
		resource.setResourceType(RepositoryResourceType.PROJECT);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer resourceId = repositortResourceDao.save(resource).getId();
		
		resource = new RepositoryResource();
		resource.setRepositoryId(repositoryId);
		resource.setKey("key2");
		resource.setName("name2");
		resource.setAppType(AppType.UNKNOWN);
		resource.setResourceType(RepositoryResourceType.GROUP);
		resource.setParentId(resourceId);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		Integer resourceId2 = repositortResourceDao.save(resource).getId();
		
		List<RepositoryResource> groups = repositoryResourceService.findParentGroupsByParentPath(repositoryId, "key1/key2");
		assertThat(groups).hasSize(2);
		assertThat(groups.get(0).getId()).isEqualTo(resourceId);
		assertThat(groups.get(1).getId()).isEqualTo(resourceId2);
	}

	@Test
	public void findParentGroupsByParentPathIsTwoLevelNotExist() {
		Integer repositoryId = 1;
		
		RepositoryResource resource = new RepositoryResource();
		resource.setRepositoryId(repositoryId);
		resource.setKey("key1");
		resource.setName("name1");
		resource.setAppType(AppType.UNKNOWN);
		resource.setResourceType(RepositoryResourceType.PROJECT);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(1);
		resource.setCreateTime(LocalDateTime.now());
		repositortResourceDao.save(resource).getId();
		
		assertThat(repositoryResourceService.findParentGroupsByParentPath(repositoryId, "key1/key2")).isEmpty();
	}

	@Test
	public void findChangesSuccess(@TempDir Path rootFolder) throws IOException {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		
		Repository repository = new Repository();
		repository.setName("repoName1");
		repository.setIsPublic(true);
		repository.setDescription("description");
		repository.setLastActiveTime(LocalDateTime.now());
		repository.setCreateUserId(userId);
		repository.setCreateTime(LocalDateTime.now());
		repository.setCreateUserName("user_name");
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		
		Repository savedRepository = repositoryService.createRepository(userInfo, repository);
		
		RepositoryResource resource = new RepositoryResource();
		resource.setKey("page1");
		resource.setName("name1");
		resource.setResourceType(RepositoryResourceType.PAGE);
		resource.setAppType(AppType.WEB);
		resource.setCreateTime(LocalDateTime.now());
		resource.setCreateUserId(userId);
		resource.setRepositoryId(savedRepository.getId());
		resource.setParentId(Constant.TREE_ROOT_ID);
		repositoryResourceService.insert(savedRepository, resource);
		
		// 有一个未跟踪的文件。
		List<UncommittedFile> changes = repositoryResourceService.findChanges(savedRepository);
		
		assertThat(changes).hasSize(1);
		UncommittedFile file = changes.get(0);
		assertThat(file.getFullKeyPath()).isEqualTo("page1.page.web.json");
		assertThat(file.getGitStatus()).isEqualTo(GitFileStatus.UNTRACKED);
		assertThat(file.getIcon()).isEqualTo(AppType.WEB.getIcon());
		assertThat(file.getResourceName()).isEqualTo("name1");
		assertThat(file.getParentNamePath()).isBlank();
	}
	
	@Test
	public void findChangesNotContainGroup(@TempDir Path rootFolder) throws IOException {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		
		Repository repository = new Repository();
		repository.setName("repoName1");
		repository.setIsPublic(true);
		repository.setDescription("description");
		repository.setLastActiveTime(LocalDateTime.now());
		repository.setCreateUserId(userId);
		repository.setCreateTime(LocalDateTime.now());
		repository.setCreateUserName("user_name");
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		
		Repository savedRepository = repositoryService.createRepository(userInfo, repository);
		
		// 有一个未跟踪的文件夹。
		RepositoryResource resource = new RepositoryResource();
		resource.setKey("group1");
		resource.setName("name1");
		resource.setResourceType(RepositoryResourceType.GROUP);
		resource.setAppType(AppType.UNKNOWN);
		resource.setCreateTime(LocalDateTime.now());
		resource.setCreateUserId(userId);
		resource.setRepositoryId(savedRepository.getId());
		resource.setParentId(Constant.TREE_ROOT_ID);
		repositoryResourceService.insert(savedRepository, resource);
		
		List<UncommittedFile> changes = repositoryResourceService.findChanges(savedRepository);
		
		assertThat(changes).isEmpty();
	}

	@Test
	public void findChangesExistTwoLevelParentNamePathUseName(@TempDir Path rootFolder) throws IOException {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		
		Repository repository = new Repository();
		repository.setName("repoName1");
		repository.setIsPublic(true);
		repository.setDescription("description");
		repository.setLastActiveTime(LocalDateTime.now());
		repository.setCreateUserId(userId);
		repository.setCreateTime(LocalDateTime.now());
		repository.setCreateUserName("user_name");
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		
		Repository savedRepository = repositoryService.createRepository(userInfo, repository);
		
		// 有一个未跟踪的文件夹。
		RepositoryResource group1 = new RepositoryResource();
		group1.setKey("group1");
		group1.setResourceType(RepositoryResourceType.GROUP);
		group1.setAppType(AppType.UNKNOWN);
		group1.setCreateTime(LocalDateTime.now());
		group1.setCreateUserId(userId);
		group1.setRepositoryId(savedRepository.getId());
		group1.setParentId(Constant.TREE_ROOT_ID);
		Integer group1Id = repositoryResourceService.insert(savedRepository, group1).getId();
		
		RepositoryResource group11 = new RepositoryResource();
		group11.setKey("group11");
		group11.setResourceType(RepositoryResourceType.GROUP);
		group11.setAppType(AppType.UNKNOWN);
		group11.setCreateTime(LocalDateTime.now());
		group11.setCreateUserId(userId);
		group11.setRepositoryId(savedRepository.getId());
		group11.setParentId(group1Id);
		Integer group11Id = repositoryResourceService.insert(savedRepository, group11).getId();
				
		RepositoryResource page1 = new RepositoryResource();
		page1.setKey("page111");
		page1.setResourceType(RepositoryResourceType.PAGE);
		page1.setAppType(AppType.WEB);
		page1.setCreateTime(LocalDateTime.now());
		page1.setCreateUserId(userId);
		page1.setRepositoryId(savedRepository.getId());
		page1.setParentId(group11Id);
		repositoryResourceService.insert(savedRepository, page1);
		
		// 有一个未跟踪的文件。
		List<UncommittedFile> changes = repositoryResourceService.findChanges(savedRepository);
		
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
	public void stageChangesSuccess(@TempDir Path rootFolder) throws IOException {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		
		Repository repository = new Repository();
		repository.setName("repoName1");
		repository.setIsPublic(true);
		repository.setDescription("description");
		repository.setLastActiveTime(LocalDateTime.now());
		repository.setCreateUserId(userId);
		repository.setCreateTime(LocalDateTime.now());
		repository.setCreateUserName("user_name");
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		
		Repository savedRepository = repositoryService.createRepository(userInfo, repository);
		
		RepositoryResource resource = new RepositoryResource();
		resource.setKey("page1");
		resource.setName("name1");
		resource.setResourceType(RepositoryResourceType.PAGE);
		resource.setAppType(AppType.WEB);
		resource.setCreateTime(LocalDateTime.now());
		resource.setCreateUserId(userId);
		resource.setRepositoryId(savedRepository.getId());
		resource.setParentId(Constant.TREE_ROOT_ID);
		repositoryResourceService.insert(savedRepository, resource);
		
		repositoryResourceService.stageChanges(savedRepository, new String[] {"page1.page.web.json"});
		// 有一个已跟踪，但未提交的文件。
		List<UncommittedFile> changes = repositoryResourceService.findChanges(savedRepository);
		
		assertThat(changes).hasSize(1);
		
		UncommittedFile file = changes.get(0);
		assertThat(file.getFullKeyPath()).isEqualTo("page1.page.web.json");
		assertThat(file.getGitStatus()).isEqualTo(GitFileStatus.ADDED);
		assertThat(file.getIcon()).isEqualTo(AppType.WEB.getIcon());
		assertThat(file.getResourceName()).isEqualTo("name1");
		assertThat(file.getParentNamePath()).isBlank();
	}
	
	@Test
	public void unstageChangesSuccess(@TempDir Path rootFolder) throws IOException {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		
		Repository repository = new Repository();
		repository.setName("repoName1");
		repository.setIsPublic(true);
		repository.setDescription("description");
		repository.setLastActiveTime(LocalDateTime.now());
		repository.setCreateUserId(userId);
		repository.setCreateTime(LocalDateTime.now());
		repository.setCreateUserName("user_name");
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		
		Repository savedRepository = repositoryService.createRepository(userInfo, repository);
		
		RepositoryResource resource = new RepositoryResource();
		resource.setKey("page1");
		resource.setName("name1");
		resource.setResourceType(RepositoryResourceType.PAGE);
		resource.setAppType(AppType.WEB);
		resource.setCreateTime(LocalDateTime.now());
		resource.setCreateUserId(userId);
		resource.setRepositoryId(savedRepository.getId());
		resource.setParentId(Constant.TREE_ROOT_ID);
		repositoryResourceService.insert(savedRepository, resource);
		
		repositoryResourceService.stageChanges(savedRepository, new String[] {"page1.page.web.json"});
		repositoryResourceService.unstageChanges(savedRepository, new String[] {"page1.page.web.json"});
		// 有一个已跟踪，但未提交的文件。
		List<UncommittedFile> changes = repositoryResourceService.findChanges(savedRepository);
		
		assertThat(changes).hasSize(1);
		
		UncommittedFile file = changes.get(0);
		assertThat(file.getFullKeyPath()).isEqualTo("page1.page.web.json");
		assertThat(file.getGitStatus()).isEqualTo(GitFileStatus.UNTRACKED);
		assertThat(file.getIcon()).isEqualTo(AppType.WEB.getIcon());
		assertThat(file.getResourceName()).isEqualTo("name1");
		assertThat(file.getParentNamePath()).isBlank();
	}
	
	@Test
	public void commitNoStage(@TempDir Path rootFolder) throws IOException {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		
		Repository repository = new Repository();
		repository.setName("repoName1");
		repository.setIsPublic(true);
		repository.setDescription("description");
		repository.setLastActiveTime(LocalDateTime.now());
		repository.setCreateUserId(userId);
		repository.setCreateTime(LocalDateTime.now());
		repository.setCreateUserName("user_name");
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		
		Repository savedRepository = repositoryService.createRepository(userInfo, repository);
		
		List<UncommittedFile> changes = repositoryResourceService.findChanges(savedRepository);
		assertThat(changes).isEmpty();
		
		Assertions.assertThrows(GitEmptyCommitException.class, () -> repositoryResourceService.commit(userInfo, savedRepository, "commit page1"));
	}
	
	@Test
	public void commitSuccess(@TempDir Path rootFolder) throws IOException {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		
		Repository repository = new Repository();
		repository.setName("repoName1");
		repository.setIsPublic(true);
		repository.setDescription("description");
		repository.setLastActiveTime(LocalDateTime.now());
		repository.setCreateUserId(userId);
		repository.setCreateTime(LocalDateTime.now());
		repository.setCreateUserName("user_name");
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		
		Repository savedRepository = repositoryService.createRepository(userInfo, repository);
		
		RepositoryResource resource = new RepositoryResource();
		resource.setKey("page1");
		resource.setName("name1");
		resource.setResourceType(RepositoryResourceType.PAGE);
		resource.setAppType(AppType.WEB);
		resource.setCreateTime(LocalDateTime.now());
		resource.setCreateUserId(userId);
		resource.setRepositoryId(savedRepository.getId());
		resource.setParentId(Constant.TREE_ROOT_ID);
		repositoryResourceService.insert(savedRepository, resource);
		
		repositoryResourceService.stageChanges(savedRepository, new String[] {"page1.page.web.json"});
		String commitId = repositoryResourceService.commit(userInfo, savedRepository, "commit page1");
		// 有一个已跟踪，但未提交的文件。
		List<UncommittedFile> changes = repositoryResourceService.findChanges(savedRepository);
		assertThat(changes).hasSize(0);
		
		Optional<RepositoryCommit> commitOption = repositoryCommitDao.findByRepositoryIdAndBranchAndCommitId(savedRepository.getId(), Constants.MASTER, commitId);
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
	public void updatePageModelNewWidgetWithProperty() {
		Integer userId = 1;
		// 初始化数据
		// 1. 创建一个 API 仓库，类型为 Widget
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
		// 2. 创建一个 API 版本号
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setName("name");
		apiVersion.setVersion("master");
		apiVersion.setGitTagName("refs/heads/master");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		apiVersion.setLastPublishTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);
		// 3. 在对应的 API 版本下添加两个部件，分别为每一个部件设置一个属性
		// 3.1 部件1
		ApiWidget widget1 = new ApiWidget();
		widget1.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget1.setCode("0001");
		widget1.setName("Widget1");
		widget1.setLabel("Widget 1");
		widget1.setDescription("Description1");
		widget1.setCreateUserId(1);
		widget1.setCreateTime(LocalDateTime.now());
		ApiWidget savedWidget1 = apiComponentDao.save(widget1);
		// 3.1 属性1
		ApiWidgetProperty widgetProperty11 = new ApiWidgetProperty();
		widgetProperty11.setApiWidgetId(savedWidget1.getId());
		widgetProperty11.setApiRepoVersionId(savedApiRepoVersion.getId());
		widgetProperty11.setCode("0011");
		widgetProperty11.setDefaultValue("default_value_11");
		widgetProperty11.setDescription("description_11");
		widgetProperty11.setName("prop_name_11");
		widgetProperty11.setValueType(WidgetPropertyValueType.STRING.getKey());
		apiComponentAttrDao.save(widgetProperty11);
		// 3.2 部件2
		ApiWidget widget2 = new ApiWidget();
		widget2.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget2.setCode("0002");
		widget2.setName("Widget2");
		widget2.setDescription("Description2");
		widget2.setCreateUserId(1);
		widget2.setCreateTime(LocalDateTime.now());
		ApiWidget savedWidget2 = apiComponentDao.save(widget2);
		// 3.2  属性2
		ApiWidgetProperty widgetProperty21 = new ApiWidgetProperty();
		widgetProperty21.setApiWidgetId(savedWidget2.getId());
		widgetProperty21.setApiRepoVersionId(savedApiRepoVersion.getId());
		widgetProperty21.setCode("0021");
		widgetProperty21.setDefaultValue("default_value_21");
		widgetProperty21.setDescription("description_21");
		widgetProperty21.setName("prop_name_21");
		widgetProperty21.setLabel("prop_label_21");
		widgetProperty21.setValueType(WidgetPropertyValueType.STRING.getKey());
		apiComponentAttrDao.save(widgetProperty21);
		// 4. 创建一个 ide 组件库，实现上述的 API 仓库
		//    因为在查数据时，可直接获得 ide 组件仓库的版本信息
		//    然后根据版本信息，可直接获取 API 组件库的版本信息
		//    所以准备数据时，可跳过这一步。
		// 5. 为 ide 组件库创建一个版本号，实现上述的 API 版本
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1); // 组件仓库 id 为 1
		componentRepoVersion.setName("name");
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setAppType(AppType.WEB);
		componentRepoVersion.setBuild("build");
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersion.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 6. 创建一个项目
		//    为项目添加依赖时，直接使用项目标识即可
		//    所以准备数据时，可跳过这一步
		Integer repositoryId = 1;
		
		RepositoryResource project = new RepositoryResource();
		project.setRepositoryId(repositoryId);
		project.setParentId(Constant.TREE_ROOT_ID);
		project.setKey("project1");
		project.setAppType(AppType.WEB);
		project.setResourceType(RepositoryResourceType.PROJECT);
		project.setCreateTime(LocalDateTime.now());
		project.setCreateUserId(userId);
		project.setSeq(1);
		RepositoryResource savedProject = repositortResourceDao.save(project);
		
		// 7. 将创建的 ide 组件库的一个版本添加为项目依赖
		ProjectDependency dependency = new ProjectDependency();
		dependency.setRepositoryId(repositoryId);
		dependency.setProjectId(savedProject.getId());
		dependency.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependency.setCreateUserId(1);
		dependency.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(dependency);
		
		AttachedWidget attachedWidget1 = new AttachedWidget();
		attachedWidget1.setId("1");
		attachedWidget1.setParentId(Constant.TREE_ROOT_ID.toString());
		attachedWidget1.setApiRepoId(savedApiRepo.getId());
		attachedWidget1.setWidgetCode("0001");
		attachedWidget1.setWidgetName("Widget1"); // 只能取 widgetName 的值
		attachedWidget1.setWidgetId(savedWidget1.getId());
		AttachedWidgetProperty attachedWidgetProperty11 = new AttachedWidgetProperty();
		attachedWidgetProperty11.setId("11");
		attachedWidgetProperty11.setCode("0011");
		attachedWidgetProperty11.setName("prop_name_11"); // 注意，不会直接在页面模型中存储该值，只取 name 值，不取 label 值
		attachedWidgetProperty11.setValue("value11");
		attachedWidgetProperty11.setValueType(WidgetPropertyValueType.STRING.getKey());
		attachedWidget1.setProperties(Collections.singletonList(attachedWidgetProperty11));
		
		AttachedWidget attachedWidget2 = new AttachedWidget();
		attachedWidget2.setId("2"); // id 是在前台生成的
		attachedWidget2.setParentId("1"); // 父部件是 widget1
		attachedWidget2.setApiRepoId(savedApiRepo.getId());
		attachedWidget2.setWidgetCode("0002");
		attachedWidget2.setWidgetName("Widget2"); // 只能取 widgetName 的值
		attachedWidget2.setWidgetId(savedWidget2.getId());
		AttachedWidgetProperty attachedWidgetProperty21 = new AttachedWidgetProperty();
		attachedWidgetProperty21.setId("21"); // id 是在前台生成的
		attachedWidgetProperty21.setCode("0021");
		attachedWidgetProperty21.setName("prop_name_21"); // 注意，不会直接在页面模型中存储该值，只取 name 值，不取 label 值
		attachedWidgetProperty21.setValue("value21");
		attachedWidgetProperty21.setValueType(WidgetPropertyValueType.STRING.getKey());
		
		attachedWidget2.setProperties(Arrays.asList(attachedWidgetProperty21));
		
		// 新增页面模型
		PageModel model = new PageModel();
		
		Integer pageId = 1;
		model.setPageId(pageId);
		model.setWidgets(Arrays.asList(attachedWidget1, attachedWidget2));
		model.setData(Collections.emptyList());
		model.setFunctions(Collections.emptyList());
		
		RepositoryResource page = new RepositoryResource();
		page.setId(pageId);
		page.setParentId(savedProject.getId());
		
		repositoryResourceService.updatePageModel(null, null, model);
		
		PageModel savedModel = repositoryResourceService.getPageModel(page);
		
		assertThat(savedModel).usingRecursiveComparison().isEqualTo(model);
	}
	
	@Test
	public void updatePageModelNewWidgetWithEventNotSetValue() {
		// 初始化数据
		// 1. 创建一个 API 仓库，类型为 Widget
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
		// 2. 创建一个 API 版本号
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setName("name");
		apiVersion.setVersion("master");
		apiVersion.setGitTagName("refs/heads/master");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		apiVersion.setLastPublishTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);
		// 3. 在对应的 API 版本下添加一个部件，并为部件设置一个事件
		// 部件
		ApiWidget widget1 = new ApiWidget();
		widget1.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget1.setCode("0001");
		widget1.setName("Widget1");
		widget1.setLabel("Widget 1");
		widget1.setDescription("Description1");
		widget1.setCreateUserId(1);
		widget1.setCreateTime(LocalDateTime.now());
		ApiWidget savedWidget1 = apiComponentDao.save(widget1);
		// 事件
		ApiWidgetProperty widgetEvent1 = new ApiWidgetProperty();
		widgetEvent1.setApiWidgetId(savedWidget1.getId());
		widgetEvent1.setApiRepoVersionId(savedApiRepoVersion.getId());
		widgetEvent1.setCode("0002");
		widgetEvent1.setDescription("description_2");
		widgetEvent1.setName("prop_name_2");
		widgetEvent1.setLabel("prop_label_2");
		widgetEvent1.setValueType(WidgetPropertyValueType.FUNCTION.getKey());
		ApiWidgetProperty savedWidgetEvent1 = apiComponentAttrDao.save(widgetEvent1);
		// 3.2 事件的输入参数
		ApiWidgetEventArg arg = new ApiWidgetEventArg();
		arg.setApiWidgetPropertyId(savedWidgetEvent1.getId());
		arg.setApiRepoVersionId(savedApiRepoVersion.getId());
		arg.setCode("0003");
		arg.setName("arg1");
		arg.setValueType(WidgetPropertyValueType.STRING.getKey());
		arg.setSeq(1);
		apiComponentAttrFunArgDao.save(arg);
		
		// 4. 创建一个 ide 组件库，实现上述的 API 仓库
		//    因为在查数据时，可直接获得 ide 组件仓库的版本信息
		//    然后根据版本信息，可直接获取 API 组件库的版本信息
		//    所以准备数据时，可跳过这一步。
		// 5. 为 ide 组件库创建一个版本号，实现上述的 API 版本
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1); // 组件仓库 id 为 1
		componentRepoVersion.setName("name");
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setAppType(AppType.WEB);
		componentRepoVersion.setBuild("build");
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersion.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 6. 创建一个项目
		//    为项目添加依赖时，直接使用项目标识即可
		//    所以准备数据时，可跳过这一步
		Integer repositoryId = 1;
		Integer userId = 2;
		
		RepositoryResource project = new RepositoryResource();
		project.setRepositoryId(repositoryId);
		project.setParentId(Constant.TREE_ROOT_ID);
		project.setKey("project1");
		project.setAppType(AppType.WEB);
		project.setResourceType(RepositoryResourceType.PROJECT);
		project.setCreateTime(LocalDateTime.now());
		project.setCreateUserId(userId);
		project.setSeq(1);
		RepositoryResource savedProject = repositortResourceDao.save(project);
		
		// 7. 将创建的 ide 组件库的一个版本添加为项目依赖
		ProjectDependency dependence = new ProjectDependency();
		dependence.setRepositoryId(repositoryId);
		dependence.setProjectId(savedProject.getId());
		dependence.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependence.setCreateUserId(1);
		dependence.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(dependence);
		
		// 新增页面模型
		PageModel model = new PageModel();
		
		Integer pageId = 3;
		model.setPageId(pageId);
		
		AttachedWidget attachedWidget1 = new AttachedWidget();
		attachedWidget1.setId("1");
		attachedWidget1.setParentId(Constant.TREE_ROOT_ID.toString());
		attachedWidget1.setApiRepoId(savedApiRepo.getId());
		attachedWidget1.setWidgetCode("0001");
		attachedWidget1.setWidgetName("Widget1"); // 只能取 widgetName 的值
		attachedWidget1.setWidgetId(savedWidget1.getId());
		
		AttachedWidgetProperty attachedWidgetProperty22 = new AttachedWidgetProperty();
		// id 是在前台生成的，如果前台没有生成，则后台生成
		// 因为没有设置 value 的值，所以此 id 是后台生成的 uuid
		attachedWidgetProperty22.setCode("0002");
		attachedWidgetProperty22.setName("prop_name_2"); 
		attachedWidgetProperty22.setValueType(WidgetPropertyValueType.FUNCTION.getKey());
		// 此处没有设置属性值
		
		EventArgument arg1 = new EventArgument();
		arg1.setCode("0003");
		arg1.setName("arg1");
		arg1.setValueType(WidgetPropertyValueType.STRING.getKey());
		attachedWidgetProperty22.setEventArgs(Collections.singletonList(arg1));
		
		attachedWidget1.setProperties(Collections.singletonList(attachedWidgetProperty22));
		
		model.setWidgets(Collections.singletonList(attachedWidget1));
		model.setData(Collections.emptyList());
		model.setFunctions(Collections.emptyList());

		repositoryResourceService.updatePageModel(null, null, model);
		
		RepositoryResource page = new RepositoryResource();
		page.setId(pageId);
		page.setParentId(savedProject.getId());
		PageModel savedModel = repositoryResourceService.getPageModel(page);
		
		// 因为此时的 widgets.properties.id 是在后台生成的 uuid，无法断言，所以忽略掉
		assertThat(savedModel).usingRecursiveComparison().ignoringFields("widgets.properties.id").isEqualTo(model);
		assertThat(savedModel.getWidgets().get(0).getProperties().get(0).getId()).matches((value) -> value.length() == 32);
	}

	@Test
	public void updatePageModelNewWidgetWithEventSetValue() {
		// 初始化数据
		// 1. 创建一个 API 仓库，类型为 Widget
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
		// 2. 创建一个 API 版本号
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setName("name");
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		apiVersion.setLastPublishTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);
		// 3. 在对应的 API 版本下添加一个部件，并为部件设置一个事件
		// 部件
		ApiWidget widget1 = new ApiWidget();
		widget1.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget1.setCode("0001");
		widget1.setName("Widget1");
		widget1.setLabel("Widget 1");
		widget1.setDescription("Description1");
		widget1.setCreateUserId(1);
		widget1.setCreateTime(LocalDateTime.now());
		ApiWidget savedWidget1 = apiComponentDao.save(widget1);
		// 事件
		ApiWidgetProperty widgetEvent1 = new ApiWidgetProperty();
		widgetEvent1.setApiWidgetId(savedWidget1.getId());
		widgetEvent1.setApiRepoVersionId(savedApiRepoVersion.getId());
		widgetEvent1.setCode("0002");
		widgetEvent1.setDescription("description_2");
		widgetEvent1.setName("onValue");
		widgetEvent1.setLabel("prop_label_2");
		widgetEvent1.setValueType(WidgetPropertyValueType.FUNCTION.getKey());
		ApiWidgetProperty savedWidgetEvent1 = apiComponentAttrDao.save(widgetEvent1);
		// 3.2 事件的输入参数
		ApiWidgetEventArg arg = new ApiWidgetEventArg();
		arg.setApiWidgetPropertyId(savedWidgetEvent1.getId());
		arg.setApiRepoVersionId(savedApiRepoVersion.getId());
		arg.setCode("0003");
		arg.setName("value");
		arg.setValueType(WidgetPropertyValueType.STRING.getKey());
		arg.setSeq(1);
		apiComponentAttrFunArgDao.save(arg);
		
		// 4. 创建一个 ide 组件库，实现上述的 API 仓库
		//    因为在查数据时，可直接获得 ide 组件仓库的版本信息
		//    然后根据版本信息，可直接获取 API 组件库的版本信息
		//    所以准备数据时，可跳过这一步。
		// 5. 为 ide 组件库创建一个版本号，实现上述的 API 版本
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1); // 组件仓库 id 为 1
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setName("name");
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setAppType(AppType.WEB);
		componentRepoVersion.setBuild("build");
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersion.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 6. 创建一个项目
		//    为项目添加依赖时，直接使用项目标识即可
		//    所以准备数据时，可跳过这一步
		Integer repositoryId = 1;
		Integer userId = 2;
		
		RepositoryResource project = new RepositoryResource();
		project.setRepositoryId(repositoryId);
		project.setParentId(Constant.TREE_ROOT_ID);
		project.setKey("project1");
		project.setAppType(AppType.WEB);
		project.setResourceType(RepositoryResourceType.PROJECT);
		project.setCreateTime(LocalDateTime.now());
		project.setCreateUserId(userId);
		project.setSeq(1);
		RepositoryResource savedProject = repositortResourceDao.save(project);
		
		// 7. 将创建的 ide 组件库的一个版本添加为项目依赖
		ProjectDependency dependency = new ProjectDependency();
		dependency.setRepositoryId(repositoryId);
		dependency.setProjectId(savedProject.getId());
		dependency.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependency.setCreateUserId(1);
		dependency.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(dependency);
		
		// 新增页面模型
		PageModel model = new PageModel();
		
		Integer pageId = 1;
		model.setPageId(pageId);
		
		AttachedWidget attachedWidget1 = new AttachedWidget();
		attachedWidget1.setId("1");
		attachedWidget1.setParentId(Constant.TREE_ROOT_ID.toString());
		attachedWidget1.setApiRepoId(savedApiRepo.getId());
		attachedWidget1.setWidgetCode("0001");
		attachedWidget1.setWidgetName("Widget1"); // 只能取 widgetName 的值
		attachedWidget1.setWidgetId(savedWidget1.getId());
		
		AttachedWidgetProperty attachedWidgetProperty22 = new AttachedWidgetProperty();
		// id 是在前台生成的，如果前台没有生成，则后台生成
		// 因为设置了 value 的值，所以此 id 是前台设置的值
		attachedWidgetProperty22.setId("eventId");
		attachedWidgetProperty22.setCode("0002");
		attachedWidgetProperty22.setName("onValue"); 
		attachedWidgetProperty22.setValueType(WidgetPropertyValueType.FUNCTION.getKey());
		// 此处设置属性值，即为事件绑定了一个 id 为 `a_function_id` 的事件处理函数
		String handlerId = "a_function_id";
		attachedWidgetProperty22.setValue(handlerId);
		
		EventArgument arg1 = new EventArgument();
		arg1.setCode("0003");
		arg1.setName("value");
		arg1.setValueType(WidgetPropertyValueType.STRING.getKey());
		attachedWidgetProperty22.setEventArgs(Collections.singletonList(arg1));
		
		attachedWidget1.setProperties(Collections.singletonList(attachedWidgetProperty22));
		
		model.setWidgets(Collections.singletonList(attachedWidget1));
		model.setData(Collections.emptyList());
		
		// 为事件绑定一个事件处理函数后，就需要定义一个事件处理函数
		PageEventHandler func = new PageEventHandler();
		func.setId(handlerId);
		
		// 一个函数定义节点
		VisualNode node = new VisualNode();
		node.setId("1");
		node.setLeft(20);
		node.setTop(20);
		node.setCaption("事件处理函数");
		node.setText("onValue");
		node.setLayout(NodeLayout.FLOW_CONTROL.getKey());
		node.setCategory(NodeCategory.FUNCTION.getKey());
		
		// 节点中包含一个 output sequence port 和 一个 output data port
		OutputSequencePort outputSequencePort = new OutputSequencePort();
		outputSequencePort.setId("osp1");
		node.setOutputSequencePorts(Collections.singletonList(outputSequencePort));
		
		DataPort outputDataPort = new DataPort();
		outputDataPort.setId("odp1");
		outputDataPort.setName("value");
		outputDataPort.setType(WidgetPropertyValueType.STRING.getKey());
		node.setOutputDataPorts(Collections.singletonList(outputDataPort));
		
		func.setNodes(Collections.singletonList(node));
		
		// 需要定义一个节点和一个 port（因为事件包含一个输入参数）
		model.setFunctions(Collections.singletonList(func));

		repositoryResourceService.updatePageModel(null, null, model);
		
		RepositoryResource page = new RepositoryResource();
		page.setId(pageId);
		page.setParentId(savedProject.getId());
		PageModel savedModel = repositoryResourceService.getPageModel(page);
		
		assertThat(savedModel).usingRecursiveComparison().isEqualTo(model);
	}
	
	// 没有测试连接线，而是为 set variable 设置一个默认值。
	@Test
	public void updatePageModelNewWidgetWithEventHasOneSetVariableNode() {
		// 初始化数据
		// 1. 创建一个 API 仓库，类型为 Widget
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
		// 2. 创建一个 API 版本号
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setName("name");
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		apiVersion.setLastPublishTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);
		// 3. 在对应的 API 版本下添加一个部件，并为部件设置一个事件
		// 部件
		ApiWidget widget1 = new ApiWidget();
		widget1.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget1.setCode("0001");
		widget1.setName("Widget1");
		widget1.setLabel("Widget 1");
		widget1.setDescription("Description1");
		widget1.setCreateUserId(1);
		widget1.setCreateTime(LocalDateTime.now());
		ApiWidget savedWidget1 = apiComponentDao.save(widget1);
		// 事件
		ApiWidgetProperty widgetEvent1 = new ApiWidgetProperty();
		widgetEvent1.setApiWidgetId(savedWidget1.getId());
		widgetEvent1.setApiRepoVersionId(savedApiRepoVersion.getId());
		widgetEvent1.setCode("0002");
		widgetEvent1.setDescription("description_2");
		widgetEvent1.setName("onValue");
		widgetEvent1.setLabel("prop_label_2");
		widgetEvent1.setValueType(WidgetPropertyValueType.FUNCTION.getKey());
		ApiWidgetProperty savedWidgetEvent1 = apiComponentAttrDao.save(widgetEvent1);
		// 3.2 事件的输入参数
		ApiWidgetEventArg arg = new ApiWidgetEventArg();
		arg.setApiWidgetPropertyId(savedWidgetEvent1.getId());
		arg.setApiRepoVersionId(savedApiRepoVersion.getId());
		arg.setCode("0003");
		arg.setName("value");
		arg.setValueType(WidgetPropertyValueType.STRING.getKey());
		arg.setSeq(1);
		apiComponentAttrFunArgDao.save(arg);
		
		// 4. 创建一个 ide 组件库，实现上述的 API 仓库
		//    因为在查数据时，可直接获得 ide 组件仓库的版本信息
		//    然后根据版本信息，可直接获取 API 组件库的版本信息
		//    所以准备数据时，可跳过这一步。
		// 5. 为 ide 组件库创建一个版本号，实现上述的 API 版本
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1); // 组件仓库 id 为 1
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setName("name");
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setAppType(AppType.WEB);
		componentRepoVersion.setBuild("dojo");
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersion.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 6. 创建一个项目
		//    为项目添加依赖时，直接使用项目标识即可
		//    所以准备数据时，可跳过这一步
		Integer repositoryId = 1;
		Integer userId = 2;
		
		RepositoryResource project = new RepositoryResource();
		project.setRepositoryId(repositoryId);
		project.setParentId(Constant.TREE_ROOT_ID);
		project.setKey("project1");
		project.setAppType(AppType.WEB);
		project.setResourceType(RepositoryResourceType.PROJECT);
		project.setCreateTime(LocalDateTime.now());
		project.setCreateUserId(userId);
		project.setSeq(1);
		RepositoryResource savedProject = repositortResourceDao.save(project);
		
		// 7. 将创建的 ide 组件库的一个版本添加为项目依赖
		ProjectDependency dependence = new ProjectDependency();
		dependence.setRepositoryId(repositoryId);
		dependence.setProjectId(savedProject.getId());
		dependence.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependence.setCreateUserId(1);
		dependence.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(dependence);
		
		// 新增页面模型
		PageModel model = new PageModel();
		
		Integer pageId = 1;
		model.setPageId(pageId);
		
		AttachedWidget attachedWidget1 = new AttachedWidget();
		attachedWidget1.setId("1");
		attachedWidget1.setParentId(Constant.TREE_ROOT_ID.toString());
		attachedWidget1.setApiRepoId(savedApiRepo.getId());
		attachedWidget1.setWidgetCode("0001");
		attachedWidget1.setWidgetName("Widget1"); // 只能取 widgetName 的值
		attachedWidget1.setWidgetId(savedWidget1.getId());
		
		AttachedWidgetProperty attachedWidgetProperty22 = new AttachedWidgetProperty();
		// id 是在前台生成的，如果前台没有生成，则后台生成
		// 因为设置了 value 的值，所以此 id 是前台设置的值
		attachedWidgetProperty22.setId("eventId");
		attachedWidgetProperty22.setCode("0002");
		attachedWidgetProperty22.setName("onValue"); 
		attachedWidgetProperty22.setValueType(WidgetPropertyValueType.FUNCTION.getKey());
		// 此处设置属性值，即为事件绑定了一个 id 为 `a_function_id` 的事件处理函数
		String handlerId = "a_function_id";
		attachedWidgetProperty22.setValue(handlerId);
		
		EventArgument arg1 = new EventArgument();
		arg1.setCode("0003");
		arg1.setName("value");
		arg1.setValueType(WidgetPropertyValueType.STRING.getKey());
		attachedWidgetProperty22.setEventArgs(Collections.singletonList(arg1));
		
		attachedWidget1.setProperties(Collections.singletonList(attachedWidgetProperty22));
		
		model.setWidgets(Collections.singletonList(attachedWidget1));
		
		// 定义一个页面数据，并在事件处理函数中通过 set 方法为此变量赋值。
		PageDataItem rootDataItem = new PageDataItem();
		rootDataItem.setId("dataId1");
		rootDataItem.setParentId(Constant.TREE_ROOT_ID.toString());
		rootDataItem.setName("root");
		rootDataItem.setPageId(pageId);
		rootDataItem.setSeq(1);
		rootDataItem.setType("Object");
		
		PageDataItem dataItem1 = new PageDataItem();
		dataItem1.setId("dataId2");
		dataItem1.setParentId("dataId1");
		dataItem1.setName("foo");
		dataItem1.setPageId(pageId);
		dataItem1.setSeq(2); // 页面级排序
		dataItem1.setValue("bar"); // 默认值为 bar
		dataItem1.setType("String");
		
		model.setData(Arrays.asList(rootDataItem, dataItem1));
		
		// 为事件绑定一个事件处理函数后，就需要定义一个事件处理函数
		PageEventHandler func = new PageEventHandler();
		func.setId(handlerId);
		
		// 一个函数定义节点
		VisualNode node1 = new VisualNode();
		node1.setId("1");
		node1.setLeft(20);
		node1.setTop(20);
		node1.setCaption("事件处理函数");
		node1.setText("onValue");
		node1.setLayout(NodeLayout.FLOW_CONTROL.getKey());
		node1.setCategory(NodeCategory.FUNCTION.getKey());
		
		// 函数定义节点
		// 包含一个 output sequence port 和 一个 output data port
		OutputSequencePort outputSequencePort = new OutputSequencePort();
		outputSequencePort.setId("osp1");
		node1.setOutputSequencePorts(Collections.singletonList(outputSequencePort));
		
		DataPort outputDataPort = new DataPort();
		outputDataPort.setId("odp1");
		outputDataPort.setName("value");
		outputDataPort.setType(WidgetPropertyValueType.STRING.getKey());
		node1.setOutputDataPorts(Collections.singletonList(outputDataPort));
		
		// set variable node
		// 包含一个 input sequence port、一个 output sequence port 和 一个 input data port
		VisualNode node2 = new VisualNode();
		node2.setId("2");
		node2.setLeft(40);
		node2.setTop(40);
		node2.setCaption("Set foo");
		node2.setLayout(NodeLayout.DATA.getKey());
		node2.setCategory(NodeCategory.VARIABLE_SET.getKey());
		node2.setDataItemId(dataItem1.getId());
		
		InputSequencePort isp2 = new InputSequencePort();
		isp2.setId("isp2");
		node2.setInputSequencePort(isp2);
		
		OutputSequencePort osp2 = new OutputSequencePort();
		osp2.setId("osp2");
		node2.addOutputSequencePort(osp2);
		
		InputDataPort inputDataPort = new InputDataPort();
		inputDataPort.setId("idp1");
		inputDataPort.setName("set");
		inputDataPort.setType("String");
		inputDataPort.setValue("bar1"); // 注意，与 pageDataItem 中的 bar 不是一回事
		
		node2.addInputDataPort(inputDataPort);
		
		func.setNodes(Arrays.asList(node1, node2));
		
		// 需要定义一个节点和一个 port（因为事件包含一个输入参数）
		model.setFunctions(Collections.singletonList(func));

		repositoryResourceService.updatePageModel(null, null, model);
		
		RepositoryResource page = new RepositoryResource();
		page.setId(pageId);
		page.setParentId(savedProject.getId());
		PageModel savedModel = repositoryResourceService.getPageModel(page);
		
		assertThat(savedModel).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(model);
	}
	
	@Test
	public void updatePageModelNewWidgetWithEventHasOneGetVariableNode() {
		// 初始化数据
		// 1. 创建一个 API 仓库，类型为 Widget
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
		// 2. 创建一个 API 版本号
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setName("name");
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		apiVersion.setLastPublishTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);
		// 3. 在对应的 API 版本下添加一个部件，并为部件设置一个事件
		// 部件
		ApiWidget widget1 = new ApiWidget();
		widget1.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget1.setCode("0001");
		widget1.setName("Widget1");
		widget1.setLabel("Widget 1");
		widget1.setDescription("Description1");
		widget1.setCreateUserId(1);
		widget1.setCreateTime(LocalDateTime.now());
		ApiWidget savedWidget1 = apiComponentDao.save(widget1);
		// 事件
		ApiWidgetProperty widgetEvent1 = new ApiWidgetProperty();
		widgetEvent1.setApiWidgetId(savedWidget1.getId());
		widgetEvent1.setApiRepoVersionId(savedApiRepoVersion.getId());
		widgetEvent1.setCode("0002");
		widgetEvent1.setDescription("description_2");
		widgetEvent1.setName("onValue");
		widgetEvent1.setLabel("prop_label_2");
		widgetEvent1.setValueType(WidgetPropertyValueType.FUNCTION.getKey());
		ApiWidgetProperty savedWidgetEvent1 = apiComponentAttrDao.save(widgetEvent1);
		// 3.2 事件的输入参数
		ApiWidgetEventArg arg = new ApiWidgetEventArg();
		arg.setApiRepoVersionId(savedApiRepoVersion.getId());
		arg.setApiWidgetPropertyId(savedWidgetEvent1.getId());
		arg.setCode("0003");
		arg.setName("value");
		arg.setValueType(WidgetPropertyValueType.STRING.getKey());
		arg.setSeq(1);
		apiComponentAttrFunArgDao.save(arg);
		
		// 4. 创建一个 ide 组件库，实现上述的 API 仓库
		//    因为在查数据时，可直接获得 ide 组件仓库的版本信息
		//    然后根据版本信息，可直接获取 API 组件库的版本信息
		//    所以准备数据时，可跳过这一步。
		// 5. 为 ide 组件库创建一个版本号，实现上述的 API 版本
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1); // 组件仓库 id 为 1
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setName("name");
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setAppType(AppType.WEB);
		componentRepoVersion.setBuild("dojo");
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersion.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 6. 创建一个项目
		//    为项目添加依赖时，直接使用项目标识即可
		//    所以准备数据时，可跳过这一步
		Integer repositoryId = 1;
		Integer userId = 2;
		
		RepositoryResource project = new RepositoryResource();
		project.setRepositoryId(repositoryId);
		project.setParentId(Constant.TREE_ROOT_ID);
		project.setKey("project1");
		project.setAppType(AppType.WEB);
		project.setResourceType(RepositoryResourceType.PROJECT);
		project.setCreateTime(LocalDateTime.now());
		project.setCreateUserId(userId);
		project.setSeq(1);
		RepositoryResource savedProject = repositortResourceDao.save(project);
		
		// 7. 将创建的 ide 组件库的一个版本添加为项目依赖
		ProjectDependency dependency = new ProjectDependency();
		dependency.setRepositoryId(repositoryId);
		dependency.setProjectId(savedProject.getId());
		dependency.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependency.setCreateUserId(1);
		dependency.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(dependency);
		
		// 新增页面模型
		PageModel model = new PageModel();
		
		Integer pageId = 1;
		model.setPageId(pageId);
		
		AttachedWidget attachedWidget1 = new AttachedWidget();
		attachedWidget1.setId("1");
		attachedWidget1.setParentId(Constant.TREE_ROOT_ID.toString());
		attachedWidget1.setApiRepoId(savedApiRepo.getId());
		attachedWidget1.setWidgetCode("0001");
		attachedWidget1.setWidgetName("Widget1"); // 只能取 widgetName 的值
		attachedWidget1.setWidgetId(savedWidget1.getId());
		
		AttachedWidgetProperty attachedWidgetProperty22 = new AttachedWidgetProperty();
		// id 是在前台生成的，如果前台没有生成，则后台生成
		// 因为设置了 value 的值，所以此 id 是前台设置的值
		attachedWidgetProperty22.setId("eventId");
		attachedWidgetProperty22.setCode("0002");
		attachedWidgetProperty22.setName("onValue"); 
		attachedWidgetProperty22.setValueType(WidgetPropertyValueType.FUNCTION.getKey());
		// 此处设置属性值，即为事件绑定了一个 id 为 `a_function_id` 的事件处理函数
		String handlerId = "a_function_id";
		attachedWidgetProperty22.setValue(handlerId);
		
		EventArgument arg1 = new EventArgument();
		arg1.setCode("0003");
		arg1.setName("value");
		arg1.setValueType(WidgetPropertyValueType.STRING.getKey());
		attachedWidgetProperty22.setEventArgs(Collections.singletonList(arg1));
		
		attachedWidget1.setProperties(Collections.singletonList(attachedWidgetProperty22));
		
		model.setWidgets(Collections.singletonList(attachedWidget1));
		
		// 定义一个页面数据，并在事件处理函数中通过 set 方法为此变量赋值。
		PageDataItem rootDataItem = new PageDataItem();
		rootDataItem.setId("dataId1");
		rootDataItem.setParentId(Constant.TREE_ROOT_ID.toString());
		rootDataItem.setName("root");
		rootDataItem.setPageId(pageId);
		rootDataItem.setSeq(1);
		rootDataItem.setType("Object");
		
		PageDataItem dataItem1 = new PageDataItem();
		dataItem1.setId("dataId2");
		dataItem1.setParentId("dataId1");
		dataItem1.setName("foo");
		dataItem1.setPageId(pageId);
		dataItem1.setSeq(2); // 页面级排序
		dataItem1.setValue("bar"); // 默认值为 bar
		dataItem1.setType("String");
		
		model.setData(Arrays.asList(rootDataItem, dataItem1));
		
		// 为事件绑定一个事件处理函数后，就需要定义一个事件处理函数
		PageEventHandler func = new PageEventHandler();
		func.setId(handlerId);
		
		// 一个函数定义节点
		VisualNode node1 = new VisualNode();
		node1.setId("1");
		node1.setLeft(20);
		node1.setTop(20);
		node1.setCaption("事件处理函数");
		node1.setText("onValue");
		node1.setLayout(NodeLayout.FLOW_CONTROL.getKey());
		node1.setCategory(NodeCategory.FUNCTION.getKey());
		
		// 函数定义节点中
		// 包含一个 output sequence port 和一个 output data port
		OutputSequencePort outputSequencePort = new OutputSequencePort();
		outputSequencePort.setId("osp1");
		node1.setOutputSequencePorts(Collections.singletonList(outputSequencePort));
		
		DataPort outputDataPort = new DataPort();
		outputDataPort.setId("odp1");
		outputDataPort.setName("value");
		outputDataPort.setType(WidgetPropertyValueType.STRING.getKey());
		node1.setOutputDataPorts(Collections.singletonList(outputDataPort));
		
		// get variable node
		// 不包含 sequence port，只包含一个 output data port
		VisualNode node2 = new VisualNode();
		node2.setId("2");
		node2.setLeft(40);
		node2.setTop(40);
		node2.setCaption("Get foo");
		node2.setLayout(NodeLayout.DATA.getKey());
		node2.setCategory(NodeCategory.VARIABLE_GET.getKey());
		node2.setDataItemId(dataItem1.getId());
		
		DataPort odp2 = new DataPort();
		odp2.setId("odp2");
		odp2.setName("value");
		odp2.setType("String");
		
		node2.addOutputDataPort(odp2);
		
		func.setNodes(Arrays.asList(node1, node2));
		
		// 需要定义一个节点和一个 port（因为事件包含一个输入参数）
		model.setFunctions(Collections.singletonList(func));

		repositoryResourceService.updatePageModel(null, null, model);
		
		RepositoryResource page = new RepositoryResource();
		page.setId(pageId);
		page.setParentId(savedProject.getId());
		PageModel savedModel = repositoryResourceService.getPageModel(page);
		
		assertThat(savedModel).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(model);
	}
	
	@Test
	public void updatePageModeelNewWidgetWithEventConnectFunctionDeclareNodeAndSetVariableNode() {
		// 初始化数据
		// 1. 创建一个 API 仓库，类型为 Widget
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
		// 2. 创建一个 API 版本号
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setName("name");
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		apiVersion.setLastPublishTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);
		// 3. 在对应的 API 版本下添加一个部件，并为部件设置一个事件
		// 部件
		ApiWidget widget1 = new ApiWidget();
		widget1.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget1.setCode("0001");
		widget1.setName("Widget1");
		widget1.setLabel("Widget 1");
		widget1.setDescription("Description1");
		widget1.setCreateUserId(1);
		widget1.setCreateTime(LocalDateTime.now());
		ApiWidget savedWidget1 = apiComponentDao.save(widget1);
		// 事件
		ApiWidgetProperty widgetEvent1 = new ApiWidgetProperty();
		widgetEvent1.setApiWidgetId(savedWidget1.getId());
		widgetEvent1.setApiRepoVersionId(savedApiRepoVersion.getId());
		widgetEvent1.setCode("0002");
		widgetEvent1.setDescription("description_2");
		widgetEvent1.setName("onValue");
		widgetEvent1.setLabel("prop_label_2");
		widgetEvent1.setValueType(WidgetPropertyValueType.FUNCTION.getKey());
		ApiWidgetProperty savedWidgetEvent1 = apiComponentAttrDao.save(widgetEvent1);
		// 3.2 事件的输入参数
		ApiWidgetEventArg arg = new ApiWidgetEventArg();
		arg.setApiWidgetPropertyId(savedWidgetEvent1.getId());
		arg.setApiRepoVersionId(savedApiRepoVersion.getId());
		arg.setCode("0003");
		arg.setName("value");
		arg.setValueType(WidgetPropertyValueType.STRING.getKey());
		arg.setSeq(1);
		apiComponentAttrFunArgDao.save(arg);
		
		// 4. 创建一个 ide 组件库，实现上述的 API 仓库
		//    因为在查数据时，可直接获得 ide 组件仓库的版本信息
		//    然后根据版本信息，可直接获取 API 组件库的版本信息
		//    所以准备数据时，可跳过这一步。
		// 5. 为 ide 组件库创建一个版本号，实现上述的 API 版本
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1); // 组件仓库 id 为 1
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setName("name");
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setAppType(AppType.WEB);
		componentRepoVersion.setBuild("build");
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersion.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 6. 创建一个项目
		//    为项目添加依赖时，直接使用项目标识即可
		//    所以准备数据时，可跳过这一步
		Integer repositoryId = 1;
		Integer userId = 2;
		
		RepositoryResource project = new RepositoryResource();
		project.setRepositoryId(repositoryId);
		project.setParentId(Constant.TREE_ROOT_ID);
		project.setKey("project1");
		project.setAppType(AppType.WEB);
		project.setResourceType(RepositoryResourceType.PROJECT);
		project.setCreateTime(LocalDateTime.now());
		project.setCreateUserId(userId);
		project.setSeq(1);
		RepositoryResource savedProject = repositortResourceDao.save(project);
		
		// 7. 将创建的 ide 组件库的一个版本添加为项目依赖
		ProjectDependency dependency = new ProjectDependency();
		dependency.setRepositoryId(repositoryId);
		dependency.setProjectId(savedProject.getId());
		dependency.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependency.setCreateUserId(1);
		dependency.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(dependency);
		
		// 新增页面模型
		PageModel model = new PageModel();
		
		Integer pageId = 1;
		model.setPageId(pageId);
		
		AttachedWidget attachedWidget1 = new AttachedWidget();
		attachedWidget1.setId("1");
		attachedWidget1.setParentId(Constant.TREE_ROOT_ID.toString());
		attachedWidget1.setApiRepoId(savedApiRepo.getId());
		attachedWidget1.setWidgetCode("0001");
		attachedWidget1.setWidgetName("Widget1"); // 只能取 widgetName 的值
		attachedWidget1.setWidgetId(savedWidget1.getId());
		
		AttachedWidgetProperty attachedWidgetProperty22 = new AttachedWidgetProperty();
		// id 是在前台生成的，如果前台没有生成，则后台生成
		// 因为设置了 value 的值，所以此 id 是前台设置的值
		attachedWidgetProperty22.setId("eventId");
		attachedWidgetProperty22.setCode("0002");
		attachedWidgetProperty22.setName("onValue"); 
		attachedWidgetProperty22.setValueType(WidgetPropertyValueType.FUNCTION.getKey());
		// 此处设置属性值，即为事件绑定了一个 id 为 `a_function_id` 的事件处理函数
		String handlerId = "a_function_id";
		attachedWidgetProperty22.setValue(handlerId);
		
		EventArgument arg1 = new EventArgument();
		arg1.setCode("0003");
		arg1.setName("value");
		arg1.setValueType(WidgetPropertyValueType.STRING.getKey());
		attachedWidgetProperty22.setEventArgs(Collections.singletonList(arg1));
		
		attachedWidget1.setProperties(Collections.singletonList(attachedWidgetProperty22));
		
		model.setWidgets(Collections.singletonList(attachedWidget1));
		
		// 定义一个页面数据，并在事件处理函数中通过 set 方法为此变量赋值。
		PageDataItem rootDataItem = new PageDataItem();
		rootDataItem.setId("dataId1");
		rootDataItem.setParentId(Constant.TREE_ROOT_ID.toString());
		rootDataItem.setName("root");
		rootDataItem.setPageId(pageId);
		rootDataItem.setSeq(1);
		rootDataItem.setType("Object");
		
		PageDataItem dataItem1 = new PageDataItem();
		dataItem1.setId("dataId2");
		dataItem1.setParentId("dataId1");
		dataItem1.setName("foo");
		dataItem1.setPageId(pageId);
		dataItem1.setSeq(2); // 页面级排序
		dataItem1.setValue("bar"); // 默认值为 bar
		dataItem1.setType("String");
		
		model.setData(Arrays.asList(rootDataItem, dataItem1));
		
		// 为事件绑定一个事件处理函数后，就需要定义一个事件处理函数
		PageEventHandler func = new PageEventHandler();
		func.setId(handlerId);
		
		// 一个函数定义节点
		VisualNode node1 = new VisualNode();
		node1.setId("1");
		node1.setLeft(20);
		node1.setTop(20);
		node1.setCaption("事件处理函数");
		node1.setText("onValue");
		node1.setLayout(NodeLayout.FLOW_CONTROL.getKey());
		node1.setCategory(NodeCategory.FUNCTION.getKey());
		
		// 函数定义节点
		// 包含一个 output sequence port 和 一个 output data port
		OutputSequencePort outputSequencePort = new OutputSequencePort();
		outputSequencePort.setId("osp1");
		node1.setOutputSequencePorts(Collections.singletonList(outputSequencePort));
		
		DataPort outputDataPort = new DataPort();
		outputDataPort.setId("odp1");
		outputDataPort.setName("value");
		outputDataPort.setType(WidgetPropertyValueType.STRING.getKey());
		node1.setOutputDataPorts(Collections.singletonList(outputDataPort));
		
		// set variable node
		// 包含一个 input sequence port、一个 output sequence port 和 一个 input data port
		VisualNode node2 = new VisualNode();
		node2.setId("2");
		node2.setLeft(40);
		node2.setTop(40);
		node2.setCaption("Set foo");
		node2.setLayout(NodeLayout.DATA.getKey());
		node2.setCategory(NodeCategory.VARIABLE_SET.getKey());
		node2.setDataItemId(dataItem1.getId());
		
		InputSequencePort isp2 = new InputSequencePort();
		isp2.setId("isp2");
		node2.setInputSequencePort(isp2);
		
		OutputSequencePort osp2 = new OutputSequencePort();
		osp2.setId("osp2");
		node2.addOutputSequencePort(osp2);
		
		InputDataPort inputDataPort = new InputDataPort();
		inputDataPort.setId("idp2");
		inputDataPort.setName("set");
		inputDataPort.setType("String");
		inputDataPort.setValue("bar1"); // 注意，与 pageDataItem 中的 bar 不是一回事
		
		node2.addInputDataPort(inputDataPort);
		
		func.setNodes(Arrays.asList(node1, node2));
		
		// 一条序列连接线
		NodeConnection sc = new NodeConnection();
		sc.setId("sc1");
		sc.setFromNode(node1.getId());
		sc.setFromOutput(outputSequencePort.getId());
		sc.setToNode(node2.getId());
		sc.setToInput(isp2.getId());
		func.setSequenceConnections(Collections.singletonList(sc));
		
		// 一条数据连接线
		NodeConnection dc = new NodeConnection();
		dc.setId("dc1");
		dc.setFromNode(node1.getId());
		dc.setFromOutput(outputDataPort.getId());
		dc.setToNode(node2.getId());
		dc.setToInput(inputDataPort.getId());
		func.setDataConnections(Collections.singletonList(dc));
		
		// 需要定义一个节点和一个 port（因为事件包含一个输入参数）
		model.setFunctions(Collections.singletonList(func));

		repositoryResourceService.updatePageModel(null, null, model);
		repositoryResourceService.updatePageModel(null, null, model); // 执行两次，确保之前的数据已彻底删除
		
		RepositoryResource page = new RepositoryResource();
		page.setId(pageId);
		page.setParentId(savedProject.getId());
		
		PageModel savedModel = repositoryResourceService.getPageModel(page);
		
		assertThat(savedModel).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(model);
	}
	
	// 更新页面模型
	@Test
	public void updatePageModelWidgetsUpdate() {
		// 初始化数据
		// 1. 创建一个 API 仓库，类型为 Widget
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
		// 2. 创建一个 API 版本号
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setName("name");
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		apiVersion.setLastPublishTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);
		// 3. 在对应的 API 版本下添加两个部件，分别为每一个部件设置一个属性
		//  部件
		ApiWidget widget1 = new ApiWidget();
		widget1.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget1.setCode("0001");
		widget1.setName("Widget1");
		widget1.setLabel("Widget 1");
		widget1.setDescription("Description1");
		widget1.setCreateUserId(1);
		widget1.setCreateTime(LocalDateTime.now());
		ApiWidget savedWidget1 = apiComponentDao.save(widget1);
		//  属性
		ApiWidgetProperty widgetProperty11 = new ApiWidgetProperty();
		widgetProperty11.setApiWidgetId(savedWidget1.getId());
		widgetProperty11.setApiRepoVersionId(savedApiRepoVersion.getId());
		widgetProperty11.setCode("0011");
		widgetProperty11.setDefaultValue("default_value_11");
		widgetProperty11.setDescription("description_11");
		widgetProperty11.setName("prop_name_11");
		widgetProperty11.setValueType(WidgetPropertyValueType.STRING.getKey());
		apiComponentAttrDao.save(widgetProperty11);
		
		// 4. 创建一个 ide 组件库，实现上述的 API 仓库
		//    因为在查数据时，可直接获得 ide 组件仓库的版本信息
		//    然后根据版本信息，可直接获取 API 组件库的版本信息
		//    所以准备数据时，可跳过这一步。
		// 5. 为 ide 组件库创建一个版本号，实现上述的 API 版本
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1); // 组件仓库 id 为 1
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setName("name");
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setAppType(AppType.WEB);
		componentRepoVersion.setBuild("dojo");
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersion.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 6. 创建一个项目
		//    为项目添加依赖时，直接使用项目标识即可
		//    所以准备数据时，可跳过这一步
		Integer repositoryId = 1;
		Integer userId = 2;
		
		RepositoryResource project = new RepositoryResource();
		project.setRepositoryId(repositoryId);
		project.setParentId(Constant.TREE_ROOT_ID);
		project.setKey("project1");
		project.setAppType(AppType.WEB);
		project.setResourceType(RepositoryResourceType.PROJECT);
		project.setCreateTime(LocalDateTime.now());
		project.setCreateUserId(userId);
		project.setSeq(1);
		RepositoryResource savedProject = repositortResourceDao.save(project);
		
		// 7. 将创建的 ide 组件库的一个版本添加为项目依赖
		ProjectDependency dependency = new ProjectDependency();
		dependency.setRepositoryId(repositoryId);
		dependency.setProjectId(savedProject.getId());
		dependency.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependency.setCreateUserId(1);
		dependency.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(dependency);
		
		// 新增页面模型
		PageModel model = new PageModel();
		
		Integer pageId = 1;
		model.setPageId(pageId);
		
		AttachedWidget attachedWidget1 = new AttachedWidget();
		attachedWidget1.setId("1");
		attachedWidget1.setParentId(Constant.TREE_ROOT_ID.toString());
		attachedWidget1.setApiRepoId(savedApiRepo.getId());
		attachedWidget1.setWidgetCode("0001");
		attachedWidget1.setWidgetName("Widget1"); // 只能取 widgetName 的值
		attachedWidget1.setWidgetId(savedWidget1.getId());
		AttachedWidgetProperty attachedWidgetProperty11 = new AttachedWidgetProperty();
		attachedWidgetProperty11.setId("11");
		attachedWidgetProperty11.setCode("0011");
		attachedWidgetProperty11.setName("prop_name_11"); // 注意，不会直接在页面模型中存储该值，如果 label 有值，则优先取 label 值
		attachedWidgetProperty11.setValue("value11");
		attachedWidgetProperty11.setValueType(WidgetPropertyValueType.STRING.getKey());
		attachedWidget1.setProperties(Collections.singletonList(attachedWidgetProperty11));
		
		model.setWidgets(Collections.singletonList(attachedWidget1));
		
		repositoryResourceService.updatePageModel(null, null, model); // 第一次执行
		repositoryResourceService.updatePageModel(null, null, model); // 第二次执行
		
		RepositoryResource page = new RepositoryResource();
		page.setId(pageId);
		page.setParentId(savedProject.getId());
		
		PageModel savedModel = repositoryResourceService.getPageModel(page);
		
		assertThat(savedModel).usingRecursiveComparison().isEqualTo(model);
	}
	
	// 要测试，API 库的版本升级后，部件的属性增加的情况。
	// 此时 API 部件属性列表中有新增属性，但是之前存的页面模型中不包含新增属性
	// 则在查询时要包含新增属性
	@Test
	public void updatePageModelUpgrade() {
		// 初始化数据
		// 1. 创建一个 API 仓库，类型为 Widget
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
		// 2. 创建一个 API 版本号
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setName("name");
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		apiVersion.setLastPublishTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);
		// 3. 在对应的 API 版本下添加一个部件，并设置一个属性
		// 3.1 部件1
		ApiWidget widget1 = new ApiWidget();
		widget1.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget1.setCode("0001");
		widget1.setName("Widget1");
		widget1.setLabel("Widget 1");
		widget1.setDescription("Description1");
		widget1.setCreateUserId(1);
		widget1.setCreateTime(LocalDateTime.now());
		ApiWidget savedWidget1 = apiComponentDao.save(widget1);
		// 3.1 属性1
		ApiWidgetProperty widgetProperty11 = new ApiWidgetProperty();
		widgetProperty11.setApiWidgetId(savedWidget1.getId());
		widgetProperty11.setApiRepoVersionId(savedApiRepoVersion.getId());
		widgetProperty11.setCode("0011");
		widgetProperty11.setDefaultValue("default_value_11");
		widgetProperty11.setDescription("description_11");
		widgetProperty11.setName("prop_name_11");
		widgetProperty11.setValueType(WidgetPropertyValueType.STRING.getKey());
		apiComponentAttrDao.save(widgetProperty11);
		// 4. 创建一个 ide 组件库，实现上述的 API 仓库
		//    因为在查数据时，可直接获得 ide 组件仓库的版本信息
		//    然后根据版本信息，可直接获取 API 组件库的版本信息
		//    所以准备数据时，可跳过这一步。
		// 5. 为 ide 组件库创建一个版本号，实现上述的 API 版本
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1); // 组件仓库 id 为 1
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setName("name");
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setAppType(AppType.WEB);
		componentRepoVersion.setBuild("build");
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersion.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 6. 创建一个项目
		//    为项目添加依赖时，直接使用项目标识即可
		//    所以准备数据时，可跳过这一步
		Integer repositoryId = 1;
		Integer userId = 2;
		
		RepositoryResource project = new RepositoryResource();
		project.setRepositoryId(repositoryId);
		project.setParentId(Constant.TREE_ROOT_ID);
		project.setKey("project1");
		project.setAppType(AppType.WEB);
		project.setResourceType(RepositoryResourceType.PROJECT);
		project.setCreateTime(LocalDateTime.now());
		project.setCreateUserId(userId);
		project.setSeq(1);
		RepositoryResource savedProject = repositortResourceDao.save(project);
		
		// 7. 将创建的 ide 组件库的一个版本添加为项目依赖
		ProjectDependency dependency = new ProjectDependency();
		dependency.setRepositoryId(repositoryId);
		dependency.setProjectId(savedProject.getId());
		dependency.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependency.setCreateUserId(1);
		dependency.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(dependency);
		
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
		
		model.setWidgets(Collections.singletonList(attachedWidget1));
		
		repositoryResourceService.updatePageModel(null, null, model);
		
		RepositoryResource page = new RepositoryResource();
		page.setId(pageId);
		page.setParentId(savedProject.getId());
		PageModel result = repositoryResourceService.getPageModel(page);
		
		assertThat(result.getWidgets().get(0).getProperties()).hasSize(1);
		
		AttachedWidgetProperty actualAttachedWidgetProperty = result.getWidgets().get(0).getProperties().get(0);
		
		assertThat(actualAttachedWidgetProperty.getCode()).isEqualTo("0011");
		assertThat(actualAttachedWidgetProperty.getName()).isEqualTo("prop_name_11");
		assertThat(actualAttachedWidgetProperty.getValueType()).isEqualTo("string");
		
		assertThat(actualAttachedWidgetProperty.getId()).hasSize(32); // uuid
		assertThat(actualAttachedWidgetProperty.getValue()).isEqualTo("default_value_11");
	}

	@Test
	public void updatePageModelWidgetsReadPageFileInGit(@TempDir Path rootFolder) throws IOException {
		// 初始化数据
		// 1. 创建一个 API 仓库，类型为 Widget
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
		// 2. 创建一个 API 版本号
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setName("name");
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		apiVersion.setLastPublishTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);
		// 3. 在对应的 API 版本下添加两个部件，分别为每一个部件设置一个属性
		// 3.1 部件1
		ApiWidget widget1 = new ApiWidget();
		widget1.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget1.setCode("0001");
		widget1.setName("Widget1");
		widget1.setLabel("Widget 1");
		widget1.setDescription("Description1");
		widget1.setCreateUserId(1);
		widget1.setCreateTime(LocalDateTime.now());
		ApiWidget savedWidget1 = apiComponentDao.save(widget1);
		// 3.1 属性1
		ApiWidgetProperty widgetProperty11 = new ApiWidgetProperty();
		widgetProperty11.setApiWidgetId(savedWidget1.getId());
		widgetProperty11.setApiRepoVersionId(savedApiRepoVersion.getId());
		widgetProperty11.setCode("0011");
		widgetProperty11.setDefaultValue("default_value_11");
		widgetProperty11.setDescription("description_11");
		widgetProperty11.setName("prop_name_11");
		widgetProperty11.setValueType(WidgetPropertyValueType.STRING.getKey());
		apiComponentAttrDao.save(widgetProperty11);
		// 3.2 部件2
		ApiWidget widget2 = new ApiWidget();
		widget2.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget2.setCode("0002");
		widget2.setName("Widget2");
		widget2.setDescription("Description2");
		widget2.setCreateUserId(1);
		widget2.setCreateTime(LocalDateTime.now());
		ApiWidget savedWidget2 = apiComponentDao.save(widget2);
		// 3.2  属性2
		ApiWidgetProperty widgetProperty21 = new ApiWidgetProperty();
		widgetProperty21.setApiWidgetId(savedWidget2.getId());
		widgetProperty21.setApiRepoVersionId(savedApiRepoVersion.getId());
		widgetProperty21.setCode("0021");
		widgetProperty21.setDefaultValue("default_value_21");
		widgetProperty21.setDescription("description_21");
		widgetProperty21.setName("prop_name_21");
		widgetProperty21.setLabel("prop_label_21");
		widgetProperty21.setValueType(WidgetPropertyValueType.STRING.getKey());
		apiComponentAttrDao.save(widgetProperty21);
		// 4. 创建一个 ide 组件库，实现上述的 API 仓库
		//    因为在查数据时，可直接获得 ide 组件仓库的版本信息
		//    然后根据版本信息，可直接获取 API 组件库的版本信息
		//    所以准备数据时，可跳过这一步。
		// 5. 为 ide 组件库创建一个版本号，实现上述的 API 版本
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1); // 组件仓库 id 为 1
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setName("name");
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setAppType(AppType.WEB);
		componentRepoVersion.setBuild("build");
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersion.setLastPublishTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 6. 创建一个项目
		//    为项目添加依赖时，直接使用项目标识即可
		//    所以准备数据时，可跳过这一步
		Integer repositoryId = 1;
		Integer userId = 2;
		String projectKey = "project1";
		
		RepositoryResource project = new RepositoryResource();
		project.setRepositoryId(repositoryId);
		project.setParentId(Constant.TREE_ROOT_ID);
		project.setKey(projectKey);
		project.setAppType(AppType.WEB);
		project.setResourceType(RepositoryResourceType.PROJECT);
		project.setCreateTime(LocalDateTime.now());
		project.setCreateUserId(userId);
		project.setSeq(1);
		RepositoryResource savedProject = repositortResourceDao.save(project);
		
		// 7. 将创建的 ide 组件库的一个版本添加为项目依赖
		ProjectDependency dependency = new ProjectDependency();
		dependency.setRepositoryId(repositoryId);
		dependency.setProjectId(savedProject.getId());
		dependency.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependency.setCreateUserId(1);
		dependency.setCreateTime(LocalDateTime.now());
		projectDependencyDao.save(dependency);
		
		AttachedWidget attachedWidget1 = new AttachedWidget();
		attachedWidget1.setId("1");
		attachedWidget1.setParentId(Constant.TREE_ROOT_ID.toString());
		attachedWidget1.setApiRepoId(savedApiRepo.getId());
		attachedWidget1.setWidgetCode("0001");
		attachedWidget1.setWidgetName("Widget 1"); // 如果 label 有值，则用 label 的值
		attachedWidget1.setWidgetId(savedWidget1.getId());
		AttachedWidgetProperty attachedWidgetProperty11 = new AttachedWidgetProperty();
		attachedWidgetProperty11.setId("11");
		attachedWidgetProperty11.setCode("0011");
		attachedWidgetProperty11.setName("prop_name_11"); // 注意，不会直接在页面模型中存储该值，如果 label 有值，则优先取 label 值
		attachedWidgetProperty11.setValue("value11");
		attachedWidgetProperty11.setValueType(WidgetPropertyValueType.STRING.getKey());
		attachedWidget1.setProperties(Collections.singletonList(attachedWidgetProperty11));
		
		AttachedWidget attachedWidget2 = new AttachedWidget();
		attachedWidget2.setId("2"); // id 是在前台生成的
		attachedWidget2.setParentId("1"); // 父部件是 widget1
		attachedWidget2.setApiRepoId(savedApiRepo.getId());
		attachedWidget2.setWidgetCode("0002");
		attachedWidget2.setWidgetName("Widget2"); // 如果 label 有值，则用 label 的值
		attachedWidget2.setWidgetId(savedWidget2.getId());
		AttachedWidgetProperty attachedWidgetProperty21 = new AttachedWidgetProperty();
		attachedWidgetProperty21.setId("21"); // id 是在前台生成的
		attachedWidgetProperty21.setCode("0021");
		attachedWidgetProperty21.setName("prop_label_21");
		attachedWidgetProperty21.setValue("value21");
		attachedWidgetProperty21.setValueType(WidgetPropertyValueType.STRING.getKey());
		attachedWidget2.setProperties(Collections.singletonList(attachedWidgetProperty21));
		
		// 新增页面模型
		PageModel model = new PageModel();
		Integer pageId = 1;
		model.setPageId(pageId);
		model.setWidgets(Arrays.asList(attachedWidget1, attachedWidget2));

		UserInfo user = new UserInfo();
		user.setId(userId);
		user.setLoginName("jack");
		when(userService.findById(anyInt())).thenReturn(Optional.of(user));
		
		String owner = "jack";
		String repoName = "repo1";
		Repository repository = new Repository();
		repository.setId(repositoryId);
		repository.setCreateUserName(owner);
		repository.setCreateUserId(userId);
		repository.setName(repoName);
		
		RepositoryResource resource = new RepositoryResource();
		String pageKey = "key1";
		resource.setRepositoryId(repositoryId);
		resource.setParentId(savedProject.getId());
		resource.setAppType(AppType.WEB);
		resource.setKey(pageKey);
		resource.setName("name");
		resource.setResourceType(RepositoryResourceType.PAGE);
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		RepositoryContext context = new RepositoryContext(owner, repoName, rootFolder.toString());
		Files.createDirectories(context.getGitRepositoryDirectory().resolve(projectKey));
		
		repositoryResourceService.updatePageModel(repository, resource, model);
		
		String mainPageJsonString = Files.readString(context.getGitRepositoryDirectory().resolve(projectKey).resolve(pageKey + ".page.web.json"));
		assertThat(JsonUtil.fromJsonObject(mainPageJsonString, PageModel.class)).usingRecursiveComparison().isEqualTo(model);
	}

	@Test
	public void updatePageModelDataNew() {
		Integer repositoryId = 1;
		Integer userId = 2;
		
		RepositoryResource project = new RepositoryResource();
		project.setRepositoryId(repositoryId);
		project.setParentId(Constant.TREE_ROOT_ID);
		project.setKey("projectKey");
		project.setAppType(AppType.WEB);
		project.setResourceType(RepositoryResourceType.PROJECT);
		project.setCreateTime(LocalDateTime.now());
		project.setCreateUserId(userId);
		project.setSeq(1);
		RepositoryResource savedProject = repositortResourceDao.save(project);
		
		Integer pageId = 2;
		PageModel model = new PageModel();
		model.setPageId(pageId);
		
		PageDataItem item1 = new PageDataItem();
		item1.setPageId(pageId);
		item1.setId("1");
		item1.setParentId(Constant.TREE_ROOT_ID.toString());
		item1.setName("root");
		item1.setType("Object");
		
		PageDataItem item2 = new PageDataItem();
		item2.setPageId(pageId);
		item2.setId("2");
		item2.setParentId("1");
		item2.setName("foo");
		item2.setType("String");
		item2.setValue("bar");
		
		model.setWidgets(Collections.emptyList());
		model.setData(Arrays.asList(item1, item2));
		
		repositoryResourceService.updatePageModel(null, null, model);
		
		// 保存成功后，补上排序号
		item1.setSeq(1);
		item2.setSeq(2);
		
		RepositoryResource page = new RepositoryResource();
		page.setId(pageId);
		page.setParentId(savedProject.getId());

		PageModel savedModel = repositoryResourceService.getPageModel(page);
		assertThat(savedModel.getData())
			.hasSize(2)
			.usingFieldByFieldElementComparator()
			.contains(item1)
			.contains(item2);
	}
	
	@Test
	public void getPageModelEmptyPage() {
		RepositoryResource page = new RepositoryResource();
		page.setId(1);
		
		PageModel savedModel = repositoryResourceService.getPageModel(page);
		assertThat(savedModel.getWidgets()).isEmpty();
		assertThat(savedModel.getData()).isEmpty();
		assertThat(savedModel.getFunctions()).isEmpty();
	}
	
	@Test
	public void findProjectNoData() {
		Integer repositoryId = 1;
		String projectKey = "project1";
		assertThat(repositoryResourceService.findProject(repositoryId, projectKey)).isEmpty();
	}
	
	@Test
	public void findProjectSuccess() {
		Integer repositoryId = 1;
		Integer userId = 2;
		String projectKey = "project1";
		
		RepositoryResource project = new RepositoryResource();
		project.setRepositoryId(repositoryId);
		project.setParentId(Constant.TREE_ROOT_ID);
		project.setKey(projectKey);
		project.setAppType(AppType.WEB);
		project.setResourceType(RepositoryResourceType.PROJECT);
		project.setCreateTime(LocalDateTime.now());
		project.setCreateUserId(userId);
		project.setSeq(1);
		RepositoryResource savedProject = repositortResourceDao.save(project);
		
		RepositoryResource page = new RepositoryResource();
		page.setRepositoryId(repositoryId);
		page.setParentId(savedProject.getId());
		page.setKey("page1");
		page.setAppType(AppType.WEB);
		page.setResourceType(RepositoryResourceType.PAGE);
		page.setCreateTime(LocalDateTime.now());
		page.setCreateUserId(userId);
		page.setSeq(1);
		repositortResourceDao.save(project);
		
		assertThat(repositoryResourceService.findProject(repositoryId, projectKey)).isPresent();
	}
}
