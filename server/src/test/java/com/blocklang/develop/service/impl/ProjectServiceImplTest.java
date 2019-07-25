package com.blocklang.develop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
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
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.dao.ProjectAuthorizationDao;
import com.blocklang.develop.dao.ProjectCommitDao;
import com.blocklang.develop.dao.ProjectDao;
import com.blocklang.develop.dao.ProjectFileDao;
import com.blocklang.develop.dao.ProjectResourceDao;
import com.blocklang.develop.data.GitCommitInfo;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectAuthorization;
import com.blocklang.develop.model.ProjectContext;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.develop.service.ProjectService;
import com.blocklang.release.dao.AppDao;

public class ProjectServiceImplTest extends AbstractServiceTest{
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private ProjectDao projectDao;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private AppDao appDao;
	
	@Autowired
	private ProjectResourceService projectResourceService;
	
	@Autowired
	private ProjectResourceDao projectResourceDao;
	
	@Autowired
	private ProjectFileDao projectFileDao;
	
	@Autowired
	private ProjectAuthorizationDao projectAuthorizationDao;
	
	@Autowired
	private ProjectCommitDao projectCommitDao;
	
	@MockBean
	private PropertyService propertyService;
	
	@Test
	public void find_no_data() {
		Optional<Project> projectOption = projectService.find("not-exist-owner", "not-exist-name");
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
		
		Project project = new Project();
		project.setName("project_name");
		project.setIsPublic(true);
		project.setLastActiveTime(LocalDateTime.now());
		project.setCreateUserId(userId);
		project.setCreateTime(LocalDateTime.now());
		
		projectDao.save(project);
		
		Optional<Project> projectOption = projectService.find("user_name", "project_name");
		assertThat(projectOption).isPresent();
		assertThat(projectOption.get().getCreateUserName()).isEqualTo("user_name");
	}
	
	@Test
	public void create_success() throws IOException {
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
		
		Integer projectId = projectService.create(userInfo, project).getId();
		
		// 断言
		// 项目基本信息已保存
		assertThat(projectDao.findById(projectId).get()).hasNoNullFieldsOrPropertiesExcept("lastUpdateUserId", "lastUpdateTime", "avatarUrl", "accessLevel");
		
		// 项目授权信息已保存，项目创建者具有 admin 权限
		assertThat(projectAuthorizationDao.findAllByUserId(userId)).hasSize(1).allMatch(projectAuth -> {
			return projectAuth.getAccessLevel() == AccessLevel.ADMIN && projectAuth.getCreateUserId() == userId;
		});
		
		// APP 基本信息已保存
		assertThat(appDao.findByProjectId(projectId).get()).hasNoNullFieldsOrPropertiesExcept("lastUpdateUserId", "lastUpdateTime");
		
		// 已创建入口程序模块
		// 传入 Main，首字母大写是为了测试忽略大小写
		assertThat(projectResourceDao.findByProjectIdAndParentIdAndResourceTypeAndAppTypeAndKeyIgnoreCase(
				projectId, 
				Constant.TREE_ROOT_ID, 
				ProjectResourceType.PAGE,
				AppType.WEB,
				"Main").get())
			.hasNoNullFieldsOrPropertiesExcept(
					"lastUpdateUserId", 
					"lastUpdateTime", 
					"description",
					"latestCommitId",
					"latestShortMessage",
					"latestFullMessage",
					"latestCommitTime",
					"messageSource",
					"gitStatus");
		
		// 已在项目资源表中登记 README.md 文件
		ProjectResource readmeResource = projectResourceDao.findByProjectIdAndParentIdAndResourceTypeAndAppTypeAndKeyIgnoreCase(
				projectId, 
				Constant.TREE_ROOT_ID, 
				ProjectResourceType.FILE,
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
		assertThat(projectFileDao.findByProjectResourceId(readmeResource.getId()).get()).hasNoNullFieldsOrProperties();
		
		// 已在项目资源表中登记 DEPENDENCE.json 文件
		ProjectResource dependenceResource = projectResourceDao.findByProjectIdAndParentIdAndResourceTypeAndAppTypeAndKeyIgnoreCase(
				projectId, 
				Constant.TREE_ROOT_ID, 
				ProjectResourceType.DEPENDENCE, 
				AppType.UNKNOWN, 
				ProjectResource.DEPENDENCE_KEY).get();
		assertThat(dependenceResource).hasNoNullFieldsOrPropertiesExcept(
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
		ProjectContext context = new ProjectContext("user_name", "project_name", rootFolder.getPath());
		assertThat(GitUtils.isGitRepo(context.getGitRepositoryDirectory())).isTrue();
		
		// 在创建一个仓库时，会执行一个 git commit，此操作也保存在 project_commit 中
		assertThat(projectCommitDao.findAllByProjectIdAndBranchOrderByCommitTimeDesc(projectId, "master").get(0)).hasNoNullFieldsOrPropertiesExcept("fullMessage");
		
		// TODO: 确认已将应用的模板也保存到了 git 仓库中
		// 为了便于测试，可能要将 applyTemplate 方法单独提取出来
		
		// 确认 git 仓库中有 main.json 文件，并比较其内容
		assertThat(Files.readString(context.getGitRepositoryDirectory().resolve("main.page.web.json")))
			.contains("resource")
			.contains("uiModel")
			.contains("view")
			.contains("data")
			.contains("methods");
		
		// 确认 git 仓库中有 README.md 文件，并比较其内容
		String expectedReadmeContext = "# project_name\r\n\r\n**TODO: 在这里添加项目介绍，帮助感兴趣的人快速了解您的项目。**";
		assertThat(Files.readString(context.getGitRepositoryDirectory().resolve("README.md"))).isEqualTo(expectedReadmeContext);
		
		// 确认 git 仓库中有 DEPENDENCE.json 文件，内容为空 json 对象
		assertThat(Files.readString(context.getGitRepositoryDirectory().resolve("DEPENDENCE.json"))).isEqualTo("{}");
	}
	
	@Test
	public void find_can_access_projects_created_no_data() {
		List<Project> projects = projectService.findCanAccessProjectsByUserId(1);
		assertThat(projects).isEmpty();
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
		
		Project project = new Project();
		project.setName("project_name");
		project.setIsPublic(true);
		project.setDescription("description");
		project.setLastActiveTime(LocalDateTime.now());
		project.setCreateUserId(userId);
		project.setCreateTime(LocalDateTime.now());
		
		Integer savedProjectId = projectDao.save(project).getId();
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setProjectId(savedProjectId);
		auth.setUserId(userId);
		auth.setAccessLevel(AccessLevel.ADMIN); // 项目创建者具有管理员权限
		auth.setCreateTime(LocalDateTime.now());
		auth.setCreateUserId(userId);
		projectAuthorizationDao.save(auth);
		
		List<Project> projects = projectService.findCanAccessProjectsByUserId(userId);
		assertThat(projects).hasSize(1).allMatch(each -> each.getCreateUserName().equals("user_name"));
		
		projects = projectService.findCanAccessProjectsByUserId(userId + 1);
		assertThat(projects).isEmpty();
	}
	
	@Test
	public void find_can_access_projects_order_by_last_active_time_desc() {
		// 第一条记录
		Project project = new Project();
		project.setName("project_name");
		project.setIsPublic(true);
		project.setDescription("description");
		project.setLastActiveTime(LocalDateTime.now());
		project.setCreateUserId(1);
		project.setCreateTime(LocalDateTime.now());
		project.setCreateUserName("user_name");
		
		Integer savedProjectId = projectDao.save(project).getId();
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setProjectId(savedProjectId);
		auth.setUserId(1);
		auth.setAccessLevel(AccessLevel.ADMIN);
		auth.setCreateTime(LocalDateTime.now());
		auth.setCreateUserId(1);
		projectAuthorizationDao.save(auth);
		
		// 第二条记录
		project = new Project();
		project.setName("project_name_2");
		project.setIsPublic(true);
		project.setDescription("description");
		project.setLastActiveTime(LocalDateTime.now().plusSeconds(1));
		project.setCreateUserId(1);
		project.setCreateTime(LocalDateTime.now());
		project.setCreateUserName("user_name");
		
		savedProjectId = projectDao.save(project).getId();
		
		auth = new ProjectAuthorization();
		auth.setProjectId(savedProjectId);
		auth.setUserId(1);
		auth.setAccessLevel(AccessLevel.ADMIN);
		auth.setCreateTime(LocalDateTime.now());
		auth.setCreateUserId(1);
		projectAuthorizationDao.save(auth);
	
		List<Project> projects = projectService.findCanAccessProjectsByUserId(1);
		assertThat(projects).hasSize(2).isSortedAccordingTo(Comparator.comparing(Project::getLastActiveTime).reversed());
	}

	@Test
	public void find_latest_commit_success() throws IOException {
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
		
		GitCommitInfo commitInfo = projectService.findLatestCommitInfo(savedProject, null).get();
		assertThat(commitInfo.getShortMessage()).isEqualTo("First Commit");
		assertThat(commitInfo.getUserName()).isEqualTo("user_name");
	}
	
	@Test
	public void find_latest_commit_for_untracked_and_empty_folder() throws IOException {
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
		resource.setKey("group1");
		resource.setResourceType(ProjectResourceType.GROUP);
		resource.setAppType(AppType.UNKNOWN);
		resource.setCreateTime(LocalDateTime.now());
		resource.setCreateUserId(userId);
		resource.setProjectId(savedProject.getId());
		resource.setParentId(Constant.TREE_ROOT_ID);
		projectResourceService.insert(savedProject, resource);
		
		assertThat(projectService.findLatestCommitInfo(savedProject, "group1")).isEmpty();
	}
}
