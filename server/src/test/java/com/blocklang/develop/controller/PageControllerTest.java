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
import com.blocklang.develop.constant.RepositoryResourceType;
import com.blocklang.develop.data.CheckPageKeyParam;
import com.blocklang.develop.data.CheckPageNameParam;
import com.blocklang.develop.data.NewPageParam;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.RepositoryPermissionService;
import com.blocklang.develop.service.RepositoryResourceService;
import com.blocklang.develop.service.RepositoryService;

import io.restassured.http.ContentType;

@WebMvcTest(PageController.class)
public class PageControllerTest extends AbstractControllerTest{
	
	@MockBean
	private RepositoryService repositoryService;
	@MockBean
	private RepositoryResourceService repositoryResourceService;
	@MockBean
	private RepositoryPermissionService repositoryPermissionService;

	@Test
	public void checkKeyAnonymousCanNotCheck() {
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("key");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages/check-key", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void checkKeyUserLoginButProjectNotExist() {
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("key");
		
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages/check-key", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void checkKeyUserCanNotWriteRepo() {
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("key");
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages/check-key", "jack", "repo")
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
		
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey(" ");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages/check-key", "jack", "repo")
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
		
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("中文");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages/check-key", "jack", "repo")
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
		
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("a-used-key");
		param.setParentId(Constant.TREE_ROOT_ID);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages/check-key", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("根目录下已存在名称<strong>a-used-key</strong>"),
					"errors.key.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void checkKeyIsUsedAtSubAndNameIsNotBlank() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		Integer parentId = 1;
		RepositoryResource resource = new RepositoryResource();
		resource.setParentId(parentId);
		when(repositoryResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		RepositoryResource parentResource = new RepositoryResource();
		parentResource.setId(parentId);
		parentResource.setName("二级目录");
		when(repositoryResourceService.findById(anyInt())).thenReturn(Optional.of(parentResource));
		
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("a-used-key");
		param.setParentId(parentId);
		param.setAppType(AppType.WEB.getKey());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages/check-key", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("二级目录下已存在名称<strong>a-used-key</strong>"),
					"errors.key.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void checkKeyIsUsedAtSubAndNameIsBlank() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		Integer parentId = 1;
		RepositoryResource resource = new RepositoryResource();
		resource.setParentId(parentId);
		when(repositoryResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		RepositoryResource parentResource = new RepositoryResource();
		parentResource.setId(parentId);
		parentResource.setKey("two level");
		when(repositoryResourceService.findById(anyInt())).thenReturn(Optional.of(parentResource));
		
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("a-used-key");
		param.setParentId(parentId);
		param.setAppType(AppType.WEB.getKey());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages/check-key", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("two level下已存在名称<strong>a-used-key</strong>"),
					"errors.key.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void checkKeyFromRootIsPassed() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		when(repositoryResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.empty());
		
		CheckPageKeyParam param = new CheckPageKeyParam();
		param.setKey("key");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages/check-key", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("{}"));
	}
	
	@Test
	public void checkNameAnonymousCanNotCheck() {
		CheckPageNameParam param = new CheckPageNameParam();
		param.setName("name");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages/check-name", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}

	@WithMockUser(username = "jack")
	@Test
	public void checkNameUserLoginButRepoNotExist() {
		CheckPageNameParam param = new CheckPageNameParam();
		param.setName("name");
		
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages/check-name", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}

	@WithMockUser(username = "jack")
	@Test
	public void checkNameCanNotWriteRepo() {
		CheckPageNameParam param = new CheckPageNameParam();
		param.setName("name");
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages/check-name", "jack", "repo")
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
		
		CheckPageNameParam param = new CheckPageNameParam();
		param.setName(null);
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages/check-name", "jack", "repo")
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
		
		CheckPageNameParam param = new CheckPageNameParam();
		param.setName("a-used-name");
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages/check-name", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.name", hasItem("根目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void checkNameIsUsedAtSubAndNameIsNotBlank() {
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
		
		CheckPageNameParam param = new CheckPageNameParam();
		param.setName("a-used-name");
		param.setParentId(groupId);
		param.setAppType(AppType.WEB.getKey());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages/check-name", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.name", hasItem("二级目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void checkNameIsUsedAtSubAndNameIsBlank() {
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
		parentResource.setKey("Two Level");
		when(repositoryResourceService.findById(anyInt())).thenReturn(Optional.of(parentResource));
		
		CheckPageNameParam param = new CheckPageNameParam();
		param.setName("a-used-name");
		param.setParentId(groupId);
		param.setAppType(AppType.WEB.getKey());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages/check-name", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.name", hasItem("Two Level下已存在备注<strong>a-used-name</strong>"),
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
		
		CheckPageNameParam param = new CheckPageNameParam();
		param.setName("name");
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages/check-name", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("{}"));
	}
	
	@Test
	public void newPageAnonymousCanNotNew() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		param.setKey("key");
		param.setName("name");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void newPageUserLoginButRepoNotExist() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		param.setKey("key");
		param.setName("name");
		
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void newPageUserCanNotWriteRepo() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
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
			.post("/repos/{owner}/{repoName}/pages", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}

	@WithMockUser(username = "jack")
	@Test
	public void newPageCheckKeyIsNullAndNameIsNull() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
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
			.post("/repos/{owner}/{repoName}/pages", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("名称不能为空"),
					"errors.key.size()", is(1),
					"errors.name", is(nullValue()));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void newPageCheckKeyIsBlankAndNamePassed() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
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
			.post("/repos/{owner}/{repoName}/pages", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("名称不能为空"),
					"errors.key.size()", is(1),
					"errors.name", is(nullValue()));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void newPageCheckKeyIsBlankAndNameIsUsed() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
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
			.post("/repos/{owner}/{repoName}/pages", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("名称不能为空"),
					"errors.key.size()", is(1),
					"errors.name", hasItem("根目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void newPageCheckKeyIsInvalidAndNameIsPassed() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
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
			.post("/repos/{owner}/{repoName}/pages", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("只允许字母、数字、中划线(-)、下划线(_)"),
					"errors.key.size()", is(1),
					"errors.name", is(nullValue()));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void newPageCheckKeyIsInvalidAndNameIsUsed() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
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
			.post("/repos/{owner}/{repoName}/pages", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("只允许字母、数字、中划线(-)、下划线(_)"),
					"errors.key.size()", is(1),
					"errors.name", hasItem("根目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void newPageCheckKeyIsUsedAndNameIsUsed() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
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
			.post("/repos/{owner}/{repoName}/pages", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("根目录下已存在名称<strong>a-used-key</strong>"),
					"errors.key.size()", is(1),
					"errors.name", hasItem("根目录下已存在备注<strong>a-used-name</strong>"),
					"errors.name.size()", is(1));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void newPageCheckKeyIsUsedAndNameIsPass() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
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
			.post("/repos/{owner}/{repoName}/pages", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.key", hasItem("根目录下已存在名称<strong>a-used-key</strong>"),
					"errors.key.size()", is(1),
					"errors.name", is(nullValue()));
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void newPageSuccess() {
		NewPageParam param = new NewPageParam();
		param.setParentId(Constant.TREE_ROOT_ID);
		param.setAppType(AppType.WEB.getKey());
		param.setKey("key");
		param.setName("name");
		
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
		savedResource.setResourceType(RepositoryResourceType.PAGE);
		when(repositoryResourceService.insert(any(), any())).thenReturn(savedResource);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/pages", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_CREATED)
			.body("key", equalTo("key"),
				  "name", equalTo("name"),
				  "id", is(notNullValue()));
	}

	@Test
	public void getPageRepoNotFound() {
		String owner = "owner";
		String repositoryName = "not-exist-repository";
		
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/pages/{pagePath}", owner, repositoryName, "a")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void getPageCanNotReadRepo() {
		String owner = "owner";
		String repositoryName = "private-repository";
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.empty());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/pages/{pagePath}", owner, repositoryName, "a")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@Test
	public void getPagePageNotExist() {
		String owner = "owner";
		String repositoryName = "repository";
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		when(repositoryResourceService.findParentGroupsByParentPath(anyInt(), anyString())).thenReturn(Collections.emptyList());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/pages/{pagePath}", owner, repositoryName, "a")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void getPageAtRootSuccess() {
		String owner = "owner";
		String repositoryName = "repo";
		
		int repoId = 1;
		Repository repository = new Repository();
		repository.setId(repoId);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		when(repositoryResourceService.findParentGroupsByParentPath(anyInt(), anyString())).thenReturn(new ArrayList<>());

		String pageKey = "a";
		RepositoryResource repositoryResource = new RepositoryResource();
		repositoryResource.setId(11);
		repositoryResource.setKey(pageKey);
		repositoryResource.setName("A");
		repositoryResource.setAppType(AppType.UNKNOWN);
		repositoryResource.setResourceType(RepositoryResourceType.PAGE);
		when(repositoryResourceService.findByKey(eq(repoId), 
				eq(Constant.TREE_ROOT_ID), 
				eq(RepositoryResourceType.PAGE), 
				eq(AppType.UNKNOWN), 
				eq(pageKey))).thenReturn(Optional.of(repositoryResource));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/pages/{pagePath}", owner, repositoryName, "a")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("repositoryResource.id", equalTo(11),
					  "parentGroups.size", equalTo(1),
					  "parentGroups[0].name", equalTo("A"),
					  "parentGroups[0].path", equalTo("/a"));
	}
	
	@Test
	public void getPageAtSubGroupSuccess() {
		String owner = "owner";
		String repositoryName = "repo";
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		RepositoryResource groupA = new RepositoryResource();
		groupA.setId(1);
		groupA.setKey("groupA");
		List<RepositoryResource> parents = new ArrayList<>();
		parents.add(groupA);
		when(repositoryResourceService.findParentGroupsByParentPath(anyInt(), anyString())).thenReturn(parents);

		RepositoryResource repositoryResource = new RepositoryResource();
		repositoryResource.setId(11);
		repositoryResource.setKey("page_b");
		repositoryResource.setName("PAGE_B");
		when(repositoryResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(repositoryResource));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/pages/{pagePath}", owner, repositoryName, "groupA/PAGE_B")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("repositoryResource.id", equalTo(11),
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
	public void getPageMiniProgramApp() {
		String owner = "owner";
		String repoName = "repo";
		int repoId = 1;
		Repository repo = new Repository();
		repo.setId(repoId);
		when(repositoryService.find(eq(owner), eq(repoName))).thenReturn(Optional.of(repo));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		int miniProjectId = 11;
		RepositoryResource groupA = new RepositoryResource();
		groupA.setId(miniProjectId);
		groupA.setKey("miniProgram1");
		groupA.setAppType(AppType.MINI_PROGRAM);
		groupA.setResourceType(RepositoryResourceType.PROJECT);
		List<RepositoryResource> parents = new ArrayList<>();
		parents.add(groupA);
		when(repositoryResourceService.findParentGroupsByParentPath(anyInt(), anyString())).thenReturn(parents);

		int appId = 111;
		String mainPageKey = "app";
		RepositoryResource repositoryResource = new RepositoryResource();
		repositoryResource.setId(appId);
		repositoryResource.setKey(mainPageKey);
		repositoryResource.setParentId(miniProjectId);
		repositoryResource.setAppType(AppType.MINI_PROGRAM);
		repositoryResource.setResourceType(RepositoryResourceType.PAGE);
		when(repositoryResourceService.findByKey(eq(repoId), 
				eq(miniProjectId), 
				eq(RepositoryResourceType.PAGE), 
				eq(AppType.MINI_PROGRAM), 
				eq(mainPageKey)))
			.thenReturn(Optional.of(repositoryResource));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/pages/{pagePath}", owner, repoName, "miniProgram1/app")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("repositoryResource.id", equalTo(appId),
					  "parentGroups.size", equalTo(2),
					  "parentGroups[0].name", equalTo("miniProgram1"),
					  "parentGroups[0].path", equalTo("/miniProgram1"),
					  "parentGroups[1].name", equalTo("app"),
					  "parentGroups[1].path", equalTo("/miniProgram1/app"));
	}
	
	@Test
	public void getPageMiniProgramIndexPage() {
		String owner = "owner";
		String repoName = "repo";
		int repoId = 1;
		Repository repo = new Repository();
		repo.setId(repoId);
		when(repositoryService.find(eq(owner), eq(repoName))).thenReturn(Optional.of(repo));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		int miniProjectId = 11;
		RepositoryResource groupA = new RepositoryResource();
		groupA.setId(miniProjectId);
		groupA.setKey("miniProgram1");
		groupA.setAppType(AppType.MINI_PROGRAM);
		groupA.setResourceType(RepositoryResourceType.PROJECT);
		groupA.setParentId(Constant.TREE_ROOT_ID);
		
		int pagesGroupId = 111;
		RepositoryResource groupAA = new RepositoryResource();
		groupAA.setId(pagesGroupId);
		groupAA.setKey("pages");
		groupAA.setAppType(AppType.MINI_PROGRAM);
		groupAA.setResourceType(RepositoryResourceType.GROUP);
		groupAA.setParentId(miniProjectId);
		
		List<RepositoryResource> parents = new ArrayList<>();
		parents.add(groupA);
		parents.add(groupAA);
		when(repositoryResourceService.findParentGroupsByParentPath(anyInt(), anyString())).thenReturn(parents);
		
		int indexPageId = 1111;
		String indexPageKey = "index";
		RepositoryResource indexPage = new RepositoryResource();
		indexPage.setId(indexPageId);
		indexPage.setKey(indexPageKey);
		indexPage.setAppType(AppType.MINI_PROGRAM);
		indexPage.setResourceType(RepositoryResourceType.PAGE);
		indexPage.setParentId(pagesGroupId);
		when(repositoryResourceService.findByKey(
				eq(repoId), 
				eq(pagesGroupId), 
				eq(RepositoryResourceType.PAGE), 
				eq(AppType.MINI_PROGRAM), 
				eq(indexPageKey)))
			.thenReturn(Optional.of(indexPage));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/pages/{pagePath}", owner, repoName, "miniProgram1/pages/index")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("repositoryResource.id", equalTo(indexPageId),
					  "parentGroups.size", equalTo(3),
					  "parentGroups[0].name", equalTo("miniProgram1"),
					  "parentGroups[0].path", equalTo("/miniProgram1"),
					  "parentGroups[1].name", equalTo("pages"),
					  "parentGroups[1].path", equalTo("/miniProgram1/pages"),
					  "parentGroups[2].name", equalTo("index"),
					  "parentGroups[2].path", equalTo("/miniProgram1/pages/index"));
	}
}
