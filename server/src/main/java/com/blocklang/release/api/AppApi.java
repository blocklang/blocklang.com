package com.blocklang.release.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.core.service.PropertyService;
import com.blocklang.develop.model.AppGlobalContext;
import com.blocklang.release.model.App;
import com.blocklang.release.model.AppRelease;
import com.blocklang.release.model.AppReleaseFile;
import com.blocklang.release.service.AppReleaseFileService;
import com.blocklang.release.service.AppReleaseService;
import com.blocklang.release.service.AppService;

@RestController
@RequestMapping("/apps")
public class AppApi {
	
	@Autowired
	private AppService appService;
	@Autowired
	private AppReleaseService appReleaseService;
	@Autowired
	private AppReleaseFileService appReleaseFileService;
	@Autowired
	private PropertyService propertyService;

	@GetMapping
	public ResponseEntity<InputStreamSource> downloadAppFile(String appName, 
			String version, 
			String targetOs,
			String arch) {
		
		App app = appService.findByAppName(appName).orElseThrow(ResourceNotFoundException::new);
		
		AppRelease appRelease = appReleaseService.findByAppIdAndVersion(app.getId(), version)
				.orElseThrow(ResourceNotFoundException::new);
		
		AppReleaseFile appReleaseFile = appReleaseFileService.find(appRelease.getId(), targetOs, arch)
				.orElseThrow(ResourceNotFoundException::new);

		String dataRootPath = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).get();
		String mavenRootPath = propertyService.findStringValue(CmPropKey.MAVEN_ROOT_PATH).get();
		
		AppGlobalContext context = new AppGlobalContext(dataRootPath, mavenRootPath);
		if(app.getProjectId() == null) {
			// 如果是平台使用的软件，则从 apps 文件夹中获取文件
			// 只存储紧密相关的相对路径，如不存储 apps 文件夹
			appReleaseFile.setAbsoluteRootPath(context.getAppsDirectory().toString());
		} else {
			// 如果是 blocklang 中的项目生成的 jar，则从 maven 目录结构中获取
			appReleaseFile.setAbsoluteRootPath(context.getMavenRepositoryRootDirectory().toString());
		}

		Path path = Paths.get(appReleaseFile.getFullPath());
		File file = path.toFile();
		if(file.exists()) {
			try {
				InputStreamResource resource = new InputStreamResource(Files.newInputStream(path));
				// 设置文件名
				return ResponseEntity.ok()
						.contentLength(file.length())
						.contentType(MediaType.APPLICATION_OCTET_STREAM)
						.header("Content-Disposition","attachment;fileName=" + appReleaseFile.getFileName())
						.body(resource);
			} catch (IOException e) {
				throw new ResourceNotFoundException();
			}
		}
		
		throw new ResourceNotFoundException();
	}
}
