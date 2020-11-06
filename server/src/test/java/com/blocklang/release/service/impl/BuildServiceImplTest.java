package com.blocklang.release.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.constant.Constant;
import com.blocklang.core.dao.UserDao;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.RepositoryResourceType;
import com.blocklang.develop.dao.RepositoryDao;
import com.blocklang.develop.dao.RepositoryResourceDao;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.release.constant.ReleaseResult;
import com.blocklang.release.dao.AppDao;
import com.blocklang.release.dao.ProjectReleaseTaskDao;
import com.blocklang.release.model.App;
import com.blocklang.release.model.ProjectReleaseTask;
import com.blocklang.release.service.BuildService;

public class BuildServiceImplTest extends AbstractServiceTest{
	
	@Autowired
	private AppDao appDao;
	@Autowired
	private ProjectReleaseTaskDao projectReleaseTaskDao;
	@Autowired
	private BuildService buildService;
	@MockBean
	private PropertyService propertyService;
	@Autowired
	private UserDao userDao;
	@Autowired
	private RepositoryDao repositoryDao;
	@Autowired
	private RepositoryResourceDao repositoryResourceDao;
	
	// TODO: 解决 linux 上出现 java.nio.file.AccessDeniedException: /home/blocklang 的问题
	@Disabled
	@Test
	public void build_success() throws IOException {
		Integer projectId = Integer.MAX_VALUE;
		Repository project = new Repository();
		project.setId(projectId);
		project.setName("demo_project");
		project.setCreateUserName("jack");
		
		App app = new App();
		app.setProjectId(projectId);
		app.setAppName("demo_project");
		app.setCreateTime(LocalDateTime.now());
		app.setCreateUserId(1);
		appDao.save(app);
		
		ProjectReleaseTask task = new ProjectReleaseTask();
		task.setProjectId(projectId);
		task.setTitle("title");
		task.setDescription("description");
		task.setJdkReleaseId(2);
		task.setVersion("0.0.1");
		task.setStartTime(LocalDateTime.now());
		task.setReleaseResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(1);
		ProjectReleaseTask savedTask = projectReleaseTaskDao.save(task);
		
		buildService.build(project, savedTask);
	}
	
	@Test
	public void buildProject(@TempDir Path rootFolder) {
		String owner = "jack";
		String repoName = "repo1";
		
		// 创建一个用户
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		// 创建一个 git 仓库
		Repository repository = new Repository();
		repository.setName("repo1");
		repository.setIsPublic(true);
		repository.setLastActiveTime(LocalDateTime.now());
		repository.setCreateUserId(userId);
		repository.setCreateTime(LocalDateTime.now());
		repositoryDao.save(repository);
		
		// 在 git 仓库下创建一个小程序项目
		RepositoryResource project = new RepositoryResource();
		String projectKey = "project1";
		project.setRepositoryId(repository.getId());
		project.setParentId(Constant.TREE_ROOT_ID);
		project.setAppType(AppType.WEB);
		project.setKey(projectKey);
		project.setName("name");
		project.setResourceType(RepositoryResourceType.PAGE);
		project.setCreateUserId(1);
		project.setCreateTime(LocalDateTime.now());
		project.setSeq(1);
		repositoryResourceDao.save(project);
		
		when(propertyService.findStringValue(eq(CmPropKey.BLOCKLANG_ROOT_PATH), eq(""))).thenReturn(rootFolder.toString());
		//buildService.buildProject(owner, repoName, projectKey);
		
		assertThat(projectReleaseTaskDao.findAll()).hasSize(1);
	}
}
