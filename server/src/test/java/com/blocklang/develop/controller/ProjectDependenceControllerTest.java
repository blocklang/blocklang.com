package com.blocklang.develop.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import com.blocklang.core.model.UserInfo;
import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.data.AddDependenceParam;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectAuthorization;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectAuthorizationService;
import com.blocklang.develop.service.ProjectDependenceService;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.develop.service.ProjectService;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;
import com.blocklang.marketplace.service.ApiRepoService;
import com.blocklang.marketplace.service.ApiRepoVersionService;
import com.blocklang.marketplace.service.ComponentRepoService;
import com.blocklang.marketplace.service.ComponentRepoVersionService;

import io.restassured.http.ContentType;

@WebMvcTest(ProjectDependenceController.class)
public class ProjectDependenceControllerTest extends AbstractControllerTest{
	
	@MockBean
	private ProjectService projectService;
	@MockBean
	private ProjectAuthorizationService projectAuthorizationService;
	@MockBean
	private ProjectResourceService projectResourceService;
	@MockBean
	private ProjectDependenceService projectDependenceService;
	@MockBean
	private ComponentRepoService componentRepoService;
	@MockBean
	private ComponentRepoVersionService componentRepoVersionService;
	@MockBean
	private ApiRepoService apiRepoService;
	@MockBean
	private ApiRepoVersionService apiRepoVersionService;

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

	@Test
	public void add_dependence_anonymous_user_forbidden() {
		AddDependenceParam param = new AddDependenceParam();
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/dependences", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("jack")
	@Test
	public void add_dependence_project_not_exist() {
		AddDependenceParam param = new AddDependenceParam();
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/dependences", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser("jack")
	@Test
	public void add_dependence_login_user_can_not_write() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		ComponentRepo repo = new ComponentRepo();
		repo.setIsIdeExtension(false);
		when(componentRepoService.findById(anyInt())).thenReturn(Optional.of(repo));
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.emptyList());
		
		AddDependenceParam param = new AddDependenceParam();
		param.setComponentRepoId(1);
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/dependences", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("jack")
	@Test
	public void add_dependence_when_is_build_dependence_but_the_dependence_has_added() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.WRITE);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		ComponentRepo repo = new ComponentRepo();
		repo.setIsIdeExtension(false);
		when(componentRepoService.findById(anyInt())).thenReturn(Optional.of(repo));
		
		when(projectDependenceService.buildDependenceExists(anyInt(), anyInt(), any(), anyString())).thenReturn(true);
		
		AddDependenceParam param = new AddDependenceParam();
		param.setComponentRepoId(1);
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/dependences", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.componentRepoId", hasItem("项目已依赖该组件仓库"),
					"errors.componentRepoId.size()", is(1));
	}
	
	@WithMockUser("jack")
	@Test
	public void add_dependence_when_is_api_dependence_but_the_dependence_has_added() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.WRITE);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		ComponentRepo repo = new ComponentRepo();
		repo.setIsIdeExtension(true);
		when(componentRepoService.findById(anyInt())).thenReturn(Optional.of(repo));
		
		when(projectDependenceService.devDependenceExists(anyInt(), anyInt())).thenReturn(true);
		
		AddDependenceParam param = new AddDependenceParam();
		param.setComponentRepoId(1);
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/dependences", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.componentRepoId", hasItem("项目已依赖该组件仓库"),
					"errors.componentRepoId.size()", is(1));
	}
	
	@WithMockUser("jack")
	@Test
	public void add_dependence_success() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.WRITE);
		when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.singletonList(auth));
		
		ComponentRepo componentRepo = new ComponentRepo();
		componentRepo.setApiRepoId(1);
		componentRepo.setIsIdeExtension(false);
		when(componentRepoService.findById(anyInt())).thenReturn(Optional.of(componentRepo));
		
		when(projectDependenceService.buildDependenceExists(anyInt(), anyInt(), any(), anyString())).thenReturn(false);
		
		ProjectDependence dependence = new ProjectDependence();
		dependence.setComponentRepoVersionId(2);
		when(projectDependenceService.save(anyInt(), any(), any())).thenReturn(dependence);
		
		ApiRepo apiRepo = new ApiRepo();
		when(apiRepoService.findById(anyInt())).thenReturn(Optional.of(apiRepo));
		
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setApiRepoVersionId(1);
		when(componentRepoVersionService.findById(anyInt())).thenReturn(Optional.of(componentRepoVersion));
		
		ApiRepoVersion apiRepoVersion = new ApiRepoVersion();
		when(apiRepoVersionService.findById(anyInt())).thenReturn(Optional.of(apiRepoVersion));
		
		AddDependenceParam param = new AddDependenceParam();
		param.setComponentRepoId(1);
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/dependences", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_CREATED)
			.body("componentRepo", is(notNullValue()),
					"componentRepoVersion", is(notNullValue()),
					"apiRepo", is(notNullValue()),
					"apiRepoVersion", is(notNullValue()));
		
		verify(projectDependenceService).save(anyInt(), any(), any());
	}
}
