package com.blocklang.develop.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import com.blocklang.core.git.exception.GitEmptyCommitException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.data.CommitMessage;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.service.RepositoryPermissionService;
import com.blocklang.develop.service.RepositoryResourceService;
import com.blocklang.develop.service.RepositoryService;

import io.restassured.http.ContentType;

@WebMvcTest(CommitController.class)
public class CommitControllerTest extends AbstractControllerTest{

	@MockBean
	private RepositoryService repositoryService;
	@MockBean
	private RepositoryResourceService repositoryResourceService;
	@MockBean
	private RepositoryPermissionService repositoryPermissionService;

	@Test
	public void listChangesRepoNotFound() {
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/changes", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void listChangesCanNotReadRepo() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/changes", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@Test
	public void listChangesSuccess() {
		Repository repository = new Repository();
		repository.setId(1);
		repository.setCreateUserName("jack");
		repository.setName("repo");
		repository.setIsPublic(true);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canRead(any(), any())).thenReturn(Optional.of(AccessLevel.READ));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/repos/{owner}/{repoName}/changes", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("[]"));
	}
	
	@Test
	public void stageChangesAnonymousCanNotStage() {
		String[] param = new String[] {};
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/stage-changes", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void stageChangesRepoNotFound() {
		String[] param = new String[] {};
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/stage-changes", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void stageChangesCanNotWriteRepo() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());
		
		String[] param = new String[] {};
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/stage-changes", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void stageChangesSuccess() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		String[] param = new String[] {};
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/stage-changes", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_OK);
		
		verify(repositoryResourceService).stageChanges(any(), any());
	}

	@Test
	public void unstageChangesAnonymousCanNotUnstage() {
		String[] param = new String[] {};
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/unstage-changes", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void unstageChangesRepoNotFound() {
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		String[] param = new String[] {};
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/unstage-changes", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void unstageChangesCanNotWriteRepo() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());
		
		String[] param = new String[] {};
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/unstage-changes", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void unstageChangesSuccess() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		String[] param = new String[] {};
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/unstage-changes", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_OK);
		
		verify(repositoryResourceService).unstageChanges(any(), any());
	}

	@Test
	public void commitAnonymousCanNotCommit() {
		CommitMessage param = new CommitMessage();
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/commits", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void commitMessageCanNotBlank() {		
		CommitMessage param = new CommitMessage();
		param.setValue("");

		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/commits", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.value", hasItem("提交信息不能为空"),
					"errors.value.size()", is(1));
	}

	@WithMockUser(username = "jack")
	@Test
	public void commitRepoNotFound() {
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		CommitMessage param = new CommitMessage();
		param.setValue("first commit");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/commits", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@WithMockUser(username = "other")
	@Test
	public void commitCanNotWriteRepo() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.empty());

		CommitMessage param = new CommitMessage();
		param.setValue("first commit");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/commits", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN);
	}

	@WithMockUser(username = "jack")
	@Test
	public void commitWhenNoChangedFiles() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		UserInfo loginUser = new UserInfo();
		loginUser.setId(1);
		loginUser.setLoginName("other");
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(loginUser));
		
		CommitMessage param = new CommitMessage();
		param.setValue("first commit");
		
		when(repositoryResourceService.commit(any(), any(), anyString())).thenThrow(GitEmptyCommitException.class);
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/commits", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.globalErrors", hasItem("没有发现变更的文件"),
					"errors.globalErrors.size()", is(1));
		
		verify(repositoryResourceService).commit(any(), any(), anyString());
	}
	
	@WithMockUser(username = "jack")
	@Test
	public void commitSuccess() {
		Repository repository = new Repository();
		repository.setId(1);
		when(repositoryService.find(anyString(), anyString())).thenReturn(Optional.of(repository));
		
		when(repositoryPermissionService.canWrite(any(), any())).thenReturn(Optional.of(AccessLevel.WRITE));
		
		UserInfo loginUser = new UserInfo();
		loginUser.setId(1);
		loginUser.setLoginName("other");
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(loginUser));
		
		CommitMessage param = new CommitMessage();
		param.setValue("first commit");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/repos/{owner}/{repoName}/commits", "jack", "repo")
		.then()
			.statusCode(HttpStatus.SC_OK);
		
		verify(repositoryResourceService).commit(any(), any(), anyString());
	}

}
