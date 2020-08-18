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
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.ProjectDependenceService;
import com.blocklang.develop.service.RepositoryPermissionService;
import com.blocklang.develop.service.RepositoryResourceService;
import com.blocklang.develop.service.RepositoryService;
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
	private RepositoryService repositoryService;
	@MockBean
	private RepositoryPermissionService repositoryPermissionService;
	@MockBean
	private RepositoryResourceService repositoryResourceService;
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
	public void getDependencyRepoNotExist() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		
		when(repositoryService.find(eq(owner), eq(repoName))).thenReturn(Optional.empty());
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/{projectName}/dependency", owner, repoName, projectName)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}
	
	@Test
	public void getDependencyCanNotReadRepo() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		
		Integer repoId = 1;
		Repository repository = new Repository();
		repository.setId(repoId);
		when(repositoryService.find(eq(owner), eq(repoName))).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/{projectName}/dependency", owner, repoName, projectName)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}

	@Test
	public void getDependencyProjectNotExist() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		
		Integer repoId = 1;
		Repository repository = new Repository();
		repository.setId(repoId);
		when(repositoryService.find(eq(owner), eq(repoName))).thenReturn(Optional.of(repository));
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		when(repositoryResourceService.findProject(eq(repoId), eq(projectName))).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/{projectName}/dependency", owner, repoName, projectName)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}
	
	@Test
	public void getDependencySuccess() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		
		Integer repoId = 1;
		Repository repository = new Repository();
		repository.setId(repoId);
		when(repositoryService.find(eq(owner), eq(repoName))).thenReturn(Optional.of(repository));
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		RepositoryResource project = new RepositoryResource();
		project.setId(11);
		project.setAppType(AppType.WEB);
		when(repositoryResourceService.findProject(eq(repoId), eq(projectName))).thenReturn(Optional.of(project));
		
		RepositoryResource resource = new RepositoryResource();
		resource.setId(10);
		resource.setKey("a");
		when(repositoryResourceService.findByKey(anyInt(), anyInt(), any(), any(), anyString())).thenReturn(Optional.of(resource));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/{projectName}/dependency", owner, repoName, projectName)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("resourceId", is(10),
					"pathes.size()", is(1),
					"pathes[0].name", equalTo("a"),
					"pathes[0].path", equalTo("/a"));
	}

	// FIXME: 新增依赖需进一步优化，等重构完成后再优化。
	
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
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
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
		Repository project = new Repository();
		project.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());

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
		Repository project = new Repository();
		project.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
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
		Repository project = new Repository();
		project.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
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
		Repository project = new Repository();
		project.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(project));

		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
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
		Repository project = new Repository();
		project.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
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
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
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
		Repository project = new Repository();
		project.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.empty());
		
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
		Repository project = new Repository();
		project.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
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
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
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
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
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
		Repository project = new Repository();
		project.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());
		
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
		Repository project = new Repository();
		project.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
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
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
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
		Repository project = new Repository();
		project.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());

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
		Repository project = new Repository();
		project.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
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
