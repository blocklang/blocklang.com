package com.blocklang.release.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.blocklang.develop.model.Project;
import com.blocklang.develop.service.ProjectService;
import com.blocklang.release.constant.ReleaseResult;
import com.blocklang.release.data.NewReleaseTaskParam;
import com.blocklang.release.model.ProjectReleaseTask;
import com.blocklang.release.model.ProjectTag;
import com.blocklang.release.service.BuildService;
import com.blocklang.release.service.ProjectReleaseTaskService;
import com.blocklang.release.service.ProjectTagService;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(ReleaseController.class)
public class ReleaseControllerTest {

	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private ProjectService projectService;
	
	@MockBean
	private ProjectReleaseTaskService projectReleaseTaskService;
	
	@MockBean
	private ProjectTagService projectTagService;
	
	@MockBean
	private BuildService buildService;
	
	@Before
	public void setUp() {
		RestAssuredMockMvc.mockMvc(mvc);
	}
	
	@Test
	public void post_release_param_is_blank() {
		NewReleaseTaskParam release = new NewReleaseTaskParam();
		given()
			.contentType(ContentType.JSON)
			.body(release)
		.when()
			.post("/projects/{owner}/{projectName}/releases", "jack", "demo_project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.version", hasItems("版本不能为空"), 
				  "errors.title", hasItems("发行版标题不能为空"),
				  "errors.jdkAppId", hasItems("必须选择 JDK 版本"));
	}
	
	@Test
	public void post_release_invalid_version() {
		NewReleaseTaskParam release = prepareNewParam();
		release.setVersion("not-a-sematic-version");
		
		given()
			.contentType(ContentType.JSON)
			.body(release)
		.when()
			.post("/projects/{owner}/{projectName}/releases", "jack", "demo_project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.version", hasItems("不是有效的语义化版本"));
	}	
	
	@Test
	public void post_release_project_not_exist() {
		NewReleaseTaskParam release = prepareNewParam();
		
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(release)
		.when()
			.post("/projects/{owner}/{projectName}/releases", "jack", "demo_project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void post_release_version_is_used() {
		NewReleaseTaskParam release = prepareNewParam();
		
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setProjectName("demo_project");
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		ProjectTag projectTag = new ProjectTag();
		when(projectTagService.find(anyInt(), anyString())).thenReturn(Optional.of(projectTag));
		
		given()
			.contentType(ContentType.JSON)
			.body(release)
		.when()
			.post("/projects/{owner}/{projectName}/releases", "jack", "demo_project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.version", hasItems("版本号 0.1.0 已被占用"));
	}
	
	@Test
	public void post_release_version_not_greater_than_previous() {
		NewReleaseTaskParam release = prepareNewParam();
		
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setProjectName("demo_project");
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectTagService.find(anyInt(), anyString())).thenReturn(Optional.empty());
		
		ProjectTag tag = new ProjectTag();
		tag.setId(1);
		tag.setVersion("0.1.1");
		when(projectTagService.findLatestTag(anyInt())).thenReturn(Optional.of(tag));
		
		given()
			.contentType(ContentType.JSON)
			.body(release)
		.when()
			.post("/projects/{owner}/{projectName}/releases", "jack", "demo_project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.version", hasItems("版本号应大于项目最新的版本号，但 0.1.0 没有大于 0.1.1"));
	}
	
	@Test
	public void post_release_started() throws IOException {
		NewReleaseTaskParam release = prepareNewParam();
		
		Project project = new Project();
		project.setId(2);
		project.setCreateUserName("jack");
		project.setProjectName("demo_project");
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectTagService.find(anyInt(), anyString())).thenReturn(Optional.empty());
		
		ProjectReleaseTask task = new ProjectReleaseTask();
		task.setId(1);
		task.setProjectId(project.getId());
		task.setVersion("0.1.0");
		task.setTitle("发行版标题");
		task.setDescription("发行版描述");
		task.setJdkAppId(3);
		task.setStartTime(LocalDateTime.now());
		task.setReleaseResult(ReleaseResult.STARTED);
		when(projectReleaseTaskService.save(any())).thenReturn(task);
		
		given()
			.contentType(ContentType.JSON)
			.body(release)
		.when()
			.post("/projects/{owner}/{projectName}/releases", "jack", "demo_project")
		.then()
			.statusCode(HttpStatus.SC_CREATED)
			.body("id", is(1),
					"projectId", is(2),
					"version", equalTo("0.1.0"),
					"title", equalTo("发行版标题"),
					"description", equalTo("发行版描述"),
					"jdkAppId", is(3),
					"startTime", is(notNullValue()),
					"endTime", is(nullValue()),
					"releaseResult", equalTo(ReleaseResult.STARTED.getKey()));
		
		verify(buildService).build(any(), any());
	}
	
	private NewReleaseTaskParam prepareNewParam() {
		NewReleaseTaskParam release = new NewReleaseTaskParam();
		release.setVersion("0.1.0");
		release.setTitle("发行版名称");
		release.setDescription("发行版描述");
		release.setJdkAppId(22);
		return release;
	}
//	
//	@Test
//	public void post_release_git_tag_failed() {
//		NewReleaseParam release = prepareNewParam();
//		
//		Project project = new Project();
//		project.setId(1);
//		project.setCreateUserName("jack");
//		project.setProjectName("demo_project");
//		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
//		
//		when(projectTagService.find(anyInt(), anyString())).thenReturn(Optional.empty());
//		
//		when(gitToolService.tag(any())).thenReturn(Optional.empty());
//		
//		given()
//			.contentType(ContentType.JSON)
//			.body(release)
//		.when()
//			.post("/projects/{owner}/{projectName}/releases", "jack", "demo_project")
//		.then()
//			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
//			.body("errors.globalErrors", hasItems("为 Git 仓库添加附注标签失败"));
//	}
//	
//	@Test
//	public void post_release_git_tag_success() {
//		NewReleaseParam release = prepareNewParam();
//		
//		Project project = new Project();
//		project.setId(1);
//		project.setCreateUserName("jack");
//		project.setProjectName("demo_project");
//		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
//		
//		when(projectTagService.find(anyInt(), anyString())).thenReturn(Optional.empty());
//		
//		String tagId = "i-am-git-tag-object-id";
//		when(gitToolService.tag(any())).thenReturn(Optional.of(tagId));
//		
//		ProjectTag projectTag = new ProjectTag();
//		projectTag.setId(1);
//		when(projectTagService.save(any())).thenReturn(projectTag);
//		
//		given()
//			.contentType(ContentType.JSON)
//			.body(release)
//		.when()
//			.post("/projects/{owner}/{projectName}/releases", "jack", "demo_project");
//		
//		verify(projectTagService).save(any());
//		verify(projectBuildService).save(any());
//	}
//	
//	@Test
//	public void post_release_build_success() {
//		NewReleaseParam release = prepareNewParam();
//		
//		Project project = new Project();
//		project.setId(1);
//		project.setCreateUserName("jack");
//		project.setProjectName("demo_project");
//		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
//		
//		when(projectTagService.find(anyInt(), anyString())).thenReturn(Optional.empty());
//		
//		ProjectTag projectTag = new ProjectTag();
//		projectTag.setId(2);
//		when(projectTagService.save(any())).thenReturn(projectTag);
//		
//		ProjectBuild projectBuild = new ProjectBuild();
//		projectBuild.setId(3);
//		when(projectBuildService.save(any())).thenReturn(projectBuild);
//		
//		when(buildToolService.runNpmInstall(any())).thenReturn(true);
//		when(buildToolService.runDojoBuild(any())).thenReturn(true);
//		when(buildToolService.copyDojoDistToSpringBoot(any())).thenReturn(true);
//		when(buildToolService.runMavenInstall(any())).thenReturn(true);
//		
//		given()
//			.contentType(ContentType.JSON)
//			.body(release)
//		.when()
//			.post("/projects/{owner}/{projectName}/releases", "jack", "demo_project")
//		.then()
//			// TODO：这里使用此 code，感觉不妥，考虑代码中如何更好的表现此逻辑
//			.statusCode(HttpStatus.SC_CREATED)
//			.body("id", is(3), 
//				  "version", equalTo("0.1.0"), 
//				  "name", equalTo("发行版名称"),
//				  "description", equalTo("发行版描述"));
//	
//	}


}
