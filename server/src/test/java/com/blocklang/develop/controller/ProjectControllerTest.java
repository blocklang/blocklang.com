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
import com.blocklang.develop.data.CheckProjectNameParam;
import com.blocklang.develop.data.GitCommitInfo;
import com.blocklang.develop.data.NewProjectParam;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectDeploy;
import com.blocklang.develop.model.ProjectFile;
import com.blocklang.develop.service.ProjectDeployService;
import com.blocklang.develop.service.ProjectFileService;
import com.blocklang.develop.service.ProjectPermissionService;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.develop.service.ProjectService;
import com.blocklang.release.model.AppRelease;
import com.blocklang.release.service.AppReleaseService;

import io.restassured.http.ContentType;

@WebMvcTest(ProjectController.class)
public class ProjectControllerTest extends AbstractControllerTest{

	@MockBean
	private ProjectService projectService;
	@MockBean
	private ProjectResourceService projectResourceService;
	@MockBean
	private ProjectFileService projectFileService;
	@MockBean
	private ProjectDeployService projectDeployService;
	@MockBean
	private AppReleaseService appReleaseService;
	@MockBean
	private ProjectPermissionService projectPermissionService;

	@Test
	public void check_name_anonymous_can_not_check() {
		CheckProjectNameParam param = new CheckProjectNameParam();
		param.setOwner("owner");
		param.setName("good-name");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/check-name")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	// 注意，这里的用户名是必填的，且必须是当前登录用户
	@WithMockUser(username = "other")
	@Test
	public void check_name_user_is_unauthorization_other_user() {
		CheckProjectNameParam param = new CheckProjectNameParam();
		param.setOwner("owner");
		param.setName("good-name");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/check-name")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "owner")
	@Test
	public void check_name_is_blank() {
		CheckProjectNameParam param = new CheckProjectNameParam();
		param.setOwner("owner");
		param.setName(" ");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/check-name")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.name", hasItem("项目名称不能为空"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "owner")
	@Test
	public void check_name_is_invalid() {
		CheckProjectNameParam param = new CheckProjectNameParam();
		param.setOwner("owner");
		param.setName("中文");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/check-name")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.name", hasItem("只允许字母、数字、中划线(-)、下划线(_)、点(.)"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "owner")
	@Test
	public void check_name_is_used() {
		CheckProjectNameParam param = new CheckProjectNameParam();
		param.setOwner("owner");
		param.setName("good-name");
		
		Project project = new Project();
		project.setId(1);
		project.setName("good-name");

		when(projectService.find(eq("owner"), eq("good-name"))).thenReturn(Optional.of(project));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/check-name")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.name", hasItem("owner下已存在<strong>good-name</strong>项目"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "owner")
	@Test
	public void check_name_pass() {
		CheckProjectNameParam param = new CheckProjectNameParam();
		param.setOwner("owner");
		param.setName("good-name");

		when(projectService.find(eq("owner"), eq("good-name"))).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/check-name")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("{}"));
	}

	// 因为新建项目和校验项目名称中的校验逻辑完全相同
	// 所以此处不再重复测试所有逻辑，而是确认其中包含测试逻辑即可。
	@WithMockUser(username = "owner")
	@Test
	public void new_project_has_validate_project_name() {
		NewProjectParam param = new NewProjectParam();
		param.setOwner("owner");
		param.setName("good-name");
		
		Project project = new Project();
		project.setId(1);
		project.setName("good-name");

		when(projectService.find(eq("owner"), eq("good-name"))).thenReturn(Optional.of(project));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.name", hasItem("owner下已存在<strong>good-name</strong>项目"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "owner")
	@Test
	public void new_project_success() {
		NewProjectParam param = new NewProjectParam();
		param.setOwner("owner");
		param.setName("good-name");
		param.setIsPublic(true);
		param.setDescription("description");

		when(projectService.find(eq("owner"), eq("good-name"))).thenReturn(Optional.empty());
		
		UserInfo user = new UserInfo();
		user.setId(1);
		user.setLoginName("owner");
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		Project savedProject = new Project();
		savedProject.setId(1);
		savedProject.setName("good-name");
		when(projectService.create(any(), any())).thenReturn(savedProject);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects")
		.then()
			.statusCode(HttpStatus.SC_CREATED)
			.body("name", equalTo("good-name"),
					"id", is(notNullValue()));
	}

	@Test
	public void get_readme_project_not_exist() {
		String owner = "owner";
		String projectName = "public-project";
		
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.TEXT)
		.when()
			.get("/projects/{owner}/{projectName}/readme", owner, projectName)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void get_readme_can_not_read_project() {
		String owner = "owner";
		String projectName = "public-project";
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.TEXT)
		.when()
			.get("/projects/{owner}/{projectName}/readme", owner, projectName)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@Test
	public void get_readme_success_no_readme() {
		String owner = "owner";
		String projectName = "public-project";
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		when(projectFileService.findReadme(anyInt())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.TEXT)
		.when()
			.get("/projects/{owner}/{projectName}/readme", owner, projectName)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo(""));
	}
	
	@Test
	public void get_readme_success_has_readme() {
		String owner = "owner";
		String projectName = "public-project";
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		ProjectFile projectFile = new ProjectFile();
		projectFile.setContent("# public-project");
		when(projectFileService.findReadme(anyInt())).thenReturn(Optional.of(projectFile));
		
		given()
			.contentType(ContentType.TEXT)
		.when()
			.get("/projects/{owner}/{projectName}/readme", owner, projectName)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("# public-project"));
	}

	@Test
	public void get_your_projects_anonymous_can_not_get() {
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/user/projects")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("owner")
	@Test
	public void get_your_projects_user_not_exist() {
		when(userService.findByLoginName(anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/user/projects")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("owner")
	@Test
	public void get_your_projects_success() {
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		when(projectService.findCanAccessProjectsByUserId(anyInt())).thenReturn(Collections.emptyList());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/user/projects")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", is(0));
	}
	
	@Test
	public void get_project_not_exist() {
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{project}", "zhangsan", "my-project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void get_project_can_not_read_project() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{project}", "jack", "my-project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "other")
	@Test
	public void get_project_success() {
		Project project = new Project();
		project.setId(1);
		project.setName("my-project");
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		when(projectPermissionService.findTopestPermission(any(), any())).thenReturn(AccessLevel.WRITE);
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{project}", "jack", "my-project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("name", equalTo("my-project"),
					"accessLevel", equalTo(AccessLevel.WRITE.getKey()));
	}
	
	@Test
	public void get_latest_commit_invalid_parentId() {
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{project}/latest-commit/{pathId}", "zhangsan", "my-project", "a-invalid-parent-id")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void get_latest_commit_project_not_exist() {
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{project}/latest-commit/{pathId}", "zhangsan", "my-project", "-1")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void get_latest_commit_can_not_read_project() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{project}/latest-commit/{pathId}", "zhangsan", "my-project", "-1")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@Test
	public void get_latest_commit_at_root() {
		Project project = new Project();
		project.setName("my-project");
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		when(projectResourceService.findParentPathes(anyInt())).thenReturn(Collections.emptyList());
		
		GitCommitInfo commitInfo = new GitCommitInfo();
		commitInfo.setShortMessage("message");
		when(projectService.findLatestCommitInfo(any(), anyString())).thenReturn(Optional.of(commitInfo));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{project}/latest-commit/{pathId}", "zhangsan", "my-project", "-1")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("shortMessage", equalTo("message"));
	}
	
	@Test
	public void get_latest_commit_info_at_sub_folder() {
		Project project = new Project();
		project.setName("my-project");
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		when(projectResourceService.findParentPathes(anyInt())).thenReturn(Collections.singletonList("a"));
		
		GitCommitInfo commitInfo = new GitCommitInfo();
		commitInfo.setShortMessage("message");
		when(projectService.findLatestCommitInfo(any(), anyString())).thenReturn(Optional.of(commitInfo));
		
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
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
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
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.empty());
		
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
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
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
