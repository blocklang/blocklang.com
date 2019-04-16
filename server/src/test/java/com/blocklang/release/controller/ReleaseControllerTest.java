package com.blocklang.release.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import com.blocklang.core.model.UserInfo;
import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.service.ProjectService;
import com.blocklang.release.constant.ReleaseResult;
import com.blocklang.release.data.CheckReleaseVersionParam;
import com.blocklang.release.data.NewReleaseTaskParam;
import com.blocklang.release.model.ProjectReleaseTask;
import com.blocklang.release.model.ProjectTag;
import com.blocklang.release.service.BuildService;
import com.blocklang.release.service.ProjectReleaseTaskService;
import com.blocklang.release.service.ProjectTagService;

import io.restassured.http.ContentType;

@WebMvcTest(ReleaseController.class)
public class ReleaseControllerTest extends AbstractControllerTest{

	@MockBean
	private ProjectService projectService;
	@MockBean
	private ProjectReleaseTaskService projectReleaseTaskService;
	@MockBean
	private ProjectTagService projectTagService;
	@MockBean
	private BuildService buildService;
	
	@WithMockUser(username = "owner")
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
			.body("errors.version", hasItems("版本号不能为空"), 
				  "errors.title", hasItems("发行版标题不能为空"),
				  "errors.jdkReleaseId", hasItems("必须选择 JDK 版本"));
	}
	
	@WithMockUser(username = "owner")
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
			.body("errors.version", hasItems("不是有效的<a href=\"https://semver.org/lang/zh-CN/\" target=\"_blank\">语义化版本</a>"));
	}	
	
	@WithMockUser(username = "owner")
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
	
	@WithMockUser(username = "owner")
	@Test
	public void post_release_version_is_used() {
		NewReleaseTaskParam release = prepareNewParam();
		
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("demo_project");
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		ProjectTag projectTag = new ProjectTag();
		when(projectTagService.find(anyInt(), anyString())).thenReturn(Optional.of(projectTag));
		
		ProjectTag latestTag = new ProjectTag();
		latestTag.setVersion("0.1.1");
		when(projectTagService.findLatestTag(anyInt())).thenReturn(Optional.of(latestTag));
		
		given()
			.contentType(ContentType.JSON)
			.body(release)
		.when()
			.post("/projects/{owner}/{projectName}/releases", "jack", "demo_project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.version", hasItems("版本号<strong>0.1.0</strong>已被占用，最新发布的版本号是<strong>0.1.1</strong>"));
	}
	
	@WithMockUser(username = "owner")
	@Test
	public void post_release_version_not_greater_than_previous() {
		NewReleaseTaskParam release = prepareNewParam();
		
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("demo_project");
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
			.body("errors.version", hasItems("要大于最新发布的版本号<strong>0.1.1</strong>"));
	}
	
	@WithMockUser(username = "owner")
	@Test
	public void post_release_started() throws IOException {
		NewReleaseTaskParam release = prepareNewParam();
		
		Project project = new Project();
		project.setId(2);
		project.setCreateUserName("jack");
		project.setName("demo_project");
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectTagService.find(anyInt(), anyString())).thenReturn(Optional.empty());
		
		UserInfo currentUser = new UserInfo();
		when(userService.findByLoginName(anyString())).thenReturn(Optional.of(currentUser));
		
		ProjectReleaseTask task = new ProjectReleaseTask();
		task.setId(1);
		task.setProjectId(project.getId());
		task.setVersion("0.1.0");
		task.setTitle("发行版标题");
		task.setDescription("发行版描述");
		task.setJdkReleaseId(3);
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
					"jdkReleaseId", is(3),
					"startTime", is(notNullValue()),
					"endTime", is(nullValue()),
					"releaseResult", equalTo(ReleaseResult.STARTED.getKey()));
		
		verify(buildService).asyncBuild(any(), any());
	}
	
	private NewReleaseTaskParam prepareNewParam() {
		NewReleaseTaskParam release = new NewReleaseTaskParam();
		release.setVersion("0.1.0");
		release.setTitle("发行版名称");
		release.setDescription("发行版描述");
		release.setJdkReleaseId(22);
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

	@Test
	public void list_release_that_project_is_not_exist() {
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/releases", "jack", "demo_project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}

	@Test
	public void list_release_success() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectReleaseTaskService.findAllByProjectId(anyInt())).thenReturn(Collections.emptyList());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/releases", "jack", "demo_project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", equalTo(0));
	}
	
	@Test
	public void check_version_user_is_unauthorization_not_login() {
		CheckReleaseVersionParam param = new CheckReleaseVersionParam();
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/releases/check-version", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_FORBIDDEN)
			.body(equalTo(""));
	}
	
	@WithMockUser(username = "owner")
	@Test
	public void check_version_is_blank() {
		CheckReleaseVersionParam param = new CheckReleaseVersionParam();
		param.setVersion(" "); // 有一个空格
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/releases/check-version", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.version", hasItem("版本号不能为空"),
					"errors.version.size()", is(1));
	}
	
	@WithMockUser(username = "owner")
	@Test
	public void check_version_is_not_a_valid_semantic_versioning() {
		CheckReleaseVersionParam param = new CheckReleaseVersionParam();
		param.setVersion("not-a-sematic-version");
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/releases/check-version", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.version", hasItem("不是有效的<a href=\"https://semver.org/lang/zh-CN/\" target=\"_blank\">语义化版本</a>"),
					"errors.version.size()", is(1));
	}
	
	@WithMockUser(username = "owner")
	@Test
	public void check_version_is_used() {
		CheckReleaseVersionParam param = new CheckReleaseVersionParam();
		param.setVersion("0.1.0");
		
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("demo_project");
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		ProjectTag projectTag = new ProjectTag();
		when(projectTagService.find(anyInt(), anyString())).thenReturn(Optional.of(projectTag));
		
		ProjectTag latestTag = new ProjectTag();
		latestTag.setVersion("0.1.1");
		when(projectTagService.findLatestTag(anyInt())).thenReturn(Optional.of(latestTag));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/releases/check-version", "jack", "demo_project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.version", hasItems("版本号<strong>0.1.0</strong>已被占用，最新发布的版本号是<strong>0.1.1</strong>"));
	}
	
	@WithMockUser(username = "owner")
	@Test
	public void check_version_not_greater_than_previous() {
		CheckReleaseVersionParam param = new CheckReleaseVersionParam();
		param.setVersion("0.1.0");
		
		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("demo_project");
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		// 版本号没有被占用
		when(projectTagService.find(anyInt(), anyString())).thenReturn(Optional.empty());
		
		// 但版本号没有大于最新版本号
		ProjectTag tag = new ProjectTag();
		tag.setId(1);
		tag.setVersion("0.1.1");
		when(projectTagService.findLatestTag(anyInt())).thenReturn(Optional.of(tag));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/releases/check-version", "jack", "demo_project")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.version", hasItems("要大于最新发布的版本号<strong>0.1.1</strong>"));
	}
	
	@WithMockUser(username = "owner")
	@Test
	public void check_version_pass() {
		CheckReleaseVersionParam param = new CheckReleaseVersionParam();
		param.setVersion("0.1.0");

		Project project = new Project();
		project.setId(1);
		project.setCreateUserName("jack");
		project.setName("demo_project");
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		// 版本号没有被占用
		when(projectTagService.find(anyInt(), anyString())).thenReturn(Optional.empty());
		
		// 但版本号没有大于最新版本号
		ProjectTag tag = new ProjectTag();
		tag.setId(1);
		tag.setVersion("0.0.1");
		when(projectTagService.findLatestTag(anyInt())).thenReturn(Optional.of(tag));
		
		given()
			.contentType(ContentType.JSON)
			.body(param)
		.when()
			.post("/projects/{owner}/{projectName}/releases/check-version", "jack", "demo_project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(equalTo("{}"));
	}

	@Test
	public void get_release_count_project_not_exist() {
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/stats/releases", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	// TODO: 只有有访问权限的用户，才能访问项目的统计数据
	// 如某些私有项目
	@Test
	public void get_release_count_success() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		when(projectReleaseTaskService.count(anyInt())).thenReturn(1l);
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/stats/releases", "jack", "project")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("total", equalTo(1));
	}
	
	@Test
	public void get_a_release_project_not_found() {
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/releases/{version}", "jack", "demo_project", "0.1.0")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void get_a_release_task_not_found() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectReleaseTaskService.findByProjectIdAndVersion(anyInt(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/releases/{version}", "jack", "demo_project", "0.1.0")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void get_a_release_success() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		ProjectReleaseTask task = new ProjectReleaseTask();
		task.setProjectId(1);
		task.setId(1);
		task.setVersion("0.1.0");
		when(projectReleaseTaskService.findByProjectIdAndVersion(anyInt(), anyString())).thenReturn(Optional.of(task));
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/releases/{version}", "jack", "demo_project", "0.1.0")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("id", equalTo(1),
					"version", equalTo("0.1.0"));
	}
	
	
	@Test
	public void get_a_release_log_then_project_not_found() {
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.empty());

		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/releases/{version}/log", "jack", "demo_project", "0.1.0")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void get_a_release_log_then_task_not_found() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		when(projectReleaseTaskService.findByProjectIdAndVersion(anyInt(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/releases/{version}/log", "jack", "demo_project", "0.1.0")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void get_a_release_log_then_log_file_not_found() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		ProjectReleaseTask task = new ProjectReleaseTask();
		task.setProjectId(1);
		task.setId(1);
		task.setVersion("0.1.0");
		when(projectReleaseTaskService.findByProjectIdAndVersion(anyInt(), anyString())).thenReturn(Optional.of(task));
		
		when(propertyService.findStringValue(anyString())).thenReturn(Optional.of("xx"));

		// 如果如果没有找到日志文件，则只在系统日志中打印 warn 信息，但不要抛出异常，而是返回一个空列表
		when(projectReleaseTaskService.getLogContent(any(), anyInt())).thenReturn(Collections.emptyList());
		
		given()
			.contentType(ContentType.JSON)
		.when()
			.get("/projects/{owner}/{projectName}/releases/{version}/log", "jack", "demo_project", "0.1.0")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", equalTo(0));
	}

	@Test
	public void get_a_release_log_has_line_num_success() throws IOException {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		ProjectReleaseTask task = new ProjectReleaseTask();
		task.setProjectId(1);
		task.setId(1);
		task.setVersion("0.1.0");
		when(projectReleaseTaskService.findByProjectIdAndVersion(anyInt(), anyString())).thenReturn(Optional.of(task));
		
		when(propertyService.findStringValue(anyString())).thenReturn(Optional.of("xx"));
		
		when(projectReleaseTaskService.getLogContent(any(), anyInt())).thenReturn(Arrays.asList("a", "b"));
		
		given()
			.contentType(ContentType.JSON)
			.params("end_line", "2")
		.when()
			.get("/projects/{owner}/{projectName}/releases/{version}/log", "jack", "demo_project", "0.1.0")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", equalTo(2));
	}
	
	@Test
	public void get_a_release_log_has_no_line_num_success() {
		Project project = new Project();
		project.setId(1);
		when(projectService.find(anyString(), anyString())).thenReturn(Optional.of(project));
		
		ProjectReleaseTask task = new ProjectReleaseTask();
		task.setProjectId(1);
		task.setId(1);
		task.setVersion("0.1.0");
		when(projectReleaseTaskService.findByProjectIdAndVersion(anyInt(), anyString())).thenReturn(Optional.of(task));

		when(propertyService.findStringValue(anyString())).thenReturn(Optional.of("xx"));
		
		when(projectReleaseTaskService.getLogContent(any(), anyInt())).thenReturn(Arrays.asList("a", "b"));
		
		given()
			.contentType(ContentType.JSON)
			.params("end_line", "2")
		.when()
			.get("/projects/{owner}/{projectName}/releases/{version}/log", "jack", "demo_project", "0.1.0")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body("size()", equalTo(2));
	}
}
