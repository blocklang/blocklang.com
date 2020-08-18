package com.blocklang.develop.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.constant.DeployState;
import com.blocklang.develop.data.CheckRepositoryNameParam;
import com.blocklang.develop.data.GitCommitInfo;
import com.blocklang.develop.data.NewRepositoryParam;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.ProjectDeploy;
import com.blocklang.develop.model.RepositoryFile;
import com.blocklang.develop.service.ProjectDeployService;
import com.blocklang.develop.service.RepositoryFileService;
import com.blocklang.develop.service.RepositoryPermissionService;
import com.blocklang.develop.service.RepositoryResourceService;
import com.blocklang.develop.service.RepositoryService;
import com.blocklang.release.model.AppRelease;
import com.blocklang.release.service.AppReleaseService;

import io.restassured.http.ContentType;

@WebMvcTest(RepositoryController.class)
public class RepositoryControllerTest extends AbstractControllerTest{

	@MockBean
	private RepositoryService repositoryService;
	@MockBean
	private RepositoryResourceService repositoryResourceService;
	@MockBean
	private RepositoryFileService repositoryFileService;
	@MockBean
	private ProjectDeployService projectDeployService;
	@MockBean
	private AppReleaseService appReleaseService;
	@MockBean
	private RepositoryPermissionService repositoryPermissionService;

	@Test
	public void checkNameAnonymousCanNotCheck() {
		CheckRepositoryNameParam param = new CheckRepositoryNameParam();
		param.setOwner("owner");
		param.setName("good-name");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/check-name")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	// 注意，这里的用户名是必填的，且必须是当前登录用户
	@WithMockUser(username = "other")
	@Test
	public void checkNameUserIsUnauthorization() {
		CheckRepositoryNameParam param = new CheckRepositoryNameParam();
		param.setOwner("owner");
		param.setName("good-name");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/check-name")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "owner")
	@Test
	public void checkNameIsBlank() {
		CheckRepositoryNameParam param = new CheckRepositoryNameParam();
		param.setOwner("owner");
		param.setName(" ");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/check-name")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.name", hasItem("仓库名不能为空"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "owner")
	@Test
	public void checkNameIsInvalid() {
		CheckRepositoryNameParam param = new CheckRepositoryNameParam();
		param.setOwner("owner");
		param.setName("中文");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/check-name")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.name", hasItem("只允许字母、数字、中划线(-)、下划线(_)、点(.)"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "owner")
	@Test
	public void checkNameIsUsed() {
		CheckRepositoryNameParam param = new CheckRepositoryNameParam();
		param.setOwner("owner");
		param.setName("good-name");
		
		Repository project = new Repository();
		project.setId(1);
		project.setName("good-name");

		when(repositoryService.find(eq("owner"), eq("good-name"))).thenReturn(Optional.of(project));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/check-name")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.name", hasItem("owner下已存在<strong>good-name</strong>仓库"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "owner")
	@Test
	public void checkNamePass() {
		CheckRepositoryNameParam param = new CheckRepositoryNameParam();
		param.setOwner("owner");
		param.setName("good-name");

		when(repositoryService.find(eq("owner"), eq("good-name"))).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/check-name")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("{}"));
	}

	// 因为新建项目和校验项目名称中的校验逻辑完全相同
	// 所以此处不再重复测试所有逻辑，而是确认其中包含测试逻辑即可。
	@WithMockUser(username = "owner")
	@Test
	public void newRepositoryHasValidateName() {
		NewRepositoryParam param = new NewRepositoryParam();
		param.setOwner("owner");
		param.setName("good-name");
		
		Repository repository = new Repository();
		repository.setId(1);
		repository.setName("good-name");

		when(repositoryService.find(eq("owner"), eq("good-name"))).thenReturn(Optional.of(repository));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.name", hasItem("owner下已存在<strong>good-name</strong>仓库"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "owner")
	@Test
	public void newRepositorySuccess() {
		NewRepositoryParam param = new NewRepositoryParam();
		param.setOwner("owner");
		param.setName("good-name");
		param.setIsPublic(true);
		param.setDescription("description");

		when(repositoryService.find(eq("owner"), eq("good-name"))).thenReturn(Optional.empty());
		
		UserInfo user = new UserInfo();
		user.setId(1);
		user.setLoginName("owner");
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		Repository savedRepository = new Repository();
		savedRepository.setId(1);
		savedRepository.setName("good-name");
		when(repositoryService.createRepository(any(), any())).thenReturn(savedRepository);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos")
		.then()
			.statusCode(HttpStatus.SC_CREATED)
			.body("name", equalTo("good-name"),
					"id", is(notNullValue()));
	}

	@Test
	public void getRepositoryReadmeButRepositoryNotExist() {
		String owner = "owner";
		String repoName = "public-repository";
		
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.TEXT)
		.when()
			.get("/repos/{owner}/{repoName}/readme", owner, repoName)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void getReadmeCanNotReadRepository() {
		String owner = "owner";
		String repositoryName = "public-repository";
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.TEXT)
		.when()
			.get("/repos/{owner}/{repoName}/readme", owner, repositoryName)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@Test
	public void getReadmeSuccessNoReadme() {
		String owner = "owner";
		String repositoryName = "public-repository";
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		when(repositoryFileService.findReadme(anyInt())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.TEXT)
		.when()
			.get("/repos/{owner}/{repoName}/readme", owner, repositoryName)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo(""));
	}
	
	@Test
	public void getReadmeSuccessHasReadme() {
		String owner = "owner";
		String repositoryName = "public-repository";
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		RepositoryFile projectFile = new RepositoryFile();
		projectFile.setContent("# public-repository");
		when(repositoryFileService.findReadme(anyInt())).thenReturn(Optional.of(projectFile));
		
		given()
			.contentType(ContentType.TEXT)
		.when()
			.get("/repos/{owner}/{repoName}/readme", owner, repositoryName)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("# public-repository"));
	}

	@Test
	public void getYourReposAnonymousCanNotGet() {
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/user/repos")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("owner")
	@Test
	public void getYourReposUserNotExist() {
		when(userService.findByLoginName(anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/user/repos")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("owner")
	@Test
	public void getYourReposSuccess() {
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		when(repositoryService.findCanAccessRepositoriesByUserId(anyInt())).thenReturn(Collections.emptyList());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/user/repos")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", is(0));
	}
	
	@Test
	public void getRepoNotExist() {
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}", "zhangsan", "my-repository")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void getRepoCanNotReadRepo() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}", "jack", "my-repository")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "other")
	@Test
	public void getRepoSuccess() {
		Repository repository = new Repository();
		repository.setId(1);
		repository.setName("my-repository");
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		when(repositoryPermissionService.findTopestPermission(any(), any())).thenReturn(AccessLevel.WRITE);
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}", "jack", "my-repository")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("name", equalTo("my-repository"),
					"accessLevel", equalTo(AccessLevel.WRITE.getKey()));
	}
	
	@Test
	public void getLatestCommitInvalidParentId() {
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/latest-commit/{parentId}", "zhangsan", "my-repository", "a-invalid-parent-id")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void getLatestCommitRepoNotExist() {
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/latest-commit/{parentId}", "zhangsan", "my-repository", "-1")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void getLatestCommitCanNotReadRepo() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/latest-commit/{parentId}", "zhangsan", "my-repository", "-1")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@Test
	public void getLatestCommitAtRoot() {
		Repository repository = new Repository();
		repository.setName("my-repository");
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		when(repositoryResourceService.findParentPathes(anyInt())).thenReturn(Collections.emptyList());
		
		GitCommitInfo commitInfo = new GitCommitInfo();
		commitInfo.setShortMessage("message");
		when(repositoryService.findLatestCommitInfo(any(), anyString())).thenReturn(Optional.of(commitInfo));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/latest-commit/{parentId}", "zhangsan", "my-repository", "-1")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("shortMessage", equalTo("message"));
	}
	
	@Test
	public void getLatestCommitInfoAtSubFolder() {
		Repository project = new Repository();
		project.setName("my-project");
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		when(repositoryResourceService.findParentPathes(anyInt())).thenReturn(Collections.singletonList("a"));
		
		GitCommitInfo commitInfo = new GitCommitInfo();
		commitInfo.setShortMessage("message");
		when(repositoryService.findLatestCommitInfo(any(), anyString())).thenReturn(Optional.of(commitInfo));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{project}/latest-commit/{pathId}", "zhangsan", "my-project", "1")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("shortMessage", equalTo("message"));
	}

	@Test
	public void get_deploy_setting_anonymous_can_not_get() {
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{project}/deploy_setting", "zhangsan", "my-project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("logged_user")
	@Test
	public void get_deploy_setting_project_not_exist() {
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{project}/deploy_setting", "zhangsan", "my-project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser("logged_user")
	@Test
	public void get_deploy_setting_can_not_read_project() {
		Repository project = new Repository();
		project.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{project}/deploy_setting", "zhangsan", "my-project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("logged_user")
	@Test
	public void get_deploy_setting_success() {
		Repository project = new Repository();
		project.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		// 登录用户信息
		UserInfo user = new UserInfo();
		user.setLoginName("logged_user");
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectDeploy deploy = new ProjectDeploy();
		deploy.setRegistrationToken("a");
		deploy.setDeployState(DeployState.UNDEPLOY);
		when(projectDeployService.findOrCreate(anyInt(), anyInt())).thenReturn(Optional.of(deploy));
		
		when(propertyService.findStringValue(eq(CmPropKey.INSTALL_API_ROOT_URL), anyString())).thenReturn("b");
		
		AppRelease appRelease = new AppRelease();
		appRelease.setVersion("0.1.0");
		when(appReleaseService.findLatestReleaseAppByAppName(anyString())).thenReturn(Optional.of(appRelease));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{project}/deploy_setting", "zhangsan", "my-project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("registrationToken", equalTo("a"),
					"url", equalTo("b"),
					"installerWindowsUrl", equalTo("/apps?appName=blocklang-installer&version=0.1.0&targetOs=windows&arch=x86_64"),
					"installerLinuxUrl", equalTo("/apps?appName=blocklang-installer&version=0.1.0&targetOs=linux&arch=x86_64"));
	}
}
