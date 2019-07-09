package com.blocklang.marketplace.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.is;

import java.util.Collections;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import com.blocklang.core.model.UserInfo;
import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.service.ComponentRepoPublishTaskService;

import io.restassured.http.ContentType;

@WebMvcTest(ComponentRepoPublishTaskController.class)
public class ComponentRepoPublishTaskControllerTest extends AbstractControllerTest {

	@MockBean
	private ComponentRepoPublishTaskService componentRepoPublishTaskService;
	
	@Test
	public void list_my_component_repo_publishing_tasks_anonymous_forbidden() {
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/user/component-repos/publishing-tasks")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("not-exist-user")
	@Test
	public void list_my_component_repo_publishing_tasks_user_not_exist_forbidden() {
		when(userService.findByLoginName(anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/user/component-repos/publishing-tasks")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("jack")
	@Test
	public void list_my_component_repo_publishing_tasks_success() {
		UserInfo userInfo = new UserInfo();
		userInfo.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(userInfo));
		
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		when(componentRepoPublishTaskService.findUserPublishingTasks(anyInt())).thenReturn(Collections.singletonList(task));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/user/component-repos/publishing-tasks")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", is(1));
	}
	
}
