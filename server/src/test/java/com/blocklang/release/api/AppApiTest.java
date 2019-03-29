package com.blocklang.release.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.test.AbstractControllerTest;
import com.blocklang.release.constant.Arch;
import com.blocklang.release.constant.TargetOs;
import com.blocklang.release.model.App;
import com.blocklang.release.model.AppRelease;
import com.blocklang.release.model.AppReleaseFile;
import com.blocklang.release.service.AppReleaseFileService;
import com.blocklang.release.service.AppReleaseService;
import com.blocklang.release.service.AppService;

@WebMvcTest(AppApi.class)
public class AppApiTest extends AbstractControllerTest{

	@MockBean
	private AppService appService;
	@MockBean
	private AppReleaseService appReleaseService;
	@MockBean
	private AppReleaseFileService appReleaseFileService;
	
	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Test
	public void should_404_if_app_not_found() {
		when(appService.findByAppName(anyString())).thenReturn(Optional.empty());
		
		given()
			.param("appName", "not-exist-app-name")
			.param("version", "0.1.0")
			.param("targetOs", "windows")
			.param("arch", "x86")
		.when()
			.get("/apps")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void should_404_if_app_release_not_found() {
		App app = new App();
		app.setId(1);
		app.setAppName("exist-app-name");
		when(appService.findByAppName(anyString())).thenReturn(Optional.of(app));
		
		when(appReleaseService.findByAppIdAndVersion(anyInt(), anyString())).thenReturn(Optional.empty());
		
		given()
			.param("appName", "exist-app-name")
			.param("version", "0.1.0")
			.param("targetOs", "windows")
			.param("arch", "x86")
		.when()
			.get("/apps")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
		
	}
	
	@Test
	public void should_404_if_app_file_info_not_found() {
		App app = new App();
		app.setId(1);
		app.setAppName("exist-app-name");
		when(appService.findByAppName(anyString())).thenReturn(Optional.of(app));
		
		AppRelease appRelease = new AppRelease();
		appRelease.setId(1);
		appRelease.setAppId(1);
		appRelease.setVersion("0.1.0");
		when(appReleaseService.findByAppIdAndVersion(anyInt(), anyString())).thenReturn(Optional.of(appRelease));
		
		when(appReleaseFileService.find(anyInt(), anyString(), anyString())).thenReturn(Optional.empty());
		
		given()
			.param("appName", "exist-app-name")
			.param("version", "0.1.0")
			.param("targetOs", "windows")
			.param("arch", "x86")
		.when()
			.get("/apps")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void should_404_if_app_file_not_found() {
		App app = new App();
		app.setId(1);
		app.setAppName("exist-app-name");
		when(appService.findByAppName(anyString())).thenReturn(Optional.of(app));
		
		AppRelease appRelease = new AppRelease();
		appRelease.setId(1);
		appRelease.setAppId(1);
		appRelease.setVersion("0.1.0");
		when(appReleaseService.findByAppIdAndVersion(anyInt(), anyString())).thenReturn(Optional.of(appRelease));
		
		AppReleaseFile appReleaseFile = new AppReleaseFile();
		appReleaseFile.setId(1);
		appReleaseFile.setAppReleaseId(1);
		appReleaseFile.setArch(Arch.X86_64);
		appReleaseFile.setTargetOs(TargetOs.LINUX);
		appReleaseFile.setFilePath("path");
		when(appReleaseFileService.find(anyInt(), anyString(), anyString())).thenReturn(Optional.of(appReleaseFile));
		
		when(propertyService.findStringValue(eq(CmPropKey.BLOCKLANG_ROOT_PATH))).thenReturn(Optional.of(temp.getRoot().getPath()));
		when(propertyService.findStringValue(eq(CmPropKey.MAVEN_ROOT_PATH))).thenReturn(Optional.of("c:/b"));
		
		// 未在临时文件夹中创建任何文件。
		
		given()
			.param("appName", "exist-app-name")
			.param("version", "0.1.0")
			.param("targetOs", "windows")
			.param("arch", "x86")
		.when()
			.get("/apps")
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void should_download_upload_app_success() throws IOException {
		// 因为没有设置 projectId，所以被认定为手工上传的 app
		App app = new App();
		app.setId(1);
		app.setAppName("exist-app-name");
		when(appService.findByAppName(anyString())).thenReturn(Optional.of(app));
		
		AppRelease appRelease = new AppRelease();
		appRelease.setId(1);
		appRelease.setAppId(1);
		appRelease.setVersion("0.1.0");
		when(appReleaseService.findByAppIdAndVersion(anyInt(), anyString())).thenReturn(Optional.of(appRelease));
		
		String filePath = "not_exist_app.temp";
		AppReleaseFile appReleaseFile = new AppReleaseFile();
		appReleaseFile.setId(1);
		appReleaseFile.setAppReleaseId(1);
		appReleaseFile.setArch(Arch.X86_64);
		appReleaseFile.setTargetOs(TargetOs.LINUX);
		appReleaseFile.setFilePath(filePath);
		when(appReleaseFileService.find(anyInt(), anyString(), anyString())).thenReturn(Optional.of(appReleaseFile));
		
		when(propertyService.findStringValue(eq(CmPropKey.BLOCKLANG_ROOT_PATH))).thenReturn(Optional.of(temp.getRoot().getPath()));
		when(propertyService.findStringValue(eq(CmPropKey.MAVEN_ROOT_PATH))).thenReturn(Optional.of("c:/b"));
		
		File appsFolder = temp.newFolder("apps");
		Files.createFile(appsFolder.toPath().resolve(filePath));
		
		given()
			.param("appName", "exist-app-name")
			.param("version", "0.1.0")
			.param("targetOs", "linux")
			.param("arch", "x86_64")
		.when()
			.get("/apps")
		.then()
			.statusCode(HttpStatus.SC_OK);
	}
}
