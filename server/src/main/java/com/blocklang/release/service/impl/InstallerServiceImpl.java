package com.blocklang.release.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.release.constant.Arch;
import com.blocklang.release.constant.Bot;
import com.blocklang.release.constant.OsType;
import com.blocklang.release.dao.InstallerDao;
import com.blocklang.release.dao.WebServerDao;
import com.blocklang.release.data.RegistrationInfo;
import com.blocklang.release.model.Installer;
import com.blocklang.release.model.WebServer;
import com.blocklang.release.service.InstallerService;
import com.blocklang.release.util.IdGenerator;

/**
 * APP 安装器业务逻辑接口
 * 
 * @author Zhengwei Jin
 *
 */
@Service
public class InstallerServiceImpl implements InstallerService {

	@Autowired
	private WebServerDao webServerDao;
	@Autowired
	private InstallerDao installerDao;
	
	@Transactional
	@Override
	public String save(RegistrationInfo registrationInfo, Integer appReleaseId) {
		String installerToken = IdGenerator.shortUuid();
		
		// 如果 Web Server 未保存，则先保存 Web Server 信息
		Optional<WebServer> webServerOption = webServerDao.findByServerToken(registrationInfo.getServerToken());
		Integer webServerId = null;
		if(webServerOption.isEmpty()) {
			// 如果 web server 还不存在，则保存
			WebServer newWebServer = new WebServer();
			newWebServer.setServerToken(registrationInfo.getServerToken());
			newWebServer.setIp(registrationInfo.getIp());
			newWebServer.setOsType(OsType.fromValue(registrationInfo.getOsType()));
			newWebServer.setOsVersion(registrationInfo.getOsVersion());
			newWebServer.setArch(Arch.fromValue(registrationInfo.getArch()));
			newWebServer.setCreateUserId(Bot.ID);
			newWebServer.setCreateTime(LocalDateTime.now());
			webServerId = webServerDao.save(newWebServer).getId();
		} else {
			WebServer existWebServer = webServerOption.get();
			webServerId = existWebServer.getId();
			// 如果 web server 信息改变了，则更新
			if(!existWebServer.getIp().equals(registrationInfo.getIp()) ||
			   !existWebServer.getOsType().getValue().equals(registrationInfo.getOsType()) ||
			   !existWebServer.getOsVersion().equals(registrationInfo.getOsVersion()) ||
			   !existWebServer.getArch().getValue().equals(registrationInfo.getArch())) {
				existWebServer.setIp(registrationInfo.getIp());
				existWebServer.setOsType(OsType.fromValue(registrationInfo.getOsType()));
				existWebServer.setOsVersion(registrationInfo.getOsVersion());
				existWebServer.setArch(Arch.fromValue(registrationInfo.getArch()));
				existWebServer.setLastUpdateUserId(Bot.ID);
				existWebServer.setLastUpdateTime(LocalDateTime.now());
				
				webServerDao.save(existWebServer);
			}
		}
		
		// 新增 installer
		Installer installer = new Installer();
		installer.setInstallerToken(installerToken);
		installer.setWebServerId(webServerId);
		installer.setAppReleaseId(appReleaseId);
		installer.setAppRunPort(registrationInfo.getAppRunPort());
		installer.setCreateUserId(Bot.ID);
		installer.setCreateTime(LocalDateTime.now());
		
		installerDao.save(installer);
		
		return installerToken;
	}

}
