package com.blocklang.develop.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.data.CheckPageKeyParam;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectAuthorization;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectAuthorizationService;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.develop.service.ProjectService;

import io.restassured.http.ContentType;

@WebMvcTest(PageController.class)
public class PageControllerTest extends AbstractControllerTest{
	
	@MockBean
	private ProjectService projectService;
	@MockBean
	private ProjectAuthorizationService projectAuthorizationService;
	@MockBean
	private ProjectResourceService projectResourceService;

	@Test
	public void check_key_user_is_unauthorization_not_login() {
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("key");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages/check-key", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void check_key_user_login_but_project_not_exist() {
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("key");
		
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages/check-key", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}
	
	// 用户无权访问的公开项目
	// 没有为用户配置该项目的任何权限
	@WithMockUser(username = "jack")
	@Test
	public void check_key_user_can_not_read_public_project() {
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("key");
		
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(true); // 公开项目
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.emptyList());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages/check-key", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	// 用户对公开项目只有 read 权限
	@WithMockUser(username = "jack")
	@Test
	public void check_key_user_can_read_public_project() {
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("key");
		
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(true); // 公开项目
		project.setCreateUserId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.READ);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages/check-key", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void check_key_is_blank() {
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(true); // 公开项目
		project.setCreateUserId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.WRITE);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey(" ");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages/check-key", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("名称不能为空"),
					"errors.key.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void check_key_is_invalid() {
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(true); // 公开项目
		project.setCreateUserId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.WRITE);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("中文");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages/check-key", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("只允许字母、数字、中划线(-)、下划线(_)"),
					"errors.key.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void check_key_is_used() {
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("project");
		project.setIsPublic(true); // 公开项目
		project.setCreateUserId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.WRITE);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		ProjectResource resource = new ProjectResource();
		when(projectResourceService.find(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(new ProjectResource()));
		
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("a-used-key");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages/check-key", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("根目录下已存在名称<strong>a-used-key</strong>"),
					"errors.key.size()", is(1));
	}
	
}
