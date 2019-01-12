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
		Optional<AppReleaseFile> appReleaseFileOption = appReleaseFileService.find(appRelease.getId(), registrationInfo.getTargetOs(), registrationInfo.getArch());
		
		// 查询 APP 依赖的 JDK 信息
		
		// 返回安装器信息
		InstallerInfo installerInfo = new InstallerInfo();
		installerInfo.setAppName(app.getAppName());
		installerInfo.setAppRunPort(registrationInfo.getAppRunPort());
		installerInfo.setAppVersion(appRelease.getVersion());
		installerInfo.setInstallerToken(installerToken);
		
		return new ResponseEntity<InstallerInfo>(installerInfo, HttpStatus.CREATED);
	}
}
