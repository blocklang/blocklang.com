package com.blocklang.develop.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import com.blocklang.core.constant.Constant;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.data.CheckPageKeyParam;
import com.blocklang.develop.data.CheckPageNameParam;
import com.blocklang.develop.data.NewPageParam;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectPermissionService;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.develop.service.ProjectService;

import io.restassured.http.ContentType;

@WebMvcTest(PageController.class)
public class PageControllerTest extends AbstractControllerTest{
	
	@MockBean
	private ProjectService projectService;
	@MockBean
	private ProjectResourceService projectResourceService;
	@MockBean
	private ProjectPermissionService projectPermissionService;

	@Test
	public void check_key_anonymous_can_not_check() {
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
	
	@WithMockUser(username = "jack")
	@Test
	public void check_key_user_can_not_write_project() {
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("key");
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());
		
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
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
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
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
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
	public void check_key_is_used_at_root() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		ProjectResource resource = new ProjectResource();
		when(projectResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(new ProjectResource()));
		
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("a-used-key");
		param.setParentId(Constant.TREE_ROOT_ID);
		
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
	
	@WithMockUser(username = "jack")
	@Test
	public void check_key_is_used_at_sub_and_name_is_not_blank() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		Integer parentId = 1;
		ProjectResource resource = new ProjectResource();
		resource.setParentId(parentId);
		when(projectResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		ProjectResource parentResource = new ProjectResource();
		parentResource.setId(parentId);
		parentResource.setName("二级目录");
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(parentResource));
		
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("a-used-key");
		param.setParentId(parentId);
		param.setAppType(AppType.WEB.getKey());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages/check-key", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("二级目录下已存在名称<strong>a-used-key</strong>"),
					"errors.key.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void check_key_is_used_at_sub_and_name_is_blank() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		Integer parentId = 1;
		ProjectResource resource = new ProjectResource();
		resource.setParentId(parentId);
		when(projectResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		ProjectResource parentResource = new ProjectResource();
		parentResource.setId(parentId);
		parentResource.setKey("two level");
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(parentResource));
		
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("a-used-key");
		param.setParentId(parentId);
		param.setAppType(AppType.WEB.getKey());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages/check-key", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("two level下已存在名称<strong>a-used-key</strong>"),
					"errors.key.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void check_key_from_root_is_passed() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		when(projectResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("key");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages/check-key", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("{}"));
	}
	
	@Test
	public void check_name_anonymous_can_not_check() {
		CheckPageNameParam param = new CheckPageNameParam();
		param.setName("name");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages/check-name", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}

	@WithMockUser(username = "jack")
	@Test
	public void check_name_user_login_but_project_not_exist() {
		CheckPageNameParam param = new CheckPageNameParam();
		param.setName("name");
		
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages/check-name", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}

	@WithMockUser(username = "jack")
	@Test
	public void check_name_can_not_write_project() {
		CheckPageNameParam param = new CheckPageNameParam();
		param.setName("name");
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages/check-name", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}

	@WithMockUser(username = "jack")
	@Test
	public void check_name_can_be_null() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		ProjectResource resource = new ProjectResource();
		when(projectResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(new ProjectResource()));
		
		CheckPageNameParam param = new CheckPageNameParam();
		param.setName(null);
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages/check-name", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void check_name_is_used_at_root() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		ProjectResource resource = new ProjectResource();
		when(projectResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(new ProjectResource()));
		
		CheckPageNameParam param = new CheckPageNameParam();
		param.setName("a-used-name");
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages/check-name", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.name", hasItem("根目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void check_name_is_used_at_sub_and_name_is_not_blank() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		Integer groupId = 1;
		ProjectResource resource = new ProjectResource();
		resource.setParentId(groupId);
		when(projectResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		ProjectResource parentResource = new ProjectResource();
		parentResource.setId(groupId);
		parentResource.setName("二级目录");
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(parentResource));
		
		CheckPageNameParam param = new CheckPageNameParam();
		param.setName("a-used-name");
		param.setParentId(groupId);
		param.setAppType(AppType.WEB.getKey());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages/check-name", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.name", hasItem("二级目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void check_name_is_used_at_sub_and_name_is_blank() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		Integer groupId = 1;
		ProjectResource resource = new ProjectResource();
		resource.setParentId(groupId);
		when(projectResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		ProjectResource parentResource = new ProjectResource();
		parentResource.setId(groupId);
		parentResource.setKey("Two Level");
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(parentResource));
		
		CheckPageNameParam param = new CheckPageNameParam();
		param.setName("a-used-name");
		param.setParentId(groupId);
		param.setAppType(AppType.WEB.getKey());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages/check-name", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.name", hasItem("Two Level下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void check_name_is_passed() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		when(projectResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		
		CheckPageNameParam param = new CheckPageNameParam();
		param.setName("name");
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages/check-name", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("{}"));
	}
	
	@Test
	public void new_page_anonymous_can_not_new() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		param.setKey("key");
		param.setName("name");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void new_page_user_login_but_project_not_exist() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		param.setKey("key");
		param.setName("name");
		
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void new_page_user_can_not_write_project() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		param.setKey("key");
		param.setName("name");
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}

	@WithMockUser(username = "jack")
	@Test
	public void new_page_check_key_is_null_and_name_is_null() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		param.setKey(null);
		param.setName(null);
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		when(projectResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("名称不能为空"),
					"errors.key.size()", is(1),
					"errors.name", is(nullValue()));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void new_page_check_key_is_blank_and_name_passed() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		param.setKey(" ");
		param.setName("name");
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		when(projectResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("名称不能为空"),
					"errors.key.size()", is(1),
					"errors.name", is(nullValue()));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void new_page_check_key_is_blank_and_name_is_used() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		param.setKey(" ");
		param.setName("a-used-name");
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		ProjectResource resource = new ProjectResource();
		when(projectResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(new ProjectResource()));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("名称不能为空"),
					"errors.key.size()", is(1),
					"errors.name", hasItem("根目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void new_page_check_key_is_invalid_and_name_is_passed() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		param.setKey("中文");
		param.setName("name");
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		when(projectResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("只允许字母、数字、中划线(-)、下划线(_)"),
					"errors.key.size()", is(1),
					"errors.name", is(nullValue()));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void new_page_check_key_is_invalid_and_name_is_used() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		param.setKey("中文");
		param.setName("a-used-name");
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		ProjectResource resource = new ProjectResource();
		when(projectResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(new ProjectResource()));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("只允许字母、数字、中划线(-)、下划线(_)"),
					"errors.key.size()", is(1),
					"errors.name", hasItem("根目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void new_page_check_key_is_used_and_name_is_used() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		param.setKey("a-used-key");
		param.setName("a-used-name");
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		ProjectResource resource1 = new ProjectResource();
		when(projectResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource1));
		
		ProjectResource resource2 = new ProjectResource();
		when(projectResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource2));
		
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(new ProjectResource()));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("根目录下已存在名称<strong>a-used-key</strong>"),
					"errors.key.size()", is(1),
					"errors.name", hasItem("根目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void new_page_check_key_is_used_and_name_is_pass() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		param.setKey("a-used-key");
		param.setName("name");
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		ProjectResource resource1 = new ProjectResource();
		when(projectResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource1));
		
		when(projectResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(new ProjectResource()));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("根目录下已存在名称<strong>a-used-key</strong>"),
					"errors.key.size()", is(1),
					"errors.name", is(nullValue()));
	}
	
	// 校验都通过后，才保存。
	@WithMockUser(username = "jack")
	@Test
	public void new_page_success() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		param.setKey("key");
		param.setName("name");
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		when(projectResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		when(projectResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		
		UserInfo currentUser = new UserInfo();
		currentUser.setLoginName("jack");
		currentUser.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(currentUser));
		
		ProjectResource savedResource = new ProjectResource();
		savedResource.setProjectId(project.getId());
		savedResource.setParentId(param.getParentId());
		savedResource.setKey("key");
		savedResource.setName("name");
		savedResource.setId(1);
		savedResource.setSeq(1);
		savedResource.setAppType(AppType.WEB);
		savedResource.setResourceType(ProjectResourceType.PAGE);
		when(projectResourceService.insert(any(), any())).thenReturn(savedResource);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/pages", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_CREATED)
			.body("key", equalTo("key"),
				  "name", equalTo("name"),
				  "id", is(notNullValue()));
	}

	@Test
	public void get_page_project_not_found() {
		String owner = "owner";
		String projectName = "not-exist-project";
		
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/pages/{pagePath}", owner, projectName, "a")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void get_page_can_not_read_project() {
		String owner = "owner";
		String projectName = "private-project";
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.empty());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/pages/{pagePath}", owner, projectName, "a")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@Test
	public void get_page_page_not_exist() {
		String owner = "owner";
		String projectName = "project";
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		when(projectResourceService.findParentGroupsByParentPath(anyInt(), anyString())).thenReturn(Collections.emptyList());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/pages/{pagePath}", owner, projectName, "a")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void get_page_at_root_success() {
		String owner = "owner";
		String projectName = "project";
		
		int repoId = 1;
		Project project = new Project();
		project.setId(repoId);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		when(projectResourceService.findParentGroupsByParentPath(anyInt(), anyString())).thenReturn(new ArrayList<>());

		String pageKey = "a";
		ProjectResource projectResource = new ProjectResource();
		projectResource.setId(11);
		projectResource.setKey(pageKey);
		projectResource.setName("A");
		projectResource.setAppType(AppType.UNKNOWN);
		projectResource.setResourceType(ProjectResourceType.PAGE);
		when(projectResourceService.findByKey(eq(repoId), eq(Constant.TREE_ROOT_ID), eq(ProjectResourceType.PAGE), eq(AppType.UNKNOWN), eq(pageKey))).thenReturn(Optional.of(projectResource));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/pages/{pagePath}", owner, projectName, "a")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("projectResource.id", equalTo(11),
					  "parentGroups.size", equalTo(1),
					  "parentGroups[0].name", equalTo("A"),
					  "parentGroups[0].path", equalTo("/a"));
	}
	
	@Test
	public void get_page_at_sub_group_success() {
		String owner = "owner";
		String projectName = "project";
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		ProjectResource groupA = new ProjectResource();
		groupA.setId(1);
		groupA.setKey("groupA");
		List<ProjectResource> parents = new ArrayList<>();
		parents.add(groupA);
		when(projectResourceService.findParentGroupsByParentPath(anyInt(), anyString())).thenReturn(parents);

		ProjectResource projectResource = new ProjectResource();
		projectResource.setId(11);
		projectResource.setKey("page_b");
		projectResource.setName("PAGE_B");
		when(projectResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(projectResource));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/pages/{pagePath}", owner, projectName, "groupA/PAGE_B")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("projectResource.id", equalTo(11),
					  "parentGroups.size", equalTo(2),
					  "parentGroups[0].name", equalTo("groupA"),
					  "parentGroups[0].path", equalTo("/groupA"),
					  "parentGroups[1].name", equalTo("PAGE_B"),
					  "parentGroups[1].path", equalTo("/groupA/page_b"));
	}

	/**
	 * 获取小程序的 app 页面
	 */
	@Test
	public void getPage_mini_program_app() {
		String owner = "owner";
		String repoName = "project";
		int repoId = 1;
		Project repo = new Project();
		repo.setId(repoId);
		when(projectService.find(eq(owner), eq(repoName))).thenReturn(Optional.of(repo));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		int miniProjectId = 11;
		ProjectResource groupA = new ProjectResource();
		groupA.setId(miniProjectId);
		groupA.setKey("miniProgram1");
		groupA.setAppType(AppType.MINI_PROGRAM);
		groupA.setResourceType(ProjectResourceType.PROJECT);
		List<ProjectResource> parents = new ArrayList<>();
		parents.add(groupA);
		when(projectResourceService.findParentGroupsByParentPath(anyInt(), anyString())).thenReturn(parents);

		int appId = 111;
		String mainPageKey = "app";
		ProjectResource projectResource = new ProjectResource();
		projectResource.setId(appId);
		projectResource.setKey(mainPageKey);
		projectResource.setParentId(miniProjectId);
		projectResource.setAppType(AppType.MINI_PROGRAM);
		projectResource.setResourceType(ProjectResourceType.MAIN);
		when(projectResourceService.findByKey(eq(repoId), eq(miniProjectId), eq(ProjectResourceType.MAIN), eq(AppType.MINI_PROGRAM), eq(mainPageKey))).thenReturn(Optional.of(projectResource));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/pages/{pagePath}", owner, repoName, "miniProgram1/app")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("projectResource.id", equalTo(appId),
					  "parentGroups.size", equalTo(2),
					  "parentGroups[0].name", equalTo("miniProgram1"),
					  "parentGroups[0].path", equalTo("/miniProgram1"),
					  "parentGroups[1].name", equalTo("app"),
					  "parentGroups[1].path", equalTo("/miniProgram1/app"));
	}
	
	@Test
	public void getPage_mini_program_index_page() {
		String owner = "owner";
		String repoName = "project";
		int repoId = 1;
		Project repo = new Project();
		repo.setId(repoId);
		when(projectService.find(eq(owner), eq(repoName))).thenReturn(Optional.of(repo));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		int miniProjectId = 11;
		ProjectResource groupA = new ProjectResource();
		groupA.setId(miniProjectId);
		groupA.setKey("miniProgram1");
		groupA.setAppType(AppType.MINI_PROGRAM);
		groupA.setResourceType(ProjectResourceType.PROJECT);
		groupA.setParentId(Constant.TREE_ROOT_ID);
		
		int pagesGroupId = 111;
		ProjectResource groupAA = new ProjectResource();
		groupAA.setId(pagesGroupId);
		groupAA.setKey("pages");
		groupAA.setAppType(AppType.MINI_PROGRAM);
		groupAA.setResourceType(ProjectResourceType.GROUP);
		groupAA.setParentId(miniProjectId);
		
		List<ProjectResource> parents = new ArrayList<>();
		parents.add(groupA);
		parents.add(groupAA);
		when(projectResourceService.findParentGroupsByParentPath(anyInt(), anyString())).thenReturn(parents);
		
		int indexPageId = 1111;
		String indexPageKey = "index";
		ProjectResource indexPage = new ProjectResource();
		indexPage.setId(indexPageId);
		indexPage.setKey(indexPageKey);
		indexPage.setAppType(AppType.MINI_PROGRAM);
		indexPage.setResourceType(ProjectResourceType.MAIN);
		indexPage.setParentId(pagesGroupId);
		when(projectResourceService.findByKey(eq(repoId), eq(pagesGroupId), eq(ProjectResourceType.PAGE), eq(AppType.MINI_PROGRAM), eq(indexPageKey))).thenReturn(Optional.of(indexPage));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/pages/{pagePath}", owner, repoName, "miniProgram1/pages/index")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("projectResource.id", equalTo(indexPageId),
					  "parentGroups.size", equalTo(3),
					  "parentGroups[0].name", equalTo("miniProgram1"),
					  "parentGroups[0].path", equalTo("/miniProgram1"),
					  "parentGroups[1].name", equalTo("pages"),
					  "parentGroups[1].path", equalTo("/miniProgram1/pages"),
					  "parentGroups[2].name", equalTo("index"),
					  "parentGroups[2].path", equalTo("/miniProgram1/pages/index"));
	}
}
