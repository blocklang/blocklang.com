package com.blocklang.release.api;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.exception.InvalidRequestException;
import com.blocklang.exception.ResourceNotFoundException;
import com.blocklang.release.data.InstallerInfo;
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

@RestController
@RequestMapping("/installers")
public class InstallerApi {
	
	private static final Logger logger = LoggerFactory.getLogger(InstallerApi.class);
	
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
	@Autowired
	private WebServerService webServerService;

	@PostMapping
	public ResponseEntity<InstallerInfo> newInstaller(
			@Valid @RequestBody NewRegistrationParam registrationInfo,
			BindingResult bindingResult) {
		logger.info("==========开始注册 installer==========");
		if(bindingResult.hasErrors()) {
			logger.error("注册信息不完整：{}", registrationInfo);
			throw new InvalidRequestException(bindingResult);
		}
		// 一、获取 APP 信息
		// 1. 根据注册 token 获取 APP 基本信息
		App app = appService.findByRegistrationToken(registrationInfo.getRegistrationToken()).orElseThrow(() -> {
			logger.error("注册 Token `{}` 不存在", registrationInfo.getRegistrationToken());
			bindingResult.rejectValue("registrationToken", 
					"NotExist.registrationToken",
					new Object[] { registrationInfo.getRegistrationToken() }, 
					null);
			return new InvalidRequestException(bindingResult);
		});
		// 2. 获取 APP 发行版基本信息
		AppRelease appRelease = appReleaseService.findLatestReleaseApp(app.getId()).orElseThrow(() -> {
			logger.error("没有找到 App (AppId = {}, AppName = {})的发行版", app.getId(), app.getAppName());
			bindingResult.reject("NotExist.appRelease", new Object[] {app.getAppName()}, null);
			return new InvalidRequestException(bindingResult);
		});
		// 3. 获取 APP 发行版文件信息
		AppReleaseFile appReleaseFile = appReleaseFileService
				.find(appRelease.getId(), registrationInfo.getTargetOs(), registrationInfo.getArch())
				.orElseThrow(() -> {
					logger.error("{} 兼容 {} {} 的发行版文件不存在", 
							app.getAppName(), 
							registrationInfo.getTargetOs(), 
							registrationInfo.getArch());
					bindingResult.reject("NotExist.appReleaseFile", 
							new Object[] { 
									app.getAppName(),
									registrationInfo.getTargetOs(), 
									registrationInfo.getArch() 
								}, 
							null);
					return new InvalidRequestException(bindingResult);
				});	

		// 二、查询 APP 依赖的 JDK 发行版信息
		// 1. 获取依赖的 APP release id
		Integer dependAppReleaseId = appReleaseRelationService.findSingle(appRelease.getId()).orElseThrow(() -> {
			logger.error("{} 依赖的 JDK 发行版信息不存在", app.getAppName());
			bindingResult.reject("NotExist.dependAppRelease", new Object[] { app.getAppName() }, null);
			return new InvalidRequestException(bindingResult);
		});

		// 2. 获取依赖的 APP 发行版信息
		AppRelease dependAppRelease = appReleaseService.findById(dependAppReleaseId).orElseThrow(() -> {
			logger.error("{} 依赖的 JDK 发行版信息不存在", app.getAppName());
			bindingResult.reject("NotExist.dependAppRelease", new Object[] { app.getAppName() }, null);
			return new InvalidRequestException(bindingResult);
		});

		// 3. 获取依赖的 APP 基本信息
		// 如果能找到发行版，则一定能找到发行版的 APP 基本信息
		App dependApp = appService.findById(dependAppRelease.getAppId())
				.filter((dapp) -> dapp.getAppName().toLowerCase().contains("jdk"))
				.orElseThrow(() -> {
					logger.error("{} 的依赖不是有效的 JDK", app.getAppName());
					bindingResult.reject("NotMatch.dependApp", new Object[] { app.getAppName() }, null);
					return new InvalidRequestException(bindingResult);
				});

		// 4. 获取依赖 APP 的发行版文件信息
		AppReleaseFile dependAppReleaseFile = appReleaseFileService.find(
				dependAppRelease.getId(),
				registrationInfo.getTargetOs(), 
				registrationInfo.getArch()).orElseThrow(() -> {
					logger.error("兼容 {} {} 的 JDK 安装文件不存在", 
							registrationInfo.getTargetOs(), 
							registrationInfo.getArch());
					bindingResult.reject("NotExist.dependAppReleaseFile", 
							new Object[] { 
									registrationInfo.getTargetOs(), 
									registrationInfo.getArch() 
								}, 
							null);
					return new InvalidRequestException(bindingResult);					
				});

		// 三、注册安装器
		// 所有校验都通过后，才开始注册安装器
		String installerToken = installerService.save(registrationInfo, appRelease.getId());
		
		// 返回安装器信息
		InstallerInfo installerInfo = new InstallerInfo();
		installerInfo.setInstallerToken(installerToken);
		installerInfo.setAppName(app.getAppName()); // 如果是自动，则约定由用户名和项目名组成
		installerInfo.setAppRunPort(registrationInfo.getAppRunPort());
		installerInfo.setAppVersion(appRelease.getVersion());
		installerInfo.setAppFileName(appReleaseFile.getFileName());
		installerInfo.setJdkName(dependApp.getAppName());
		installerInfo.setJdkVersion(dependAppRelease.getVersion());
		installerInfo.setJdkFileName(dependAppReleaseFile.getFileName());
		
		return new ResponseEntity<InstallerInfo>(installerInfo, HttpStatus.CREATED);
	}

	@PutMapping
	public ResponseEntity<InstallerInfo> updateInstaller(
			@Valid @RequestBody UpdateRegistrationParam registrationInfo,
			BindingResult bindingResult) {
		logger.info("==========开始更新 APP 信息==========");
		if(bindingResult.hasErrors()) {
			logger.error("获取 APP 最新版本的注册信息不完整：{}", registrationInfo);
			throw new InvalidRequestException(bindingResult);
		}
		
		Installer installer = installerService.findByInstallerToken(registrationInfo.getInstallerToken()).orElseThrow(() -> {
			logger.error("安装器 Token `{}` 不存在", registrationInfo.getInstallerToken());
			bindingResult.rejectValue("installerToken", 
					"NotExist.installerToken",
					new Object[] { registrationInfo.getInstallerToken() }, 
					null);
			return new InvalidRequestException(bindingResult);
		});
		// 确认 Server Token 未变化
		WebServer webServer = webServerService.findById(installer.getWebServerId()).orElseThrow(() -> {
			return new InvalidRequestException(bindingResult);
		});
		if(!webServer.getServerToken().equals(registrationInfo.getServerToken())) {
			logger.error("服务器标识被篡改。注册的服务器标识是 `{}`，但本次升级传入的服务器标识是 `{}`", 
					webServer.getServerToken(), 
					registrationInfo.getServerToken());
			bindingResult.rejectValue("serverToken", 
					"NotMatch.serverToken",
					new Object[] { webServer.getServerToken(), registrationInfo.getServerToken() }, 
					null);
			throw new InvalidRequestException(bindingResult);
		}
		
		// 1. 更新服务器信息
		installerService.update(installer, registrationInfo);
		
		// 根据 appReleaseId 获取 appId
		AppRelease currentAppRelease = appReleaseService.findById(installer.getAppReleaseId()).orElseThrow(() -> {
			logger.error("根据发行版 ID 没有找到发行版信息");
			bindingResult.reject("NotExist.appRelease.byId");
			return new InvalidRequestException(bindingResult);
		});
		// 根据 appId 获取 APP 基本信息
		App app = appService.findById(currentAppRelease.getAppId()).orElseThrow(() -> {
			logger.error("没有找到 APP");
			bindingResult.reject("NotExist.app");
			return new InvalidRequestException(bindingResult);
		});
		
		// 2. 获取 APP 最新版本信息
		AppRelease latestAppRelease = appReleaseService.findLatestReleaseApp(app.getId()).orElseThrow(() -> {
			logger.error("没有找到 App (AppId = {}, AppName = {})的发行版", app.getId(), app.getAppName());
			bindingResult.reject("NotExist.appRelease", new Object[] {app.getAppName()}, null);
			return new InvalidRequestException(bindingResult);
		});
		// 3. 获取 APP 发行版文件信息
		AppReleaseFile latestAppReleaseFile = appReleaseFileService
				.find(latestAppRelease.getId(), registrationInfo.getTargetOs(), registrationInfo.getArch())
				.orElseThrow(() -> {
					logger.error("{} 兼容 {} {} 的发行版文件不存在", 
							app.getAppName(), 
							registrationInfo.getTargetOs(), 
							registrationInfo.getArch());
					bindingResult.reject("NotExist.appReleaseFile", 
							new Object[] { 
									app.getAppName(),
									registrationInfo.getTargetOs(), 
									registrationInfo.getArch() 
								}, 
							null);
					return new InvalidRequestException(bindingResult);
				});	

		// 二、查询 APP 依赖的 JDK 发行版信息
		// 1. 获取依赖的 APP release id
		Integer dependAppReleaseId = appReleaseRelationService.findSingle(latestAppRelease.getId()).orElseThrow(() -> {
			logger.error("{} 依赖的 JDK 发行版信息不存在", app.getAppName());
			bindingResult.reject("NotExist.dependAppRelease", new Object[] { app.getAppName() }, null);
			return new InvalidRequestException(bindingResult);
		});

		// 2. 获取依赖的 APP 发行版信息
		AppRelease dependAppRelease = appReleaseService.findById(dependAppReleaseId).orElseThrow(() -> {
			logger.error("{} 依赖的 JDK 发行版信息不存在", app.getAppName());
			bindingResult.reject("NotExist.dependAppRelease", new Object[] { app.getAppName() }, null);
			return new InvalidRequestException(bindingResult);
		});

		// 3. 获取依赖的 APP 基本信息
		// 如果能找到发行版，则一定能找到发行版的 APP 基本信息
		App dependApp = appService.findById(dependAppRelease.getAppId())
				.filter((dapp) -> dapp.getAppName().toLowerCase().contains("jdk"))
				.orElseThrow(() -> {
					logger.error("{} 的依赖不是有效的 JDK", app.getAppName());
					bindingResult.reject("NotMatch.dependApp", new Object[] { app.getAppName() }, null);
					return new InvalidRequestException(bindingResult);
				});

		// 4. 获取依赖 APP 的发行版文件信息
		AppReleaseFile dependAppReleaseFile = appReleaseFileService.find(
				dependAppRelease.getId(),
				registrationInfo.getTargetOs(), 
				registrationInfo.getArch()).orElseThrow(() -> {
					logger.error("兼容 {} {} 的 JDK 安装文件不存在", 
							registrationInfo.getTargetOs(), 
							registrationInfo.getArch());
					bindingResult.reject("NotExist.dependAppReleaseFile", 
							new Object[] { 
									registrationInfo.getTargetOs(), 
									registrationInfo.getArch() 
								}, 
							null);
					return new InvalidRequestException(bindingResult);					
				});
		
		// 返回安装器信息
		InstallerInfo installerInfo = new InstallerInfo();
		installerInfo.setInstallerToken(registrationInfo.getInstallerToken());
		installerInfo.setAppName(app.getAppName()); // 如果是自动，则约定由用户名和项目名组成
		installerInfo.setAppRunPort(registrationInfo.getAppRunPort()); // 必须要更新 Installer 信息
		installerInfo.setAppVersion(latestAppRelease.getVersion());
		installerInfo.setAppFileName(latestAppReleaseFile.getFileName());
		installerInfo.setJdkName(dependApp.getAppName());
		installerInfo.setJdkVersion(dependAppRelease.getVersion());
		installerInfo.setJdkFileName(dependAppReleaseFile.getFileName());
		return new ResponseEntity<InstallerInfo>(installerInfo, HttpStatus.OK);
	}

	@DeleteMapping("/{installerToken}")
	public ResponseEntity<?> deleteInstaller(
			@PathVariable String installerToken) {
		
		logger.info("==========开始注销 installer==========");
		return installerService.findByInstallerToken(installerToken).map(installer -> {
			installerService.delete(installer);
			return ResponseEntity.noContent().build();
		}).orElseThrow(ResourceNotFoundException::new);
	}
}
