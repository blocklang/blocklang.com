package com.blocklang.release.api;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.release.data.InstallerInfo;
import com.blocklang.release.data.RegistrationInfo;
import com.blocklang.release.exception.InvalidRequestException;
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

	@PostMapping
	public ResponseEntity<InstallerInfo> newInstaller(
			@Valid @RequestBody RegistrationInfo registrationInfo,
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
		AppRelease dependAppRelease = appReleaseService.find(dependAppReleaseId).orElseThrow(() -> {
			logger.error("{} 依赖的 JDK 发行版信息不存在", app.getAppName());
			bindingResult.reject("NotExist.dependAppRelease", new Object[] { app.getAppName() }, null);
			return new InvalidRequestException(bindingResult);
		});

		// 3. 获取依赖的 APP 基本信息
		// 如果能找到发行版，则一定能找到发行版的 APP 基本信息
		App dependApp = appService.find(dependAppRelease.getAppId())
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
		String installerToken = installerService.save(registrationInfo);
		
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
