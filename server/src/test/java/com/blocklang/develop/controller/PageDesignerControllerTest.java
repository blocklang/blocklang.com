package com.blocklang.develop.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.develop.data.ProjectDependenceData;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.develop.service.ProjectAuthorizationService;
import com.blocklang.develop.service.ProjectDependenceService;
import com.blocklang.develop.service.ProjectService;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;

import io.restassured.http.ContentType;

@WebMvcTest(PageDesignerController.class)
public class PageDesignerControllerTest extends AbstractControllerTest {

	@MockBean
	private ProjectService projectService;
	@MockBean
	private ProjectAuthorizationService projectAuthorizationService;
	@MockBean
	private ProjectDependenceService projectDependenceService;
	
	@Test
	public void list_project_dependences_project_not_exist() {
		when(projectService.findById(anyInt())).thenReturn(Optional.empty());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences?category=dev", 1)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	// 用户未登录，所以无权访问私有项目
	@Test
	public void list_project_dependences_project_exist_but_can_not_read() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(false);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences?category=dev", 1)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@Test
	public void list_project_dependences_only_filter_dev_repo() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		ProjectDependence dependence = new ProjectDependence();
		ComponentRepo componentRepo = new ComponentRepo();
		componentRepo.setId(1);
		componentRepo.setGitRepoWebsite("website1");
		componentRepo.setGitRepoOwner("owner1");
		componentRepo.setGitRepoName("repoName1");
		componentRepo.setName("name1");
		componentRepo.setCategory(RepoCategory.WIDGET);
		componentRepo.setStd(true);
		componentRepo.setIsIdeExtension(false);
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setVersion("0.0.1");
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setId(2);
		ApiRepoVersion apiRepoVersion = new ApiRepoVersion();
		ProjectDependenceData data = new ProjectDependenceData(dependence, componentRepo, componentRepoVersion, apiRepo, apiRepoVersion);
		when(projectDependenceService.findProjectDependences(anyInt())).thenReturn(Collections.singletonList(data));

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences?category=dev", 1)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", equalTo(0));
	}
	
	@Test
	public void list_project_dependences_success() {
		Project project = new Project();
		project.setId(1);
		project.setIsPublic(true);
		when(projectService.findById(anyInt())).thenReturn(Optional.of(project));
		
		ProjectDependence dependence = new ProjectDependence();
		ComponentRepo componentRepo = new ComponentRepo();
		componentRepo.setId(1);
		componentRepo.setGitRepoWebsite("website1");
		componentRepo.setGitRepoOwner("owner1");
		componentRepo.setGitRepoName("repoName1");
		componentRepo.setName("name1");
		componentRepo.setCategory(RepoCategory.WIDGET);
		componentRepo.setStd(true);
		componentRepo.setIsIdeExtension(true);
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setVersion("0.0.1");
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setId(2);
		ApiRepoVersion apiRepoVersion = new ApiRepoVersion();
		ProjectDependenceData data = new ProjectDependenceData(dependence, componentRepo, componentRepoVersion, apiRepo, apiRepoVersion);
		when(projectDependenceService.findProjectDependences(anyInt())).thenReturn(Collections.singletonList(data));

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/designer/projects/{projectId}/dependences?category=dev", 1)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", equalTo(1),
					"[0].id", equalTo(1),
					"[0].gitRepoWebsite", equalTo("website1"),
					"[0].gitRepoOwner", equalTo("owner1"),
					"[0].gitRepoName", equalTo("repoName1"),
					"[0].name", equalTo("name1"),
					"[0].apiRepoId", equalTo(2),
					"[0].category", equalTo("Widget"),
					"[0].version", equalTo("0.0.1"),
					"[0].std", equalTo(true));
	}
}
