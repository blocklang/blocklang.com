package com.blocklang.release.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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

import com.blocklang.release.constant.ReleaseMethod;
import com.blocklang.release.data.RegistrationInfo;
import com.blocklang.release.model.App;
import com.blocklang.release.model.AppRelease;
import com.blocklang.release.model.AppReleaseFile;
import com.blocklang.release.service.AppReleaseFileService;
import com.blocklang.release.service.AppReleaseRelationService;
import com.blocklang.release.service.AppReleaseService;
import com.blocklang.release.service.AppService;
import com.blocklang.release.service.InstallerService;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(InstallerApi.class)
public class InstallerApiTest {
	
	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private AppService appService;
	
	@MockBean
	private AppReleaseService appReleaseService;
	
	@MockBean
	private AppReleaseFileService appReleaseFileService;
	
	@MockBean
	private AppReleaseRelationService appReleaseRelationService;
	
	@MockBean
	private InstallerService installerService;
	
	@Before
	public void setUp() {
		RestAssuredMockMvc.mockMvc(mvc);
	}
	
	// 对输入参数进行校验
	@Test
	public void post_installer_param_not_valid() {
		RegistrationInfo registration = new RegistrationInfo();
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.post("/installers")
		.then()
			.statusCode(HttpStatus.SC_BAD_REQUEST);
	}
	
	// 根据注册 token 没有找到 APP
	@Test
	public void post_installer_no_content_no_register_token() {
		String registrationToken = "not_exist_register_token";
		RegistrationInfo registration = prepareParam(registrationToken);
		
		when(appService.findByRegistratioToken(eq(registrationToken))).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.post("/installers")
		.then()
			.statusCode(204);
	}
	
	// 没有找到 APP 的发行版
	@Test
	public void post_installer_no_content_no_release_app() {
		String registrationToken = "exist_register_token";
		RegistrationInfo registration = prepareParam(registrationToken);
		
		App app = new App();
		app.setId(1);
		app.setRegistrationToken(registrationToken);
		when(appService.findByRegistratioToken(eq(registrationToken))).thenReturn(Optional.of(app));
		
		when(appReleaseService.findLatestReleaseApp(app.getId())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.post("/installers")
		.then()
			.statusCode(204);
	}
	
	@Test
	public void post_installer_success() throws Exception {
		String registrationToken = "exist_register_token";
		RegistrationInfo registration = prepareParam(registrationToken);
		
		// 获取 APP 基本信息
		App app = new App();
		app.setId(1);
		app.setAppName("App Name");
		app.setRegistrationToken(registrationToken);
		when(appService.findByRegistratioToken(eq(registrationToken))).thenReturn(Optional.of(app));
		// 获取 APP 发行版信息
		AppRelease appRelease = new AppRelease();
		int appReleaseId = 1;
		appRelease.setId(appReleaseId);
		appRelease.setVersion("0.1.0");
		appRelease.setTitle("Title");
		appRelease.setDescription("Description");
		appRelease.setReleaseTime(LocalDateTime.now());
		appRelease.setReleaseMethod(ReleaseMethod.AUTO);
		when(appReleaseService.findLatestReleaseApp(eq(app.getId()))).thenReturn(Optional.of(appRelease));
		// 获取 installer token
		String installerToken = "installer_token";
		when(installerService.save(any())).thenReturn(installerToken);
		// 获取适配操作系统和架构的发行版文件
		AppReleaseFile appReleaseFile = new AppReleaseFile();
		appReleaseFile.setFileName("app_window_x86.jar");
		when(appReleaseFileService.find(eq(appReleaseId), anyString(), anyString())).thenReturn(Optional.of(appReleaseFile));
		// 获取依赖的软件列表，因为这里的软件只依赖 JDK，所以最好提供获取单个依赖的方法
		int dependAppReleaseId = 2;
		when(appReleaseRelationService.findSingle(eq(appReleaseId))).thenReturn(Optional.of(dependAppReleaseId));
		// 获取依赖 APP 的发行版基本信息
		AppRelease dependAppRelease = new AppRelease();
		dependAppRelease.setId(dependAppReleaseId);
		dependAppRelease.setAppId(2);
		dependAppRelease.setVersion("1.1.1");
		when(appReleaseService.find(eq(dependAppReleaseId))).thenReturn(Optional.of(dependAppRelease));
		// 获取依赖 APP 的基本信息
		App dependApp = new App();
		dependApp.setId(2);
		dependApp.setAppName("depend_app");
		when(appService.find(anyInt())).thenReturn(Optional.of(dependApp));
		// 获取依赖 APP 的发行版文件信息
		AppReleaseFile dependAppReleaseFile = new AppReleaseFile();
		dependAppReleaseFile.setFileName("depend_app_window_x86.jar");
		when(appReleaseFileService.find(eq(dependAppReleaseId), anyString(), anyString())).thenReturn(Optional.of(dependAppReleaseFile));
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.post("/installers")
		.then()
			.statusCode(201)
			.body(
				"installerToken", equalTo(installerToken), 
				"appRunPort", is(80),
				"appName", equalTo("App Name"),
				"appVersion", equalTo("0.1.0"),
				"appFileName", equalTo("app_window_x86.jar"),
				"jdkName", equalTo("depend_app"),
				"jdkVersion", equalTo("1.1.1"),
				"jdkFileName", equalTo("depend_app_window_x86.jar"));
	}
	
	private RegistrationInfo prepareParam(String registrationToken) {
		RegistrationInfo registration = new RegistrationInfo();
		registration.setRegistrationToken(registrationToken);
		registration.setServerToken("server_token");
		registration.setTargetOs("windows");
		registration.setOsType("windows");
		registration.setOsVersion("11");
		registration.setIp("10.10.10.10");
		registration.setArch("X86");
		return registration;
	}
}
