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
import com.blocklang.develop.data.CheckGroupKeyParam;
import com.blocklang.develop.data.CheckGroupNameParam;
import com.blocklang.develop.data.NewGroupParam;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectPermissionService;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.develop.service.ProjectService;
import com.blocklang.marketplace.service.ApiRepoService;
import com.blocklang.marketplace.service.ApiRepoVersionService;
import com.blocklang.marketplace.service.ApiWidgetService;

import io.restassured.http.ContentType;

@WebMvcTest(GroupController.class)
public class GroupControllerTest extends AbstractControllerTest{

	@MockBean
	private ProjectService projectService;
	@MockBean
	private ProjectResourceService projectResourceService;
	@MockBean
	private ProjectPermissionService projectPermissionService;
	@MockBean
	private ApiRepoService apiRepoService;
	@MockBean
	private ApiRepoVersionService apiRepoVersionService;
	@MockBean
	private ApiWidgetService apiWidgetService;
	
	@Test
	public void check_key_anonymous_can_not_check() {
		CheckGroupKeyParam param = new CheckGroupKeyParam();
		param.setKey("key");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/groups/check-key", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void check_key_user_login_but_project_not_exist() {
		CheckGroupKeyParam param = new CheckGroupKeyParam();
		param.setKey("key");
		
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/groups/check-key", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void check_key_login_user_can_not_write_project() {
		CheckGroupKeyParam param = new CheckGroupKeyParam();
		param.setKey("key");
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/groups/check-key", "jack", "project")
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
		
		CheckGroupKeyParam param = new CheckGroupKeyParam();
		param.setKey(" ");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/groups/check-key", "jack", "project")
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
		
		CheckGroupKeyParam param = new CheckGroupKeyParam();
		param.setKey("中文");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/groups/check-key", "jack", "project")
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
		
		CheckGroupKeyParam param = new CheckGroupKeyParam();
		param.setKey("a-used-key");
		param.setParentId(Constant.TREE_ROOT_ID);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/groups/check-key", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("根目录下已存在名称<strong>a-used-key</strong>"),
					"errors.key.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void check_key_is_used_at_sub() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		Integer groupId = 1;
		ProjectResource resource = new ProjectResource();
		resource.setParentId(groupId);
		when(projectResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		ProjectResource parentResource = new ProjectResource();
		parentResource.setId(groupId);
		parentResource.setName("二级目录");
		when(projectResourceService.findById(anyInt())).thenReturn(Optional.of(parentResource));
		
		CheckGroupKeyParam param = new CheckGroupKeyParam();
		param.setKey("a-used-key");
		param.setParentId(groupId);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/groups/check-key", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("二级目录下已存在名称<strong>a-used-key</strong>"),
					"errors.key.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void check_key_is_passed() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		when(projectResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		
		CheckGroupKeyParam param = new CheckGroupKeyParam();
		param.setKey("key");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/groups/check-key", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("{}"));
	}
	
	@Test
	public void check_name_anonymous_can_not_check() {
		CheckGroupNameParam param = new CheckGroupNameParam();
		param.setName("name");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/groups/check-name", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}

	@WithMockUser(username = "jack")
	@Test
	public void check_name_user_login_but_project_not_exist() {
		CheckGroupNameParam param = new CheckGroupNameParam();
		param.setName("name");
		
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/groups/check-name", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}

	@WithMockUser(username = "jack")
	@Test
	public void check_name_login_user_can_not_write_project() {
		CheckGroupNameParam param = new CheckGroupNameParam();
		param.setName("name");
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/groups/check-name", "jack", "project")
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
		
		CheckGroupNameParam param = new CheckGroupNameParam();
		param.setName(null);
		param.setParentId(Constant.TREE_ROOT_ID);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/groups/check-name", "jack", "project")
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
		
		CheckGroupNameParam param = new CheckGroupNameParam();
		param.setName("a-used-name");
		param.setParentId(Constant.TREE_ROOT_ID);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/groups/check-name", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.name", hasItem("根目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void check_name_is_used_at_sub() {
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
		
		CheckGroupNameParam param = new CheckGroupNameParam();
		param.setName("a-used-name");
		param.setParentId(groupId);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/groups/check-name", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.name", hasItem("二级目录下已存在备注<strong>a-used-name</strong>"),
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
		
		CheckGroupNameParam param = new CheckGroupNameParam();
		param.setName("name");
		param.setParentId(Constant.TREE_ROOT_ID);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/groups/check-name", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("{}"));
	}
	
	@Test
	public void new_group_anonymous_can_not_new() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setKey("key");
		param.setName("name");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/groups", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void new_group_user_login_but_project_not_exist() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setKey("key");
		param.setName("name");
		
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/groups", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void new_group_login_user_can_not_write_project() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
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
			.post("/projects/{owner}/{projectName}/groups", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}

	@WithMockUser(username = "jack")
	@Test
	public void new_group_check_key_is_null_and_name_is_null() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
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
			.post("/projects/{owner}/{projectName}/groups", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("名称不能为空"),
					"errors.key.size()", is(1),
					"errors.name", is(nullValue()));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void new_group_check_key_is_blank_and_name_passed() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
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
			.post("/projects/{owner}/{projectName}/groups", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("名称不能为空"),
					"errors.key.size()", is(1),
					"errors.name", is(nullValue()));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void new_group_check_key_is_blank_and_name_is_used() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
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
			.post("/projects/{owner}/{projectName}/groups", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("名称不能为空"),
					"errors.key.size()", is(1),
					"errors.name", hasItem("根目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void new_group_check_key_is_invalid_and_name_is_passed() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
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
			.post("/projects/{owner}/{projectName}/groups", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("只允许字母、数字、中划线(-)、下划线(_)"),
					"errors.key.size()", is(1),
					"errors.name", is(nullValue()));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void new_group_check_key_is_invalid_and_name_is_used() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
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
			.post("/projects/{owner}/{projectName}/groups", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("只允许字母、数字、中划线(-)、下划线(_)"),
					"errors.key.size()", is(1),
					"errors.name", hasItem("根目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void new_group_check_key_is_used_and_name_is_used() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
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
			.post("/projects/{owner}/{projectName}/groups", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("根目录下已存在名称<strong>a-used-key</strong>"),
					"errors.key.size()", is(1),
					"errors.name", hasItem("根目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void new_group_check_key_is_used_and_name_is_pass() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
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
			.post("/projects/{owner}/{projectName}/groups", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("根目录下已存在名称<strong>a-used-key</strong>"),
					"errors.key.size()", is(1),
					"errors.name", is(nullValue()));
	}
	
	// 校验都通过后，才保存。
	@WithMockUser(username = "jack")
	@Test
	public void new_group_success() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setKey("key");
		param.setName("name");
		param.setResourceType(ProjectResourceType.GROUP.getKey());
		
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
		savedResource.setResourceType(ProjectResourceType.GROUP);
		when(projectResourceService.insert(any(), any())).thenReturn(savedResource);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/groups", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_CREATED)
			.body("key", equalTo("key"),
				  "name", equalTo("name"),
				  "id", is(notNullValue()));
	}

	@Test
	public void get_group_tree_project_not_exist() {
		String owner = "owner";
		String projectName = "public-project";

		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/groups", owner, projectName)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void get_group_tree_can_not_read_project() {
		String owner = "owner";
		String projectName = "public-project";

		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.empty());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/groups/a", owner, projectName)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	// 从根节点开始查找
	@Test
	public void get_group_tree_from_root() {
		String owner = "owner";
		String projectName = "public-project";

		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));

		List<ProjectResource> resources = new ArrayList<ProjectResource>();
		when(projectResourceService.findChildren(any(), anyInt())).thenReturn(resources);

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/groups", owner, projectName)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("id", equalTo(-1),
					"parentGroups.size()", equalTo(0),
					"childResources.size()", equalTo(0));
	}
	
	@Test
	public void get_group_tree_from_sub_group_parent_group_can_not_be_empty() {
		String owner = "owner";
		String projectName = "public-project";

		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		when(projectResourceService.findParentGroupsByParentPath(1, "a")).thenReturn(Collections.emptyList());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/groups/a", owner, projectName)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void get_group_tree_from_sub_group_success() {
		String owner = "owner";
		String projectName = "public-project";

		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		Integer groupAId = 2;
		ProjectResource groupA = new ProjectResource();
		groupA.setId(groupAId);
		groupA.setKey("a");
		when(projectResourceService.findParentGroupsByParentPath(1, "a")).thenReturn(Collections.singletonList(groupA));

		ProjectResource child1 = new ProjectResource();
		child1.setId(11);
		child1.setKey("child_1");
		when(projectResourceService.findChildren(any(), anyInt())).thenReturn(Collections.singletonList(child1));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/groups/a", owner, projectName)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("id", equalTo(groupAId),
					"parentGroups.size()", equalTo(1),
					"parentGroups[0].name", equalTo("a"),
					"parentGroups[0].path", equalTo("/a"),
					"childResources.size()", equalTo(1));
	}
	
	@Test
	public void get_group_path_project_not_exist() {
		String owner = "owner";
		String projectName = "public-project";
		
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/group-path", owner, projectName)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void get_group_path_can_not_read_project() {
		String owner = "owner";
		String projectName = "public-project";
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.empty());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/group-path", owner, projectName)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@Test
	public void get_group_path_from_root() {
		String owner = "owner";
		String projectName = "public-project";
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/group-path", owner, projectName)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("id", equalTo(-1),
					"parentGroups.size()", equalTo(0));
	}
	
	@Test
	public void get_group_path_from_sub_group_parent_group_can_not_be_empty() {
		String owner = "owner";
		String projectName = "public-project";

		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		when(projectResourceService.findParentGroupsByParentPath(1, "a")).thenReturn(Collections.emptyList());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/group-path/a", owner, projectName)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}

	@Test
	public void get_group_path_from_sub_group() {
		String owner = "owner";
		String projectName = "public-project";
		
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		Integer groupAId = 2;
		ProjectResource groupA = new ProjectResource();
		groupA.setId(groupAId);
		groupA.setKey("a");
		when(projectResourceService.findParentGroupsByParentPath(anyInt(), any())).thenReturn(Collections.singletonList(groupA));

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/group-path/a", owner, projectName)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("id", equalTo(groupAId),
					"parentGroups.size()", equalTo(1),
					"parentGroups[0].name", equalTo("a"),
					"parentGroups[0].path", equalTo("/a"));
	}
}
