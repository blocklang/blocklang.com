package com.blocklang.marketplace.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;

import com.blocklang.core.model.UserInfo;
import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.marketplace.data.NewComponentRepoParam;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.service.ComponentRepoPublishTaskService;
import com.blocklang.marketplace.service.ComponentRepoRegistryService;
import com.blocklang.marketplace.service.PublishService;

import io.restassured.http.ContentType;

@WebMvcTest(ComponentRepoController.class)
public class ComponentRepoControllerTest extends AbstractControllerTest{

	@MockBean
	private ComponentRepoRegistryService componentRepoRegistryService;
	@MockBean
	private ComponentRepoPublishTaskService componentRepoPublishTaskService;
	@MockBean
	private PublishService publishService;
	
	// 默认值，q 的值默认为 null，page 的值默认为 0
	@Test
	public void list_component_repos_q_is_null_and_page_is_null() {
		Page<ComponentRepo> result = new PageImpl<ComponentRepo>(Collections.emptyList());
		when(componentRepoRegistryService.findAllByNameOrLabel(any(), any())).thenReturn(result);
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/component-repos")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("content.size()", is(0));
	}
	
	@Test
	public void list_component_repos_q_is_empty_and_page_is_1() {
		Page<ComponentRepo> result = new PageImpl<ComponentRepo>(Collections.emptyList());
		when(componentRepoRegistryService.findAllByNameOrLabel(any(), any())).thenReturn(result);
		
		given()
			.contentType(ContentType.JSON)
			.param("q", "")
			.param("page", 1)
		.when()
			.get("/component-repos")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("content.size()", is(0));
	}
	
	@Test
	public void list_component_repos_q_is_null_and_page_is_not_a_number() {
		given()
			.contentType(ContentType.JSON)
			.param("q", "")
			.param("page", "not-a-number")
		.when()
			.get("/component-repos")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void list_component_repos_q_is_null_and_page_less_than_0() {
		given()
			.contentType(ContentType.JSON)
			.param("q", "")
			.param("page", -1)
		.when()
			.get("/component-repos")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void list_component_repos_q_is_null_and_page_greater_than_total() {
		Page<ComponentRepo> result = new PageImpl<ComponentRepo>(Collections.emptyList(), PageRequest.of(100, 6000), 1);
		
		when(componentRepoRegistryService.findAllByNameOrLabel(any(), any())).thenReturn(result);
		
		given()
			.contentType(ContentType.JSON)
			.param("q", "")
			.param("page", 100)
		.when()
			.get("/component-repos")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void list_component_repos_success() {
		ComponentRepo registry = new ComponentRepo();
		Page<ComponentRepo> result = new PageImpl<ComponentRepo>(Collections.singletonList(registry));
		when(componentRepoRegistryService.findAllByNameOrLabel(any(), any())).thenReturn(result);
		
		given()
			.contentType(ContentType.JSON)
			.param("q", "a")
			.param("page", 0)
		.when()
			.get("/component-repos")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("content.size()", is(1));
	}

	@Test
	public void new_component_repo_forbidden_anonymous() {
		NewComponentRepoParam param = new NewComponentRepoParam();
		param.setGitUrl("a");

		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/component-repos")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("jack")
	@Test
	public void new_component_repo_git_url_can_not_be_blank() {
		NewComponentRepoParam param = new NewComponentRepoParam();
		param.setGitUrl(" ");

		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/component-repos")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.gitUrl", hasItem("Git 仓库地址不能为空"),
					"errors.gitUrl.size()", is(1));
	}
	
	@WithMockUser("jack")
	@Test
	public void new_component_repo_git_url_is_not_valid() {
		NewComponentRepoParam param = new NewComponentRepoParam();
		param.setGitUrl("xxxx");

		UserInfo userInfo = new UserInfo();
		userInfo.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(userInfo));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/component-repos")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.gitUrl", hasItem("Git 仓库地址的无效，不是有效的远程仓库地址"),
					"errors.gitUrl.size()", is(1));
	}
	
	@WithMockUser("jack")
	@Test
	public void new_component_repo_git_url_not_support_ssh_schema() {
		NewComponentRepoParam param = new NewComponentRepoParam();
		param.setGitUrl("git@github.com:blocklang/blocklang.com.git");

		UserInfo userInfo = new UserInfo();
		userInfo.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(userInfo));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/component-repos")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.gitUrl", hasItem("Git 仓库地址无效，请使用 HTTPS 协议的 git 仓库地址"),
					"errors.gitUrl.size()", is(1));
	}
	
	@WithMockUser("jack")
	@Test
	public void new_component_repo_git_url_repo_not_exist() {
		NewComponentRepoParam param = new NewComponentRepoParam();
		param.setGitUrl("https://github.com/blocklang/not-exist-repo.git");

		UserInfo userInfo = new UserInfo();
		userInfo.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(userInfo));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/component-repos")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.gitUrl", hasItem("Git 仓库地址无效，该仓库不存在"),
					"errors.gitUrl.size()", is(1));
	}
	
	@WithMockUser("jack")
	@Test
	public void new_component_repo_git_url_is_used() {
		NewComponentRepoParam param = new NewComponentRepoParam();
		param.setGitUrl("https://github.com/blocklang/blocklang.com.git");

		UserInfo userInfo = new UserInfo();
		userInfo.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(userInfo));
		
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		when(componentRepoPublishTaskService.findByGitUrlAndUserId(anyInt(), any())).thenReturn(Optional.of(task));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/component-repos")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.gitUrl", hasItem("<strong>jack</strong>已发布过该仓库，无需重复发布"),
					"errors.gitUrl.size()", is(1));
	}
	
	@WithMockUser("jack")
	@Test
	public void new_component_repo_git_url_support_https_schema() {
		NewComponentRepoParam param = new NewComponentRepoParam();
		param.setGitUrl("https://github.com/blocklang/blocklang.com.git");

		UserInfo userInfo = new UserInfo();
		userInfo.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(userInfo));
		
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		task.setId(1);
		task.setGitUrl("https://github.com/blocklang/blocklang.com.git");
		when(componentRepoPublishTaskService.save(any())).thenReturn(task);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/component-repos")
		.then()
			.statusCode(HttpStatus.SC_CREATED)
			.body("id", is(1),
					"gitUrl", equalTo("https://github.com/blocklang/blocklang.com.git"));
		
		verify(publishService).asyncPublish(any());
	}

	
}
