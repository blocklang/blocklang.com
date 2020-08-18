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

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.constant.Constant;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.RepositoryResourceType;
import com.blocklang.develop.data.CheckGroupKeyParam;
import com.blocklang.develop.data.CheckGroupNameParam;
import com.blocklang.develop.data.NewGroupParam;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.RepositoryPermissionService;
import com.blocklang.develop.service.RepositoryResourceService;
import com.blocklang.develop.service.RepositoryService;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ApiWidget;
import com.blocklang.marketplace.service.ApiRepoService;
import com.blocklang.marketplace.service.ApiRepoVersionService;
import com.blocklang.marketplace.service.ApiWidgetService;

import io.restassured.http.ContentType;

@WebMvcTest(GroupController.class)
public class GroupControllerTest extends AbstractControllerTest{

	@MockBean
	private RepositoryService repositoryService;
	@MockBean
	private RepositoryResourceService repositoryResourceService;
	@MockBean
	private RepositoryPermissionService repositoryPermissionService;
	@MockBean
	private ApiRepoService apiRepoService;
	@MockBean
	private ApiRepoVersionService apiRepoVersionService;
	@MockBean
	private ApiWidgetService apiWidgetService;
	
	@Test
	public void checkKeyAnonymousCanNotCheck() {
		CheckGroupKeyParam param = new CheckGroupKeyParam();
		param.setKey("key");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups/check-key", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void checkKeyUserLoginButRepoNotExist() {
		CheckGroupKeyParam param = new CheckGroupKeyParam();
		param.setKey("key");
		
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups/check-key", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void checkKeyLoginUserCanNotWriteRepo() {
		CheckGroupKeyParam param = new CheckGroupKeyParam();
		param.setKey("key");
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups/check-key", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void checkKeyIsBlank() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		CheckGroupKeyParam param = new CheckGroupKeyParam();
		param.setKey(" ");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups/check-key", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("名称不能为空"),
					"errors.key.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void checkKeyIsInvalid() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		CheckGroupKeyParam param = new CheckGroupKeyParam();
		param.setKey("中文");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups/check-key", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("只允许字母、数字、中划线(-)、下划线(_)"),
					"errors.key.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void checkKeyIsUsedAtRoot() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		RepositoryResource resource = new RepositoryResource();
		when(repositoryResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		when(repositoryResourceService.findById(anyInt())).thenReturn(Optional.of(new RepositoryResource()));
		
		CheckGroupKeyParam param = new CheckGroupKeyParam();
		param.setKey("a-used-key");
		param.setParentId(Constant.TREE_ROOT_ID);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups/check-key", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("根目录下已存在名称<strong>a-used-key</strong>"),
					"errors.key.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void checkKeyIsUsedAtSub() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		Integer groupId = 1;
		RepositoryResource resource = new RepositoryResource();
		resource.setParentId(groupId);
		when(repositoryResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		RepositoryResource parentResource = new RepositoryResource();
		parentResource.setId(groupId);
		parentResource.setName("二级目录");
		when(repositoryResourceService.findById(anyInt())).thenReturn(Optional.of(parentResource));
		
		CheckGroupKeyParam param = new CheckGroupKeyParam();
		param.setKey("a-used-key");
		param.setParentId(groupId);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups/check-key", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("二级目录下已存在名称<strong>a-used-key</strong>"),
					"errors.key.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void checkKeyIsPassed() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		when(repositoryResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		
		CheckGroupKeyParam param = new CheckGroupKeyParam();
		param.setKey("key");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups/check-key", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("{}"));
	}
	
	@Test
	public void checkNameAnonymousCanNotCheck() {
		CheckGroupNameParam param = new CheckGroupNameParam();
		param.setName("name");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups/check-name", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}

	@WithMockUser(username = "jack")
	@Test
	public void checkNameUserLoginButRepoNotExist() {
		CheckGroupNameParam param = new CheckGroupNameParam();
		param.setName("name");
		
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups/check-name", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}

	@WithMockUser(username = "jack")
	@Test
	public void checkNameLoginUserCanNotWriteRepo() {
		CheckGroupNameParam param = new CheckGroupNameParam();
		param.setName("name");
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups/check-name", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}

	@WithMockUser(username = "jack")
	@Test
	public void checkNameCanBeNull() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		RepositoryResource resource = new RepositoryResource();
		when(repositoryResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		when(repositoryResourceService.findById(anyInt())).thenReturn(Optional.of(new RepositoryResource()));
		
		CheckGroupNameParam param = new CheckGroupNameParam();
		param.setName(null);
		param.setParentId(Constant.TREE_ROOT_ID);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups/check-name", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_OK);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void checkNameIsUsedAtRoot() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		RepositoryResource resource = new RepositoryResource();
		when(repositoryResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		when(repositoryResourceService.findById(anyInt())).thenReturn(Optional.of(new RepositoryResource()));
		
		CheckGroupNameParam param = new CheckGroupNameParam();
		param.setName("a-used-name");
		param.setParentId(Constant.TREE_ROOT_ID);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups/check-name", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.name", hasItem("根目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void checkNameIsUsedAtSub() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		Integer groupId = 1;
		RepositoryResource resource = new RepositoryResource();
		resource.setParentId(groupId);
		when(repositoryResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		RepositoryResource parentResource = new RepositoryResource();
		parentResource.setId(groupId);
		parentResource.setName("二级目录");
		when(repositoryResourceService.findById(anyInt())).thenReturn(Optional.of(parentResource));
		
		CheckGroupNameParam param = new CheckGroupNameParam();
		param.setName("a-used-name");
		param.setParentId(groupId);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups/check-name", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.name", hasItem("二级目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void checkNameIsPassed() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		when(repositoryResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		
		CheckGroupNameParam param = new CheckGroupNameParam();
		param.setName("name");
		param.setParentId(Constant.TREE_ROOT_ID);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups/check-name", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("{}"));
	}
	
	@Test
	public void newGroupAnonymousCanNotNew() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setKey("key");
		param.setName("name");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void newGroupUserLoginButRepoNotExist() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setKey("key");
		param.setName("name");
		
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void newGroupLoginUserCanNotWriteRepo() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setKey("key");
		param.setName("name");
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}

	@WithMockUser(username = "jack")
	@Test
	public void newGroupCheckKeyIsNullAndNameIsNull() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setKey(null);
		param.setName(null);
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		when(repositoryResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("名称不能为空"),
					"errors.key.size()", is(1),
					"errors.name", is(nullValue()));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void newGroupCheckKeyIsBlankAndNamePassed() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setKey(" ");
		param.setName("name");
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		when(repositoryResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("名称不能为空"),
					"errors.key.size()", is(1),
					"errors.name", is(nullValue()));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void newGroupCheckKeyIsBlankAndNameIsUsed() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setKey(" ");
		param.setName("a-used-name");
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		RepositoryResource resource = new RepositoryResource();
		when(repositoryResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		when(repositoryResourceService.findById(anyInt())).thenReturn(Optional.of(new RepositoryResource()));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("名称不能为空"),
					"errors.key.size()", is(1),
					"errors.name", hasItem("根目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void newGroupCheckKeyIsInvalidAndNameIsPassed() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setKey("中文");
		param.setName("name");
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		when(repositoryResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("只允许字母、数字、中划线(-)、下划线(_)"),
					"errors.key.size()", is(1),
					"errors.name", is(nullValue()));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void newGroupCheckKeyIsInvalidAndNameIsUsed() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setKey("中文");
		param.setName("a-used-name");
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		RepositoryResource resource = new RepositoryResource();
		when(repositoryResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		when(repositoryResourceService.findById(anyInt())).thenReturn(Optional.of(new RepositoryResource()));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("只允许字母、数字、中划线(-)、下划线(_)"),
					"errors.key.size()", is(1),
					"errors.name", hasItem("根目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void newGroupCheckKeyIsUsedAndNameIsUsed() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setKey("a-used-key");
		param.setName("a-used-name");
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		RepositoryResource resource1 = new RepositoryResource();
		when(repositoryResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource1));
		
		RepositoryResource resource2 = new RepositoryResource();
		when(repositoryResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource2));
		
		when(repositoryResourceService.findById(anyInt())).thenReturn(Optional.of(new RepositoryResource()));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("根目录下已存在名称<strong>a-used-key</strong>"),
					"errors.key.size()", is(1),
					"errors.name", hasItem("根目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void newGroupCheckKeyIsUsedAndNameIsPass() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setKey("a-used-key");
		param.setName("name");
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		RepositoryResource resource1 = new RepositoryResource();
		when(repositoryResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource1));
		
		when(repositoryResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		
		when(repositoryResourceService.findById(anyInt())).thenReturn(Optional.of(new RepositoryResource()));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("根目录下已存在名称<strong>a-used-key</strong>"),
					"errors.key.size()", is(1),
					"errors.name", is(nullValue()));
	}

	@WithMockUser(username = "jack")
	@Test
	public void newGroupSuccess() {
		String resourceType = RepositoryResourceType.GROUP.getKey();
		String appType = AppType.WEB.getKey();
		
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setKey("key");
		param.setName("name");
		param.setResourceType(resourceType);
		param.setAppType(appType);
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		when(repositoryResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		when(repositoryResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		
		UserInfo currentUser = new UserInfo();
		currentUser.setLoginName("jack");
		currentUser.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(currentUser));
		
		RepositoryResource savedResource = new RepositoryResource();
		savedResource.setRepositoryId(repository.getId());
		savedResource.setParentId(param.getParentId());
		savedResource.setKey("key");
		savedResource.setName("name");
		savedResource.setId(1);
		savedResource.setSeq(1);
		savedResource.setAppType(AppType.WEB);
		savedResource.setResourceType(RepositoryResourceType.GROUP);
		when(repositoryResourceService.insert(any(), any())).thenReturn(savedResource);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_CREATED)
			.body("key", equalTo("key"),
				  "name", equalTo("name"),
				  "id", is(notNullValue()),
				  "resourceType", equalTo(RepositoryResourceType.GROUP.getKey()),
				  "appType", equalTo(AppType.WEB.getKey()));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void newProjectSuccess() {
		NewGroupParam param = new NewGroupParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setKey("key");
		param.setName("name");
		param.setResourceType(RepositoryResourceType.PROJECT.getKey());
		param.setAppType(AppType.MINI_PROGRAM.getKey());
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		when(repositoryResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		when(repositoryResourceService.findByName(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		
		UserInfo currentUser = new UserInfo();
		currentUser.setLoginName("jack");
		currentUser.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(currentUser));

		String gitUrl = "git url";
		Integer userId = -1;
		when(propertyService.findStringValue(eq(CmPropKey.STD_MINI_PROGRAM_COMPONENT_API_GIT_URL))).thenReturn(Optional.of(gitUrl));
		when(propertyService.findIntegerValue(eq(CmPropKey.STD_REPO_REGISTER_USER_ID))).thenReturn(Optional.of(userId));
		
		Integer apiRepoId = 1;
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setId(apiRepoId);
		when(apiRepoService.findByGitUrlAndCreateUserId(eq(gitUrl), eq(userId))).thenReturn(Optional.of(apiRepo));
		
		Integer apiRepoVersionId = 2;
		ApiRepoVersion apiRepoVersion = new ApiRepoVersion();
		apiRepoVersion.setApiRepoId(apiRepoId);
		apiRepoVersion.setId(apiRepoVersionId);
		when(apiRepoVersionService.findMasterVersion(eq(apiRepoId))).thenReturn(Optional.of(apiRepoVersion));
		
		String appWidgetName = "app";
		String pageWidgetName = "page";
		when(propertyService.findStringValue(eq(CmPropKey.STD_MINI_PROGRAM_COMPONENT_APP_NAME))).thenReturn(Optional.of(appWidgetName));
		
		ApiWidget appWidget = new ApiWidget();
		when(apiWidgetService.findByApiRepoVersionIdAndNameIgnoreCase(apiRepoVersionId, appWidgetName)).thenReturn(Optional.of(appWidget));
		
		when(propertyService.findStringValue(eq(CmPropKey.STD_MINI_PROGRAM_COMPONENT_PAGE_NAME))).thenReturn(Optional.of(pageWidgetName));
		
		ApiWidget pageWidget = new ApiWidget();
		when(apiWidgetService.findByApiRepoVersionIdAndNameIgnoreCase(apiRepoVersionId, pageWidgetName)).thenReturn(Optional.of(pageWidget));
		
		RepositoryResource savedResource = new RepositoryResource();
		savedResource.setRepositoryId(repository.getId());
		savedResource.setParentId(param.getParentId());
		savedResource.setKey("key");
		savedResource.setName("name");
		savedResource.setId(1);
		savedResource.setSeq(1);
		savedResource.setAppType(AppType.MINI_PROGRAM);
		savedResource.setResourceType(RepositoryResourceType.PROJECT);
		when(repositoryResourceService.createMiniProgram(eq(repository), any(), eq(apiRepo), eq(appWidget), eq(pageWidget))).thenReturn(savedResource);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/groups", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_CREATED)
			.body("key", equalTo("key"),
				  "name", equalTo("name"),
				  "id", is(notNullValue()),
				  "resourceType", equalTo(RepositoryResourceType.PROJECT.getKey()),
				  "appType", equalTo(AppType.MINI_PROGRAM.getKey()));
	}

	@Test
	public void getGroupTreeRepoNotExist() {
		String owner = "owner";
		String repoName = "public-repository";

		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/groups", owner, repoName)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void getGroupTreeCanNotReadRepository() {
		String owner = "owner";
		String repositoryName = "public-repository";

		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.empty());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/groups/a", owner, repositoryName)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	// 从根节点开始查找
	@Test
	public void getGroupTreeFromRoot() {
		String owner = "owner";
		String repositoryName = "public-repository";

		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));

		List<RepositoryResource> resources = new ArrayList<RepositoryResource>();
		when(repositoryResourceService.findChildren(any(), anyInt())).thenReturn(resources);

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/groups", owner, repositoryName)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("id", equalTo(-1),
					"parentGroups.size()", equalTo(0),
					"childResources.size()", equalTo(0));
	}
	
	@Test
	public void getGroupTreeFromSubGroupParentGroupCanNotBeEmpty() {
		String owner = "owner";
		String repositoryName = "public-repository";

		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		when(repositoryResourceService.findParentGroupsByParentPath(1, "a")).thenReturn(Collections.emptyList());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/groups/a", owner, repositoryName)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void getGroupTreeFromSubGroupSuccess() {
		String owner = "owner";
		String repositoryName = "public-repository";

		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		Integer groupAId = 2;
		RepositoryResource groupA = new RepositoryResource();
		groupA.setId(groupAId);
		groupA.setKey("a");
		when(repositoryResourceService.findParentGroupsByParentPath(1, "a")).thenReturn(Collections.singletonList(groupA));

		RepositoryResource child1 = new RepositoryResource();
		child1.setId(11);
		child1.setKey("child_1");
		when(repositoryResourceService.findChildren(any(), anyInt())).thenReturn(Collections.singletonList(child1));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/groups/a", owner, repositoryName)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("id", equalTo(groupAId),
					"parentGroups.size()", equalTo(1),
					"parentGroups[0].name", equalTo("a"),
					"parentGroups[0].path", equalTo("/a"),
					"childResources.size()", equalTo(1));
	}
	
	@Test
	public void getGroupPathRepoNotExist() {
		String owner = "owner";
		String repositoryName = "public-repository";
		
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/group-path", owner, repositoryName)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void getGroupPathCanNotReadRepo() {
		String owner = "owner";
		String repositoryName = "public-repository";
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.empty());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/group-path", owner, repositoryName)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@Test
	public void getGroupPathFromRoot() {
		String owner = "owner";
		String repositoryName = "public-repository";
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/group-path", owner, repositoryName)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("id", equalTo(-1),
					"parentGroups.size()", equalTo(0));
	}
	
	@Test
	public void getGroupPathFromSubGroupParentGroupCanNotBeEmpty() {
		String owner = "owner";
		String repositoryName = "public-repository";

		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		when(repositoryResourceService.findParentGroupsByParentPath(1, "a")).thenReturn(Collections.emptyList());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/group-path/a", owner, repositoryName)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}

	@Test
	public void getGroupPathFromSubGroup() {
		String owner = "owner";
		String repositoryName = "public-repository";
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		Integer groupAId = 2;
		RepositoryResource groupA = new RepositoryResource();
		groupA.setId(groupAId);
		groupA.setKey("a");
		when(repositoryResourceService.findParentGroupsByParentPath(anyInt(), any())).thenReturn(Collections.singletonList(groupA));

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/group-path/a", owner, repositoryName)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("id", equalTo(groupAId),
					"parentGroups.size()", equalTo(1),
					"parentGroups[0].name", equalTo("a"),
					"parentGroups[0].path", equalTo("/a"));
	}
}
