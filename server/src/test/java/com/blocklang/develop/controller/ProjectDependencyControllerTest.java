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
import com.blocklang.develop.constant.RepoBranchName;
import com.blocklang.develop.data.AddDependencyParam;
import com.blocklang.develop.data.UpdateDependencyParam;
import com.blocklang.develop.model.ProjectDependency;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.ProjectDependencyService;
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

@WebMvcTest(ProjectDependencyController.class)
public class ProjectDependencyControllerTest extends AbstractControllerTest{
	
	@MockBean
	private RepositoryService repositoryService;
	@MockBean
	private RepositoryPermissionService repositoryPermissionService;
	@MockBean
	private RepositoryResourceService repositoryResourceService;
	@MockBean
	private ProjectDependencyService projectDependencyService;
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
	public void addDependencyAnonymousCanNotAdd() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		AddDependencyParam param = new AddDependencyParam();
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/{projectName}/dependencies", owner, repoName, projectName)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("jack")
	@Test
	public void addDependencyRepositoryNotExist() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		AddDependencyParam param = new AddDependencyParam();
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/{projectName}/dependencies", owner, repoName, projectName)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser("jack")
	@Test
	public void addDependencyCanNotWriteRepository() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());

		AddDependencyParam param = new AddDependencyParam();
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/{projectName}/dependencies", owner, repoName, projectName)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("jack")
	@Test
	public void addDependencyProjectNotExist() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer repositoryId = 1;
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		when(repositoryResourceService.findProject(eq(repositoryId), eq(projectName))).thenReturn(Optional.empty());
		
		AddDependencyParam param = new AddDependencyParam();
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/{projectName}/dependences", owner, repoName, projectName)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser("jack")
	@Test
	public void addDependencyAddingDependencyNotExist() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer repositoryId = 1;
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		
		RepositoryResource project = new RepositoryResource();
		project.setId(2);
		
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		when(repositoryResourceService.findProject(eq(repositoryId), eq(projectName))).thenReturn(Optional.of(project));
		
		when(componentRepoService.findById(anyInt())).thenReturn(Optional.empty());

		AddDependencyParam param = new AddDependencyParam();
		param.setComponentRepoId(1);
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/{projectName}/dependences", owner, repoName, projectName)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser("jack")
	@Test
	public void addDependencyDevDependencyHasAdded() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer repositoryId = 1;
		Integer projectId = 2;
		Integer componentRepoId = 3;
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		
		RepositoryResource project = new RepositoryResource();
		project.setId(projectId);
		
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		when(repositoryResourceService.findProject(eq(repositoryId), eq(projectName))).thenReturn(Optional.of(project));
		
		ComponentRepo repo = new ComponentRepo();
		repo.setId(componentRepoId);
		repo.setRepoType(RepoType.IDE);
		when(componentRepoService.findById(eq(componentRepoId))).thenReturn(Optional.of(repo));
		
		ComponentRepoVersion repoVersion = new ComponentRepoVersion();
		when(componentRepoVersionService.findByComponentIdAndVersion(eq(componentRepoId), eq(RepoBranchName.MASTER))).thenReturn(Optional.of(repoVersion));
		
		when(projectDependencyService.devDependencyExists(eq(projectId), eq(componentRepoId))).thenReturn(true);
		
		AddDependencyParam param = new AddDependencyParam();
		param.setComponentRepoId(componentRepoId);
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/{projectName}/dependencies", owner, repoName, projectName)
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.componentRepoId", hasItem("项目已依赖该组件仓库"),
					"errors.componentRepoId.size()", is(1));
	}

	@WithMockUser("jack")
	@Test
	public void addDependencyBuildDependencyHasAdded() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer repositoryId = 1;
		Integer projectId = 2;
		Integer buildProfileId = 3;
		Integer componentRepoId = 4;
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		
		RepositoryResource project = new RepositoryResource();
		project.setId(projectId);
		
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		when(repositoryResourceService.findProject(eq(repositoryId), eq(projectName))).thenReturn(Optional.of(project));
		
		ComponentRepo repo = new ComponentRepo();
		repo.setRepoType(RepoType.PROD);
		repo.setId(componentRepoId);
		when(componentRepoService.findById(anyInt())).thenReturn(Optional.of(repo));
		
		ComponentRepoVersion repoMaster = new ComponentRepoVersion();
		repoMaster.setApiRepoVersionId(1);
		repoMaster.setAppType(AppType.WEB);
		// 默认依赖 master 分支（即最新内容）
		when(componentRepoVersionService.findByComponentIdAndVersion(eq(componentRepoId), eq(RepoBranchName.MASTER))).thenReturn(Optional.of(repoMaster));
		
		when(projectDependencyService.buildDependencyExists(eq(projectId), eq(buildProfileId), eq(componentRepoId))).thenReturn(true);
		
		AddDependencyParam param = new AddDependencyParam();
		param.setBuildProfileId(buildProfileId);
		param.setComponentRepoId(componentRepoId);
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/{projectName}/dependencies", owner, repoName, projectName)
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.componentRepoId", hasItem("项目已依赖该组件仓库"),
					"errors.componentRepoId.size()", is(1));
	}
	
	@WithMockUser("jack")
	@Test
	public void addDependencySuccess() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer repositoryId = 1;
		Integer projectId = 2;
		Integer buildProfileId = 3;
		Integer componentRepoId = 4;
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		
		RepositoryResource project = new RepositoryResource();
		project.setId(projectId);
		
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		when(repositoryResourceService.findProject(eq(repositoryId), eq(projectName))).thenReturn(Optional.of(project));
		
		ComponentRepo repo = new ComponentRepo();
		repo.setRepoType(RepoType.PROD);
		repo.setId(componentRepoId);
		when(componentRepoService.findById(eq(componentRepoId))).thenReturn(Optional.of(repo));
		
		ComponentRepoVersion repoMaster = new ComponentRepoVersion();
		repoMaster.setApiRepoVersionId(1);
		repoMaster.setAppType(AppType.WEB);
		// 默认依赖 master 分支（即最新内容）
		when(componentRepoVersionService.findByComponentIdAndVersion(eq(componentRepoId), eq(RepoBranchName.MASTER))).thenReturn(Optional.of(repoMaster));
		
		when(projectDependencyService.buildDependencyExists(eq(projectId), eq(buildProfileId), eq(componentRepoId))).thenReturn(false);
		
		Integer apiRepoId = 5;
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setId(apiRepoId);
		when(apiRepoService.findById(anyInt())).thenReturn(Optional.of(apiRepo));
		
		ApiRepoVersion apiRepoVersion = new ApiRepoVersion();
		apiRepoVersion.setApiRepoId(apiRepoId);
		when(apiRepoVersionService.findById(anyInt())).thenReturn(Optional.of(apiRepoVersion));
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));
		
		ProjectDependency dependency = new ProjectDependency();
		dependency.setId(10);
		dependency.setComponentRepoVersionId(2);
		when(projectDependencyService.save(any(), any(), any())).thenReturn(dependency);
		
		AddDependencyParam param = new AddDependencyParam();
		param.setComponentRepoId(componentRepoId);
		param.setBuildProfileId(buildProfileId);
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/{projectName}/dependencies", owner, repoName, projectName)
		.then()
			.statusCode(HttpStatus.SC_CREATED)
			.body("dependency.id", is(10),
					"componentRepo", is(notNullValue()),
					"componentRepoVersion", is(notNullValue()),
					"apiRepo", is(notNullValue()),
					"apiRepoVersion", is(notNullValue()));
		
		verify(projectDependencyService).save(any(), any(), any());
	}
	
	@Test
	public void listDependenciesRepositoryNotExist() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		
		when(repositoryService.find(eq(owner), eq(repoName))).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/{projectName}/dependencies", owner, repoName, projectName)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}
	
	@Test
	public void listDependenciesCanNotReadRepository() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer repositoryId = 1;
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/{projectName}/dependencies", owner, repoName, projectName)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	@Test
	public void listDependenciesProjectNotExist() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer repositoryId = 1;
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		when(repositoryResourceService.findProject(eq(repositoryId), eq(projectName))).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/{projectName}/dependencies", owner, repoName, projectName)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND)
			.body(equalTo(""));
	}

	@Test
	public void listDependenciesSuccess() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer repositoryId = 1;
		Integer projectId = 2;
		AppType appType = AppType.WEB;
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		RepositoryResource project = new RepositoryResource();
		project.setId(projectId);
		project.setAppType(appType);
		when(repositoryResourceService.findProject(eq(repositoryId), eq(projectName))).thenReturn(Optional.of(project));
		
		when(projectDependencyService.findAllConfigDependencies(anyInt())).thenReturn(Collections.emptyList());
		when(projectDependencyService.findStdDevDependencies(eq(projectId), eq(appType))).thenReturn(Collections.emptyList());
		when(projectDependencyService.findStdBuildDependencies(eq(projectId), eq(appType))).thenReturn(Collections.emptyList());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/{projectName}/dependencies", owner, repoName, projectName)
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", is(0));
	}
	
	@Test
	public void deleteDependencyAnonymousCanNotDelete() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer dependencyId = 1;
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.delete("/repos/{owner}/{repoName}/{projectName}/dependencies/{dependencyId}", owner, repoName, projectName, dependencyId)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}

	@WithMockUser("jack")
	@Test
	public void deleteDependencyRepositoryNotExist() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer dependencyId = 1;
		
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.delete("/repos/{owner}/{repoName}/{projectName}/dependencies/{dependencyId}", owner, repoName, projectName, dependencyId)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser("jack")
	@Test
	public void deleteDependencyCanNotWriteRepository() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer repositoryId = 1;
		Integer dependencyId = 2;
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.delete("/repos/{owner}/{repoName}/{projectName}/dependencies/{dependencyId}", owner, repoName, projectName, dependencyId)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("jack")
	@Test
	public void deleteDependencyProjectNotExist() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer repositoryId = 1;
		Integer dependencyId = 2;
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		when(repositoryResourceService.findProject(repositoryId, projectName)).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.delete("/repos/{owner}/{repoName}/{projectName}/dependencies/{dependencyId}", owner, repoName, projectName, dependencyId)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser("jack")
	@Test
	public void deleteDependency_success() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer repositoryId = 1;
		Integer dependencyId = 2;
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		RepositoryResource project = new RepositoryResource();
		when(repositoryResourceService.findProject(repositoryId, projectName)).thenReturn(Optional.of(project));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.delete("/repos/{owner}/{repoName}/{projectName}/dependencies/{dependencyId}", owner, repoName, projectName, dependencyId)
		.then()
			.statusCode(HttpStatus.SC_NO_CONTENT)
			.body(equalTo(""));
		
		verify(projectDependencyService).delete(eq(repository), eq(project), anyInt());
	}

	@Test
	public void updateDependencyAnonymousCanNotUpdate() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer dependencyId = 2;
		
		UpdateDependencyParam param = new UpdateDependencyParam();
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.put("/repos/{owner}/{repoName}/{projectName}/dependencies/{dependencyId}", owner, repoName, projectName, dependencyId)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("jack")
	@Test
	public void updateDependencyRepositoryNotExist() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer dependencyId = 2;
		
		when(repositoryService.find(eq(owner), eq(repoName))).thenReturn(Optional.empty());
		
		UpdateDependencyParam param = new UpdateDependencyParam();
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.put("/repos/{owner}/{repoName}/{projectName}/dependencies/{dependencyId}", owner, repoName, projectName, dependencyId)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser("jack")
	@Test
	public void updateDependencyCanNotWriteRepository() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer repositoryId = 1;
		Integer dependencyId = 2;
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		when(repositoryService.find(eq(owner), eq(repoName))).thenReturn(Optional.of(repository));
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());

		UpdateDependencyParam param = new UpdateDependencyParam();
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.put("/repos/{owner}/{repoName}/{projectName}/dependencies/{dependencyId}", owner, repoName, projectName, dependencyId)
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser("jack")
	@Test
	public void updateDependencyProjectNotExist() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer repositoryId = 1;
		Integer dependencyId = 2;
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		when(repositoryService.find(eq(owner), eq(repoName))).thenReturn(Optional.of(repository));
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		when(repositoryResourceService.findProject(eq(repositoryId), eq(projectName))).thenReturn(Optional.empty());
		
		UpdateDependencyParam param = new UpdateDependencyParam();
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.put("/repos/{owner}/{repoName}/{projectName}/dependencies/{dependencyId}", owner, repoName, projectName, dependencyId)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser("jack")
	@Test
	public void updateDependencySuccess() {
		String owner = "jack";
		String repoName = "repo1";
		String projectName = "project1";
		Integer repositoryId = 1;
		Integer dependencyId = 2;
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		RepositoryResource project = new RepositoryResource();
		when(repositoryResourceService.findProject(eq(repositoryId), eq(projectName))).thenReturn(Optional.of(project));
		
		UserInfo user = new UserInfo();
		user.setId(1);
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(user));

		ProjectDependency dependency = new ProjectDependency();
		dependency.setComponentRepoVersionId(1);
		when(projectDependencyService.findById(eq(dependencyId))).thenReturn(Optional.of(dependency));
		
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setId(1);
		version.setVersion("0.2.0");
		version.setAppType(AppType.WEB);
		when(componentRepoVersionService.findById(anyInt())).thenReturn(Optional.of(version));
		
		UpdateDependencyParam param = new UpdateDependencyParam();
		param.setComponentRepoVersionId(1);
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.put("/repos/{owner}/{repoName}/{projectName}/dependencies/{dependencyId}", owner, repoName, projectName, dependencyId)
		.then()
			.statusCode(HttpStatus.SC_CREATED)
			.body("id", is(1),
					"version", is("0.2.0"));
		
		verify(projectDependencyService).update(eq(repository), eq(project), eq(dependency));
	}

}
