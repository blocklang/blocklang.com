package com.blocklang.release.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.blocklang.release.constant.ReleaseMethod;
import com.blocklang.release.data.NewRegistrationParam;
import com.blocklang.release.data.UpdateRegistrationParam;
import com.blocklang.release.model.App;
import com.blocklang.release.model.AppRelease;
import com.blocklang.release.model.AppReleaseFile;
import com.blocklang.release.model.Installer;
import com.blocklang.release.model.WebServer;
import com.blocklang.release.service.AppReleaseFileService;
import com.blocklang.release.service.AppReleaseRelationService;
import com.blocklang.release.service.AppReleaseService;
import com.blocklang.release.service.AppService;
import com.blocklang.release.service.InstallerService;
import com.blocklang.release.service.WebServerService;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(InstallerApi.class)
public class InstallerApiTest {
	
	// FIXME: 因为运行测试用例时，提示找不到 ResourceServerProperties bean，所以这里尝试mock一个
	@MockBean
	private ResourceServerProperties resourceServerProperties;
	
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
	
	@MockBean
	private WebServerService webServerService;
	
	@Before
	public void setUp() {
		RestAssuredMockMvc.mockMvc(mvc);
	}
	
	// 对输入参数进行校验
	@Test
	public void post_installer_param_not_valid() {
		NewRegistrationParam registration = new NewRegistrationParam();
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.post("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.registrationToken", hasItems("注册 Token 不能为空"));
	}
	
	// 根据注册 token 没有找到 APP
	@Test
	public void post_installer_no_register_token() {
		String registrationToken = "not_exist_register_token";
		NewRegistrationParam registration = prepareNewParam(registrationToken);
		
		when(appService.findByRegistrationToken(eq(registrationToken))).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.post("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.registrationToken", hasItems("注册 Token `not_exist_register_token` 不存在"));
	}
	
	// 注册 Token 存在，但没有找到 APP 的发行版
	// 出现这种情况，则是往数据库中写数据时逻辑有误。
	@Test
	public void post_installer_no_release_app() {
		String registrationToken = "exist_register_token";
		NewRegistrationParam registration = prepareNewParam(registrationToken);
		
		App app = new App();
		app.setId(1);
		app.setAppName("App Name");
		app.setRegistrationToken(registrationToken);
		when(appService.findByRegistrationToken(eq(registrationToken))).thenReturn(Optional.of(app));
		
		when(appReleaseService.findLatestReleaseApp(app.getId())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.post("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.globalErrors", hasItems("App Name 尚未发布"));
	}
	
	@Test
	public void post_installer_no_release_app_file() {
		String registrationToken = "exist_register_token";
		NewRegistrationParam registration = prepareNewParam(registrationToken);
		
		App app = new App();
		app.setId(1);
		app.setAppName("App Name");
		app.setRegistrationToken(registrationToken);
		when(appService.findByRegistrationToken(eq(registrationToken))).thenReturn(Optional.of(app));
		
		AppRelease appRelease = new AppRelease();
		appRelease.setId(1);
		appRelease.setAppId(1);
		when(appReleaseService.findLatestReleaseApp(app.getId())).thenReturn(Optional.of(appRelease));
		
		when(appReleaseFileService.find(anyInt(), anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.post("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.globalErrors", hasItems("App Name 兼容 windows X86 的发行版文件不存在"));
	}
	
	// 注意，依赖的 APP，是先找到 APP 发行版信息，然后再回去找 APP 基本信息
	@Test
	public void post_installer_no_depend_app_release_id() {
		String registrationToken = "exist_register_token";
		NewRegistrationParam registration = prepareNewParam(registrationToken);
		
		App app = new App();
		app.setId(1);
		app.setAppName("App Name");
		app.setRegistrationToken(registrationToken);
		when(appService.findByRegistrationToken(eq(registrationToken))).thenReturn(Optional.of(app));
		
		AppRelease appRelease = new AppRelease();
		appRelease.setId(1);
		appRelease.setAppId(1);
		when(appReleaseService.findLatestReleaseApp(app.getId())).thenReturn(Optional.of(appRelease));
		
		AppReleaseFile appReleaseFile = new AppReleaseFile();
		appReleaseFile.setId(1);
		appReleaseFile.setAppReleaseId(1);
		when(appReleaseFileService.find(anyInt(), anyString(), anyString())).thenReturn(Optional.of(appReleaseFile));
		
		when(appReleaseRelationService.findSingle(eq(1))).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.post("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.globalErrors", hasItems("App Name 依赖的 JDK 发行版信息不存在"));
	}
	
	@Test
	public void post_installer_no_depend_app_release() {
		String registrationToken = "exist_register_token";
		NewRegistrationParam registration = prepareNewParam(registrationToken);
		
		App app = new App();
		app.setId(1);
		app.setAppName("App Name");
		app.setRegistrationToken(registrationToken);
		when(appService.findByRegistrationToken(eq(registrationToken))).thenReturn(Optional.of(app));
		
		AppRelease appRelease = new AppRelease();
		appRelease.setId(1);
		appRelease.setAppId(1);
		when(appReleaseService.findLatestReleaseApp(app.getId())).thenReturn(Optional.of(appRelease));
		
		AppReleaseFile appReleaseFile = new AppReleaseFile();
		appReleaseFile.setId(1);
		appReleaseFile.setAppReleaseId(1);
		when(appReleaseFileService.find(anyInt(), anyString(), anyString())).thenReturn(Optional.of(appReleaseFile));
		
		when(appReleaseRelationService.findSingle(eq(1))).thenReturn(Optional.of(2));
		
		when(appReleaseService.findById(eq(2))).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.post("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.globalErrors", hasItems("App Name 依赖的 JDK 发行版信息不存在"));
	}
	
	@Test
	public void post_installer_no_depend_app_is_not_jdk() {
		String registrationToken = "exist_register_token";
		NewRegistrationParam registration = prepareNewParam(registrationToken);
		
		App app = new App();
		app.setId(1);
		app.setAppName("App Name");
		app.setRegistrationToken(registrationToken);
		when(appService.findByRegistrationToken(eq(registrationToken))).thenReturn(Optional.of(app));
		
		AppRelease appRelease = new AppRelease();
		appRelease.setId(1);
		appRelease.setAppId(1);
		when(appReleaseService.findLatestReleaseApp(app.getId())).thenReturn(Optional.of(appRelease));
		
		AppReleaseFile appReleaseFile = new AppReleaseFile();
		appReleaseFile.setId(1);
		appReleaseFile.setAppReleaseId(1);
		when(appReleaseFileService.find(anyInt(), anyString(), anyString())).thenReturn(Optional.of(appReleaseFile));
		
		when(appReleaseRelationService.findSingle(eq(1))).thenReturn(Optional.of(2));
		
		AppRelease dependAppRelease = new AppRelease();
		dependAppRelease.setId(2);
		dependAppRelease.setAppId(2);
		when(appReleaseService.findById(eq(2))).thenReturn(Optional.of(dependAppRelease));
		
		App dependApp = new App();
		dependApp.setId(2);
		dependApp.setAppName("xxxx");
		when(appService.findById(eq(2))).thenReturn(Optional.of(dependApp));
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.post("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			// 当前是根据文件名中是否包含 jdk 字样来确定的
			.body("errors.globalErrors", hasItems("App Name 的依赖不是有效的 JDK"));
	}
	
	@Test
	public void post_installer_no_depend_app_release_file() {
		String registrationToken = "exist_register_token";
		NewRegistrationParam registration = prepareNewParam(registrationToken);
		
		App app = new App();
		app.setId(1);
		app.setAppName("App Name");
		app.setRegistrationToken(registrationToken);
		when(appService.findByRegistrationToken(eq(registrationToken))).thenReturn(Optional.of(app));
		
		AppRelease appRelease = new AppRelease();
		appRelease.setId(1);
		appRelease.setAppId(1);
		when(appReleaseService.findLatestReleaseApp(app.getId())).thenReturn(Optional.of(appRelease));
		
		AppReleaseFile appReleaseFile = new AppReleaseFile();
		appReleaseFile.setId(1);
		appReleaseFile.setAppReleaseId(1);
		when(appReleaseFileService.find(anyInt(), anyString(), anyString())).thenReturn(Optional.of(appReleaseFile));
		
		when(appReleaseRelationService.findSingle(eq(1))).thenReturn(Optional.of(2));
		
		AppRelease dependAppRelease = new AppRelease();
		dependAppRelease.setId(2);
		dependAppRelease.setAppId(2);
		when(appReleaseService.findById(eq(2))).thenReturn(Optional.of(dependAppRelease));
		
		App dependApp = new App();
		dependApp.setId(2);
		dependApp.setAppName("jdk");
		when(appService.findById(eq(2))).thenReturn(Optional.of(dependApp));
		
		when(appReleaseFileService.find(eq(2), anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.post("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.globalErrors", hasItems("兼容 windows X86 的 JDK 安装文件不存在"));
	}
	
	@Test
	public void post_installer_success() throws Exception {
		String registrationToken = "exist_register_token";
		NewRegistrationParam registration = prepareNewParam(registrationToken);
		
		// 获取 APP 基本信息
		App app = new App();
		app.setId(1);
		app.setAppName("App Name");
		app.setRegistrationToken(registrationToken);
		when(appService.findByRegistrationToken(eq(registrationToken))).thenReturn(Optional.of(app));
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
		when(appReleaseService.findById(eq(dependAppReleaseId))).thenReturn(Optional.of(dependAppRelease));
		// 获取依赖 APP 的基本信息
		App dependApp = new App();
		dependApp.setId(2);
		dependApp.setAppName("jdk_app");
		when(appService.findById(anyInt())).thenReturn(Optional.of(dependApp));
		// 获取依赖 APP 的发行版文件信息
		AppReleaseFile dependAppReleaseFile = new AppReleaseFile();
		dependAppReleaseFile.setFileName("jdk_app_window_x86.jar");
		when(appReleaseFileService.find(eq(dependAppReleaseId), anyString(), anyString())).thenReturn(Optional.of(dependAppReleaseFile));
		
		// 获取 installer token
		String installerToken = "installer_token";
		when(installerService.save(any(), anyInt())).thenReturn(installerToken);
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.post("/installers")
		.then()
			.statusCode(HttpStatus.SC_CREATED)
			.body(
				"installerToken", equalTo(installerToken), 
				"appRunPort", is(80),
				"appName", equalTo("App Name"),
				"appVersion", equalTo("0.1.0"),
				"appFileName", equalTo("app_window_x86.jar"),
				"jdkName", equalTo("jdk_app"),
				"jdkVersion", equalTo("1.1.1"),
				"jdkFileName", equalTo("jdk_app_window_x86.jar"));
	}
	
	private NewRegistrationParam prepareNewParam(String registrationToken) {
		NewRegistrationParam registration = new NewRegistrationParam();
		registration.setRegistrationToken(registrationToken);
		registration.setServerToken("server_token");
		registration.setTargetOs("windows");
		registration.setOsType("windows");
		registration.setOsVersion("11");
		registration.setIp("10.10.10.10");
		registration.setArch("X86");
		return registration;
	}

	// 对输入参数进行校验
	@Test
	public void put_installer_param_not_valid() {
		UpdateRegistrationParam registration = new UpdateRegistrationParam();
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.put("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.installerToken", hasItems("安装器 Token 不能为空"));
	}

	// 根据 installer token 没有找到 installer 信息
	@Test
	public void put_installer_no_installer_token() {
		String installerToken = "installer_token_1";
		UpdateRegistrationParam registration = prepareUpdateParam(installerToken);
		
		when(installerService.findByInstallerToken(eq(installerToken))).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.put("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.installerToken", hasItems("安装器 Token `installer_token_1` 不存在"));
	}
	
	@Test
	public void put_installer_server_token_changed() {
		String installerToken = "installer_token_1";
		UpdateRegistrationParam registration = prepareUpdateParam(installerToken);
		
		Installer installer = new Installer();
		installer.setId(1);
		installer.setWebServerId(1);
		when(installerService.findByInstallerToken(eq(installerToken))).thenReturn(Optional.of(installer));
		
		WebServer webServer = new WebServer();
		webServer.setId(1);
		webServer.setServerToken("origin_server_token");
		when(webServerService.findById(eq(installer.getWebServerId()))).thenReturn(Optional.of(webServer));
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.put("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.serverToken", hasItems("服务器标识被篡改。注册的服务器标识是 `origin_server_token`，但本次升级传入的服务器标识是 `server_token`"));
	}
	
	@Test
	public void put_installer_no_app_release() {
		String installerToken = "installer_token_1";
		UpdateRegistrationParam registration = prepareUpdateParam(installerToken);
		
		Installer installer = new Installer();
		installer.setId(1);
		installer.setAppReleaseId(1);
		when(installerService.findByInstallerToken(eq(installerToken))).thenReturn(Optional.of(installer));
		
		WebServer webServer = new WebServer();
		webServer.setId(1);
		webServer.setServerToken("server_token");
		when(webServerService.findById(eq(installer.getWebServerId()))).thenReturn(Optional.of(webServer));
		
		when(appReleaseService.findById(anyInt())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.put("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.globalErrors", hasItems("根据发行版 ID 没有找到发行版信息"));
	}
	
	@Test
	public void put_installer_no_app() {
		String installerToken = "installer_token_1";
		UpdateRegistrationParam registration = prepareUpdateParam(installerToken);
		
		Installer installer = new Installer();
		installer.setId(1);
		installer.setAppReleaseId(1);
		when(installerService.findByInstallerToken(eq(installerToken))).thenReturn(Optional.of(installer));
		
		WebServer webServer = new WebServer();
		webServer.setId(1);
		webServer.setServerToken("server_token");
		when(webServerService.findById(eq(installer.getWebServerId()))).thenReturn(Optional.of(webServer));
		
		AppRelease currentAppRelease = new AppRelease();
		currentAppRelease.setAppId(1);
		currentAppRelease.setId(1);
		when(appReleaseService.findById(anyInt())).thenReturn(Optional.of(currentAppRelease));
		
		when(appService.findById(1)).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.put("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.globalErrors", hasItems("没有找到 APP"));
	}

	@Test
	public void put_installer_no_latest_release_app() {
		String installerToken = "installer_token_1";
		UpdateRegistrationParam registration = prepareUpdateParam(installerToken);
		
		Installer installer = new Installer();
		installer.setId(1);
		installer.setAppReleaseId(1);
		when(installerService.findByInstallerToken(eq(installerToken))).thenReturn(Optional.of(installer));
		
		WebServer webServer = new WebServer();
		webServer.setId(1);
		webServer.setServerToken("server_token");
		when(webServerService.findById(eq(installer.getWebServerId()))).thenReturn(Optional.of(webServer));
		
		AppRelease currentAppRelease = new AppRelease();
		currentAppRelease.setId(1);
		currentAppRelease.setAppId(1);
		when(appReleaseService.findById(anyInt())).thenReturn(Optional.of(currentAppRelease));
		
		App app = new App();
		app.setAppName("App Name");
		app.setId(1);
		when(appService.findById(1)).thenReturn(Optional.of(app));
		
		when(appReleaseService.findLatestReleaseApp(anyInt())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.put("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.globalErrors", hasItems("App Name 尚未发布"));
	}
	
	@Test
	public void put_installer_no_release_app_file() {
		String installerToken = "installer_token_1";
		UpdateRegistrationParam registration = prepareUpdateParam(installerToken);
		
		Installer installer = new Installer();
		installer.setId(1);
		installer.setAppReleaseId(1);
		when(installerService.findByInstallerToken(eq(installerToken))).thenReturn(Optional.of(installer));
		
		WebServer webServer = new WebServer();
		webServer.setId(1);
		webServer.setServerToken("server_token");
		when(webServerService.findById(eq(installer.getWebServerId()))).thenReturn(Optional.of(webServer));
		
		AppRelease currentAppRelease = new AppRelease();
		currentAppRelease.setId(1);
		currentAppRelease.setAppId(1);
		when(appReleaseService.findById(anyInt())).thenReturn(Optional.of(currentAppRelease));
		
		App app = new App();
		app.setAppName("App Name");
		app.setId(1);
		when(appService.findById(1)).thenReturn(Optional.of(app));
		
		int latestAppReleaseId = 2;
		AppRelease latestAppRelease = new AppRelease();
		latestAppRelease.setId(latestAppReleaseId);
		when(appReleaseService.findLatestReleaseApp(anyInt())).thenReturn(Optional.of(latestAppRelease));
		
		// 获取适配操作系统和架构的发行版文件
		when(appReleaseFileService.find(eq(latestAppReleaseId), anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.put("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.globalErrors", hasItems("App Name 兼容 windows X86 的发行版文件不存在"));
	}
	
	// 注意，依赖的 APP，是先找到 APP 发行版信息，然后再回去找 APP 基本信息
	@Test
	public void put_installer_no_depend_app_release_id() {
		String installerToken = "installer_token_1";
		UpdateRegistrationParam registration = prepareUpdateParam(installerToken);
		
		Installer installer = new Installer();
		installer.setId(1);
		installer.setAppReleaseId(1);
		when(installerService.findByInstallerToken(eq(installerToken))).thenReturn(Optional.of(installer));
		
		WebServer webServer = new WebServer();
		webServer.setId(1);
		webServer.setServerToken("server_token");
		when(webServerService.findById(eq(installer.getWebServerId()))).thenReturn(Optional.of(webServer));
		
		AppRelease currentAppRelease = new AppRelease();
		currentAppRelease.setId(1);
		currentAppRelease.setAppId(1);
		when(appReleaseService.findById(anyInt())).thenReturn(Optional.of(currentAppRelease));
		
		App app = new App();
		app.setAppName("App Name");
		app.setId(1);
		when(appService.findById(1)).thenReturn(Optional.of(app));
		
		int latestAppReleaseId = 2;
		AppRelease latestAppRelease = new AppRelease();
		latestAppRelease.setId(latestAppReleaseId);
		when(appReleaseService.findLatestReleaseApp(anyInt())).thenReturn(Optional.of(latestAppRelease));
		
		// 获取适配操作系统和架构的发行版文件
		AppReleaseFile latestAppReleaseFile = new AppReleaseFile();
		latestAppReleaseFile.setAppReleaseId(latestAppRelease.getId());
		latestAppReleaseFile.setFileName("app_window_x86.jar");
		when(appReleaseFileService.find(eq(latestAppReleaseId), anyString(), anyString()))
			.thenReturn(Optional.of(latestAppReleaseFile));
		
		// 获取依赖的软件列表，因为这里的软件只依赖 JDK，所以最好提供获取单个依赖的方法
		when(appReleaseRelationService.findSingle(eq(latestAppReleaseId))).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.put("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.globalErrors", hasItems("App Name 依赖的 JDK 发行版信息不存在"));
	}
	
	@Test
	public void put_installer_no_depend_app_release() {
		String installerToken = "installer_token_1";
		UpdateRegistrationParam registration = prepareUpdateParam(installerToken);
		
		Installer installer = new Installer();
		installer.setId(1);
		installer.setAppReleaseId(1);
		when(installerService.findByInstallerToken(eq(installerToken))).thenReturn(Optional.of(installer));
		
		WebServer webServer = new WebServer();
		webServer.setId(1);
		webServer.setServerToken("server_token");
		when(webServerService.findById(eq(installer.getWebServerId()))).thenReturn(Optional.of(webServer));
		
		AppRelease currentAppRelease = new AppRelease();
		currentAppRelease.setId(1);
		currentAppRelease.setAppId(1);
		when(appReleaseService.findById(anyInt())).thenReturn(Optional.of(currentAppRelease));
		
		App app = new App();
		app.setAppName("App Name");
		app.setId(1);
		when(appService.findById(1)).thenReturn(Optional.of(app));
		
		int latestAppReleaseId = 2;
		AppRelease latestAppRelease = new AppRelease();
		latestAppRelease.setId(latestAppReleaseId);
		when(appReleaseService.findLatestReleaseApp(anyInt())).thenReturn(Optional.of(latestAppRelease));
		
		// 获取适配操作系统和架构的发行版文件
		AppReleaseFile latestAppReleaseFile = new AppReleaseFile();
		latestAppReleaseFile.setAppReleaseId(latestAppRelease.getId());
		latestAppReleaseFile.setFileName("app_window_x86.jar");
		when(appReleaseFileService.find(eq(latestAppReleaseId), anyString(), anyString()))
			.thenReturn(Optional.of(latestAppReleaseFile));
		
		// 获取依赖的软件列表，因为这里的软件只依赖 JDK，所以最好提供获取单个依赖的方法
		int dependAppReleaseId = 3;
		when(appReleaseRelationService.findSingle(eq(latestAppReleaseId))).thenReturn(Optional.of(dependAppReleaseId));
		
		// 获取依赖 APP 的发行版基本信息
		when(appReleaseService.findById(eq(dependAppReleaseId))).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.put("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.globalErrors", hasItems("App Name 依赖的 JDK 发行版信息不存在"));
	}
	
	@Test
	public void put_installer_no_depend_app_is_not_jdk() {
		String installerToken = "installer_token_1";
		UpdateRegistrationParam registration = prepareUpdateParam(installerToken);
		
		Installer installer = new Installer();
		installer.setId(1);
		installer.setAppReleaseId(1);
		when(installerService.findByInstallerToken(eq(installerToken))).thenReturn(Optional.of(installer));
		
		WebServer webServer = new WebServer();
		webServer.setId(1);
		webServer.setServerToken("server_token");
		when(webServerService.findById(eq(installer.getWebServerId()))).thenReturn(Optional.of(webServer));
		
		AppRelease currentAppRelease = new AppRelease();
		currentAppRelease.setId(1);
		currentAppRelease.setAppId(1);
		when(appReleaseService.findById(anyInt())).thenReturn(Optional.of(currentAppRelease));
		
		App app = new App();
		app.setAppName("App Name");
		app.setId(1);
		when(appService.findById(1)).thenReturn(Optional.of(app));
		
		int latestAppReleaseId = 2;
		AppRelease latestAppRelease = new AppRelease();
		latestAppRelease.setId(latestAppReleaseId);
		when(appReleaseService.findLatestReleaseApp(anyInt())).thenReturn(Optional.of(latestAppRelease));
		
		// 获取适配操作系统和架构的发行版文件
		AppReleaseFile latestAppReleaseFile = new AppReleaseFile();
		latestAppReleaseFile.setAppReleaseId(latestAppRelease.getId());
		latestAppReleaseFile.setFileName("app_window_x86.jar");
		when(appReleaseFileService.find(eq(latestAppReleaseId), anyString(), anyString()))
			.thenReturn(Optional.of(latestAppReleaseFile));
		
		// 获取依赖的软件列表，因为这里的软件只依赖 JDK，所以最好提供获取单个依赖的方法
		int dependAppReleaseId = 3;
		when(appReleaseRelationService.findSingle(eq(latestAppReleaseId))).thenReturn(Optional.of(dependAppReleaseId));
		
		// 获取依赖 APP 的发行版基本信息
		AppRelease dependAppRelease = new AppRelease();
		dependAppRelease.setId(dependAppReleaseId);
		dependAppRelease.setAppId(2);
		dependAppRelease.setVersion("0.1.2");
		when(appReleaseService.findById(eq(dependAppReleaseId))).thenReturn(Optional.of(dependAppRelease));
		
		// 获取依赖 APP 的基本信息
		when(appService.findById(eq(2))).thenReturn(Optional.empty());
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.put("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			// 当前是根据文件名中是否包含 jdk 字样来确定的
			.body("errors.globalErrors", hasItems("App Name 的依赖不是有效的 JDK"));
	}
	
	@Test
	public void put_installer_no_depend_app_release_file() {
		String installerToken = "installer_token_1";
		UpdateRegistrationParam registration = prepareUpdateParam(installerToken);
		
		Installer installer = new Installer();
		installer.setId(1);
		installer.setAppReleaseId(1);
		when(installerService.findByInstallerToken(eq(installerToken))).thenReturn(Optional.of(installer));
		
		WebServer webServer = new WebServer();
		webServer.setId(1);
		webServer.setServerToken("server_token");
		when(webServerService.findById(eq(installer.getWebServerId()))).thenReturn(Optional.of(webServer));
		
		AppRelease currentAppRelease = new AppRelease();
		currentAppRelease.setId(1);
		currentAppRelease.setAppId(1);
		when(appReleaseService.findById(anyInt())).thenReturn(Optional.of(currentAppRelease));
		
		App app = new App();
		app.setAppName("App Name");
		app.setId(1);
		when(appService.findById(1)).thenReturn(Optional.of(app));
		
		int latestAppReleaseId = 2;
		AppRelease latestAppRelease = new AppRelease();
		latestAppRelease.setId(latestAppReleaseId);
		when(appReleaseService.findLatestReleaseApp(anyInt())).thenReturn(Optional.of(latestAppRelease));
		
		// 获取适配操作系统和架构的发行版文件
		AppReleaseFile latestAppReleaseFile = new AppReleaseFile();
		latestAppReleaseFile.setAppReleaseId(latestAppRelease.getId());
		latestAppReleaseFile.setFileName("app_window_x86.jar");
		when(appReleaseFileService.find(eq(latestAppReleaseId), anyString(), anyString()))
			.thenReturn(Optional.of(latestAppReleaseFile));
		
		// 获取依赖的软件列表，因为这里的软件只依赖 JDK，所以最好提供获取单个依赖的方法
		int dependAppReleaseId = 3;
		when(appReleaseRelationService.findSingle(eq(latestAppReleaseId))).thenReturn(Optional.of(dependAppReleaseId));
		
		// 获取依赖 APP 的发行版基本信息
		AppRelease dependAppRelease = new AppRelease();
		dependAppRelease.setId(dependAppReleaseId);
		dependAppRelease.setAppId(2);
		dependAppRelease.setVersion("0.1.2");
		when(appReleaseService.findById(eq(dependAppReleaseId))).thenReturn(Optional.of(dependAppRelease));
		
		// 获取依赖 APP 的基本信息
		App dependApp = new App();
		dependApp.setId(2);
		dependApp.setAppName("jdk_app_name_updated");
		when(appService.findById(eq(2))).thenReturn(Optional.of(dependApp));
		
		// 获取依赖 APP 的发行版文件信息
		when(appReleaseFileService.find(eq(dependAppReleaseId), anyString(), anyString())).thenReturn(Optional.empty());
				
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.put("/installers")
		.then()
			.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
			.body("errors.globalErrors", hasItems("兼容 windows X86 的 JDK 安装文件不存在"));
	}
	
	@Test
	public void put_installer_success() {
		String installerToken = "installer_token_1";
		UpdateRegistrationParam registration = prepareUpdateParam(installerToken);
		
		Installer installer = new Installer();
		installer.setId(1);
		installer.setAppReleaseId(1);
		when(installerService.findByInstallerToken(eq(installerToken))).thenReturn(Optional.of(installer));
		
		WebServer webServer = new WebServer();
		webServer.setId(1);
		webServer.setServerToken("server_token");
		when(webServerService.findById(eq(installer.getWebServerId()))).thenReturn(Optional.of(webServer));
		
		AppRelease currentAppRelease = new AppRelease();
		currentAppRelease.setId(1);
		currentAppRelease.setAppId(1);
		when(appReleaseService.findById(anyInt())).thenReturn(Optional.of(currentAppRelease));
		
		Integer latestAppReleaseId = 2;
		AppRelease latestAppRelease = new AppRelease();
		latestAppRelease.setId(latestAppReleaseId);
		latestAppRelease.setAppId(1);
		latestAppRelease.setVersion("0.0.2");
		when(appReleaseService.findLatestReleaseApp(anyInt())).thenReturn(Optional.of(latestAppRelease));
		
		// 为了防止 APP 的名字发生了变化，重新获取 APP 信息
		App app = new App();
		app.setId(1);
		app.setAppName("App Name updated");
		when(appService.findById(eq(1))).thenReturn(Optional.of(app));
		
		// 获取适配操作系统和架构的发行版文件
		AppReleaseFile appReleaseFile = new AppReleaseFile();
		appReleaseFile.setAppReleaseId(latestAppReleaseId);
		appReleaseFile.setFileName("app_window_x86.jar");
		when(appReleaseFileService.find(anyInt(), anyString(), anyString())).thenReturn(Optional.of(appReleaseFile));
		
		// 获取依赖的软件列表，因为这里的软件只依赖 JDK，所以最好提供获取单个依赖的方法
		int dependAppReleaseId = 3;
		when(appReleaseRelationService.findSingle(eq(latestAppReleaseId))).thenReturn(Optional.of(dependAppReleaseId));
		
		// 获取依赖 APP 的发行版基本信息
		AppRelease dependAppRelease = new AppRelease();
		dependAppRelease.setId(dependAppReleaseId);
		dependAppRelease.setAppId(2);
		dependAppRelease.setVersion("0.1.2");
		when(appReleaseService.findById(eq(dependAppReleaseId))).thenReturn(Optional.of(dependAppRelease));
		
		// 获取依赖 APP 的基本信息
		App dependApp = new App();
		dependApp.setId(2);
		dependApp.setAppName("jdk_app_name_updated");
		when(appService.findById(eq(2))).thenReturn(Optional.of(dependApp));
		
		// 获取依赖 APP 的发行版文件信息
		AppReleaseFile dependAppReleaseFile = new AppReleaseFile();
		dependAppReleaseFile.setFileName("jdk_app_window_x86.jar");
		when(appReleaseFileService.find(eq(dependAppReleaseId), anyString(), anyString())).thenReturn(Optional.of(dependAppReleaseFile));
		
		given()
			.contentType(ContentType.JSON)
			.body(registration)
		.when()
			.put("/installers")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(
					"installerToken", equalTo(installerToken), 
					"appRunPort", is(80),
					"appName", equalTo("App Name updated"),
					"appVersion", equalTo("0.0.2"),
					"appFileName", equalTo("app_window_x86.jar"),
					"jdkName", equalTo("jdk_app_name_updated"),
					"jdkVersion", equalTo("0.1.2"),
					"jdkFileName", equalTo("jdk_app_window_x86.jar"));	
	}

	private UpdateRegistrationParam prepareUpdateParam(String installerToken) {
		UpdateRegistrationParam registration = new UpdateRegistrationParam();
		registration.setInstallerToken(installerToken);
		registration.setServerToken("server_token");
		registration.setTargetOs("windows");
		registration.setOsType("windows");
		registration.setOsVersion("11");
		registration.setIp("10.10.10.10");
		registration.setArch("X86");
		return registration;
	}

	@Test
	public void delete_installer_invalid_installer_token() {
		when(installerService.findByInstallerToken(anyString())).thenReturn(Optional.empty());

		given()
		.when()
			.delete("/installers/{installerToken}", "not-exist-installer-token")
		.then()		
			.statusCode(HttpStatus.SC_NOT_FOUND);
		verify(installerService, never()).delete(null);
	}
	
	@Test
	public void delete_installer_success() {
		Installer installer = new Installer();
		installer.setId(1);
		installer.setInstallerToken("exist-installer-token");
		when(installerService.findByInstallerToken(eq("exist-installer-token"))).thenReturn(Optional.of(installer));

		given()
		.when()
			.delete("/installers/{installerToken}", "exist-installer-token")
		.then()		
			.statusCode(HttpStatus.SC_NO_CONTENT);
		verify(installerService).delete(eq(installer));
	}

}
