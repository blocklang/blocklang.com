package com.blocklang.develop.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import com.blocklang.core.model.UserInfo;
import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.data.AddDependenceParam;
import com.blocklang.develop.data.UpdateDependenceParam;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectDependenceService;
import com.blocklang.develop.service.ProjectPermissionService;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.develop.service.ProjectService;
import com.blocklang.marketplace.constant.RepoType;
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
	private ProjectPermissionService projectPermissionService;
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
	public void get_dependence_project_not_exist() {
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
	public void get_dependence_can_not_read_project() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.empty());
		
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
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		ProjectResource resource = new ProjectResource();
		resource.setId(10);
		resource.setKey("a");
		when(projectResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/dependence", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("resourceId", is(10),
					"pathes.size()", is(1),
					"pathes[0].name", equalTo("a"),
					"pathes[0].path", equalTo("/a"));
	}

	@Test
	public void add_dependence_anonymous_can_not_add() {
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
	public void add_dependence_can_not_write_project() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());

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
	public void add_dependence_will_add_dependence_not_exist() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		when(componentRepoService.findById(anyInt())).thenReturn(Optional.empty());

		AddDependenceParam param = new AddDependenceParam();
		param.setComponentRepoId(1);
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/dependences", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
//	ComponentRepo repo = new ComponentRepo();
//	repo.setIsIdeExtension(false);
//	when(componentRepoService.findById(anyInt())).thenReturn(Optional.of(repo));
//	
//	UserInfo user = new UserInfo();
//	user.setId(1);
//	when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
//	
//	when(projectAuthorizationService.findAllByUserIdAndProjectId(anyInt(), anyInt())).thenReturn(Collections.emptyList());
//	
	
	@WithMockUser("jack")
	@Test
	public void add_dependence_when_is_build_dependence_but_the_dependence_has_added() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		ComponentRepo repo = new ComponentRepo();
		repo.setRepoType(RepoType.PROD);
		repo.setId(1);
		when(componentRepoService.findById(anyInt())).thenReturn(Optional.of(repo));
		
		ComponentRepoVersion repoMaster = new ComponentRepoVersion();
		repoMaster.setApiRepoVersionId(1);
		repoMaster.setAppType(AppType.WEB);
		when(componentRepoVersionService.findByComponentIdAndVersion(anyInt(), eq("master"))).thenReturn(Optional.of(repoMaster));
		
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
	public void add_dependence_when_is_dev_dependence_but_the_dependence_has_added() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));

		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		ComponentRepo repo = new ComponentRepo();
		repo.setRepoType(RepoType.IDE);
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
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		ComponentRepo componentRepo = new ComponentRepo();
		componentRepo.setId(1);
		componentRepo.setRepoType(RepoType.PROD);
		when(componentRepoService.findById(anyInt())).thenReturn(Optional.of(componentRepo));
		
		ComponentRepoVersion repoMaster = new ComponentRepoVersion();
		repoMaster.setApiRepoVersionId(1);
		repoMaster.setAppType(AppType.WEB);
		when(componentRepoVersionService.findByComponentIdAndVersion(anyInt(), eq("master"))).thenReturn(Optional.of(repoMaster));
		
		when(projectDependenceService.buildDependenceExists(anyInt(), anyInt(), any(), anyString())).thenReturn(false);
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectDependence dependence = new ProjectDependence();
		dependence.setId(10);
		dependence.setComponentRepoVersionId(2);
		when(projectDependenceService.save(anyInt(), any(), any())).thenReturn(dependence);
	
		Integer apiRepoId = 1;
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setId(apiRepoId);
		when(apiRepoService.findById(anyInt())).thenReturn(Optional.of(apiRepo));
		
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setApiRepoVersionId(1);
		when(componentRepoVersionService.findById(anyInt())).thenReturn(Optional.of(componentRepoVersion));
		
		ApiRepoVersion apiRepoVersion = new ApiRepoVersion();
		apiRepoVersion.setApiRepoId(apiRepoId);
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
			.body("dependence.id", is(10),
					"componentRepo", is(notNullValue()),
					"componentRepoVersion", is(notNullValue()),
					"apiRepo", is(notNullValue()),
					"apiRepoVersion", is(notNullValue()));
		
		verify(projectDependenceService).save(anyInt(), any(), any());
	}
	
	@Test
	public void list_dependences_project_not_exist() {
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/dependences", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}
	
	@Test
	public void list_dependences_can_not_read_project() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/dependences", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}

	@Test
	public void list_dependences_success() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		when(projectDependenceService.findProjectDependences(anyInt())).thenReturn(Collections.emptyList());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/dependences", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", is(0));
	}
	
	@Test
	public void delete_dependence_anonymous_can_not_delete() {
		AddDependenceParam param = new AddDependenceParam();
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.delete("/projects/{owner}/{projectName}/dependences/{dependenceId}", "jack", "project", 1)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}


	@WithMockUser("jack")
	@Test
	public void delete_dependence_project_not_exist() {
		AddDependenceParam param = new AddDependenceParam();
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.delete("/projects/{owner}/{projectName}/dependences/{dependenceId}", "jack", "project", 1)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser("jack")
	@Test
	public void delete_dependence_can_not_write_project() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.delete("/projects/{owner}/{projectName}/dependences/{dependenceId}", "jack", "project", 1)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("jack")
	@Test
	public void delete_dependence_success() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.delete("/projects/{owner}/{projectName}/dependences/{dependenceId}", "jack", "project", 1)
		.then()
			.statusCode(HttpStatus.SC_NO_CONTENT)
			.body(equalTo(""));
		
		verify(projectDependenceService).delete(anyInt());
	}

	@Test
	public void update_dependence_anonymous_can_not_update() {
		UpdateDependenceParam param = new UpdateDependenceParam();
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.put("/projects/{owner}/{projectName}/dependences/{dependenceId}", "jack", "project", 1)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("jack")
	@Test
	public void update_dependence_project_not_exist() {
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		UpdateDependenceParam param = new UpdateDependenceParam();
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.put("/projects/{owner}/{projectName}/dependences/{dependenceId}", "jack", "project", 1)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser("jack")
	@Test
	public void update_dependence_can_not_write_project() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());

		UpdateDependenceParam param = new UpdateDependenceParam();
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.put("/projects/{owner}/{projectName}/dependences/{dependenceId}", "jack", "project", 1)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("jack")
	@Test
	public void update_dependence_success() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));

		ProjectDependence dependence = new ProjectDependence();
		dependence.setComponentRepoVersionId(1);
		when(projectDependenceService.findById(anyInt())).thenReturn(Optional.of(dependence));
		
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setId(1);
		version.setVersion("0.2.0");
		version.setAppType(AppType.WEB);
		when(componentRepoVersionService.findById(anyInt())).thenReturn(Optional.of(version));
		
		UpdateDependenceParam param = new UpdateDependenceParam();
		param.setComponentRepoVersionId(1);
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.put("/projects/{owner}/{projectName}/dependences/{dependenceId}", "jack", "project", 1)
		.then()
			.statusCode(HttpStatus.SC_CREATED)
			.body("id", is(1),
					"version", is("0.2.0"));
		
		verify(projectDependenceService).update(any());
	}

}
