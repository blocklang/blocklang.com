package com.blocklang.develop.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectAuthorizationService;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.develop.service.ProjectService;

import io.restassured.http.ContentType;

@WebMvcTest(ProjectDependenceController.class)
public class ProjectDependenceControllerTest extends AbstractControllerTest{
	
	@MockBean
	private ProjectService projectService;
	@MockBean
	private ProjectAuthorizationService projectAuthorizationService;
	@MockBean
	private ProjectResourceService projectResourceService;

	@Test
	public void get_dependence_project_not_found() {
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/dependence", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}
	
	@Test
	public void get_dependence_anonymous_user_forbidden_access_private_project() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/dependence", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	@Test
	public void get_dependence_success() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		ProjectResource resource = new ProjectResource();
		resource.setId(10);
		when(projectResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/dependence", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("resourceId", is(10),
					"pathes.size()", is(1));
	}
	
}
