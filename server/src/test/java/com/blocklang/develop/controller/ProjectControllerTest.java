package com.blocklang.develop.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.GithubLoginService;
import com.blocklang.core.service.UserService;
import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.develop.data.CheckProjectNameParam;
import com.blocklang.develop.data.NewProjectParam;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.service.ProjectService;

import io.restassured.http.ContentType;

@WebMvcTest(ProjectController.class)
public class ProjectControllerTest extends AbstractControllerTest{
	
	@MockBean
	private GithubLoginService githubLoginService;
	
	@MockBean
	private ProjectService projectService;
	
	@MockBean
	private UserService userService;

	@Test
	public void check_name_user_is_unauthorization_not_login() {
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
			.body(equalTo(""));
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
	
}
