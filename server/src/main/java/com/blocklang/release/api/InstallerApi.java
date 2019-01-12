package com.blocklang.release.api;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.release.data.InstallerInfo;
import com.blocklang.release.data.RegistrationInfo;
import com.blocklang.release.model.App;
import com.blocklang.release.model.AppRelease;
import com.blocklang.release.model.AppReleaseFile;
import com.blocklang.release.service.AppReleaseFileService;
import com.blocklang.release.service.AppReleaseRelationService;
import com.blocklang.release.service.AppReleaseService;
import com.blocklang.release.service.AppService;
import com.blocklang.release.service.InstallerService;

@RestController
@RequestMapping("/installers")
public class InstallerApi {
	
	@Autowired
	private AppService appService;
	@Autowired
	private AppReleaseService appReleaseService;
	@Autowired
	private InstallerService installerService;
	@Autowired
	private AppReleaseFileService appReleaseFileService;
	@Autowired
	private AppReleaseRelationService appReleaseRelationService;

	@PostMapping
	public ResponseEntity<InstallerInfo> newRegister(@Valid @RequestBody RegistrationInfo registrationInfo) {
		// 判断根据注册 token 是否能获取 APP 信息
		Optional<App> appOption = appService.findByRegistratioToken(registrationInfo.getRegistrationToken());
		if (appOption.isEmpty()) {
			// TODO: 返回精准的提示信息
			return ResponseEntity.noContent().build();
		}
		
		// 判断 APP 是否已发布，如果没有发布，则返回提示信息
		App app = appOption.get();
		Optional<AppRelease> appReleaseOption = appReleaseService.findLatestReleaseApp(app.getId());
		if(appReleaseOption.isEmpty()) {
			// TODO: 返回精准的提示信息
			return ResponseEntity.noContent().build();
		}
		
		AppRelease appRelease = appReleaseOption.get();
		
		// 注册安装器
		String installerToken = installerService.save(registrationInfo);
		// 查询 APP 发行版文件信息
		Optional<AppReleaseFile> appReleaseFileOption = appReleaseFileService.find(
				appRelease.getId(),
				registrationInfo.getTargetOs(), 
				registrationInfo.getArch());
		if(appReleaseFileOption.isEmpty()) {
			// TODO: 返回精准的提示信息
			return ResponseEntity.noContent().build();
		}
		AppReleaseFile appReleaseFile = appReleaseFileOption.get();
		
		// 查询 APP 依赖的 JDK 发行版信息
		// 1. 获取依赖的 APP id
		Optional<Integer> dependAppReleaseIdOption = appReleaseRelationService.findSingle(appRelease.getId());
		if(dependAppReleaseIdOption.isEmpty()) {
			// TODO: 返回精准的提示信息
			return ResponseEntity.noContent().build();
		}
		// 2. 获取依赖的 APP 发行版信息
		Integer dependAppReleaseId = dependAppReleaseIdOption.get();
		Optional<AppRelease> dependAppReleaseOption = appReleaseService.find(dependAppReleaseId);
		if(dependAppReleaseOption.isEmpty()) {
			// TODO: 返回精准的提示信息
			return ResponseEntity.noContent().build();
		}
		AppRelease dependAppRelease = dependAppReleaseOption.get();
		// 3. 获取依赖的 APP 基本信息
		Optional<App> dependAppOption = appService.find(dependAppRelease.getAppId());
		if(dependAppOption.isEmpty()) {
			// TODO: 返回精准的提示信息
			return ResponseEntity.noContent().build();
		}
		App dependApp = dependAppOption.get();
		// 4. 获取依赖 APP 的发行版文件信息
		Optional<AppReleaseFile> dependAppReleaseFileOption = appReleaseFileService.find(
				dependAppRelease.getId(),
				registrationInfo.getTargetOs(), 
				registrationInfo.getArch());
		if(dependAppReleaseFileOption.isEmpty()) {
			// TODO: 返回精准的提示信息
			return ResponseEntity.noContent().build();
		}
		AppReleaseFile dependAppReleaseFile = dependAppReleaseFileOption.get();
		// 返回安装器信息
		InstallerInfo installerInfo = new InstallerInfo();
		installerInfo.setInstallerToken(installerToken);
		installerInfo.setAppName(app.getAppName());
		installerInfo.setAppRunPort(registrationInfo.getAppRunPort());
		installerInfo.setAppVersion(appRelease.getVersion());
		installerInfo.setAppFileName(appReleaseFile.getFileName());
		installerInfo.setJdkName(dependApp.getAppName());
		installerInfo.setJdkVersion(dependAppRelease.getVersion());
		installerInfo.setJdkFileName(dependAppReleaseFile.getFileName());
		
		return new ResponseEntity<InstallerInfo>(installerInfo, HttpStatus.CREATED);
	}
}
