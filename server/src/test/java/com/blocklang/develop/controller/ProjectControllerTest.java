package com.blocklang.develop.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.blocklang.core.service.GithubLoginService;
import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.develop.data.CheckProjectNameParam;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.service.ProjectService;

import io.restassured.http.ContentType;

@WebMvcTest(ProjectController.class)
public class ProjectControllerTest extends AbstractControllerTest{
	
	@MockBean
	private GithubLoginService githubLoginService;
	
	@MockBean
	private ProjectService projectService;

	// 注意，这里的用户名是必填的，且必须是当前登录用户
	@Test
	public void check_name_is_blank() {
		CheckProjectNameParam param = new CheckProjectNameParam();
		param.setOwner("owner");
		param.setValue(" ");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/check-name")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.value", hasItem("项目名称不能为空"),
					"errors.value.size()", is(1));
	}
	
	@Test
	public void check_name_is_invalid() {
		CheckProjectNameParam param = new CheckProjectNameParam();
		param.setOwner("owner");
		param.setValue("中文");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/check-name")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.value", hasItem("只允许字母、数字、中划线(-)、下划线(_)、点(.)"),
					"errors.value.size()", is(1));
	}
	
	@Test
	public void check_name_is_used() {
		CheckProjectNameParam param = new CheckProjectNameParam();
		param.setOwner("owner");
		param.setValue("good-name");
		
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
			.body("errors.value", hasItem("owner下已存在<strong>good-name</strong>项目"),
					"errors.value.size()", is(1));
	}
	
	@Test
	public void check_name_pass() {
		CheckProjectNameParam param = new CheckProjectNameParam();
		param.setOwner("owner");
		param.setValue("good-name");

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
}
