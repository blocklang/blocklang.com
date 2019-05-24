package com.blocklang.marketplace.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.marketplace.model.ComponentRepoRegistry;
import com.blocklang.marketplace.service.ComponentRepoRegistryService;

import io.restassured.http.ContentType;

@WebMvcTest(ComponentRepoController.class)
public class ComponentRepoControllerTest  extends AbstractControllerTest{

	@MockBean
	private ComponentRepoRegistryService componentRepoRegistryService;
	
	// 默认值，q 的值默认为 null，page 的值默认为 0
	@Test
	public void list_component_repos_q_is_null_and_page_is_null() {
		Page<ComponentRepoRegistry> result = new PageImpl<ComponentRepoRegistry>(Collections.emptyList());
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
		Page<ComponentRepoRegistry> result = new PageImpl<ComponentRepoRegistry>(Collections.emptyList());
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
		Page<ComponentRepoRegistry> result = new PageImpl<ComponentRepoRegistry>(Collections.emptyList(), PageRequest.of(100, 6000), 1);
		
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
		ComponentRepoRegistry registry = new ComponentRepoRegistry();
		Page<ComponentRepoRegistry> result = new PageImpl<ComponentRepoRegistry>(Collections.singletonList(registry));
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
}
