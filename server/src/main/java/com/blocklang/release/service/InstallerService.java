package com.blocklang.release.service;

import java.util.Optional;


import com.blocklang.release.data.NewRegistrationParam;
import com.blocklang.release.data.UpdateRegistrationParam;
import com.blocklang.release.model.Installer;

public interface InstallerService {

	/**
	 * 登记服务器信息和安装器信息
	 * 
	 * @param registrationInfo 注册信息
	 * @param appReleaseId app 发行版标识
	 * @return 返回安装器 token
	 */
	String save(NewRegistrationParam registrationInfo, Integer appReleaseId);

	Optional<Installer> findByInstallerToken(String installerToken);

	void update(Installer existedInstaller, UpdateRegistrationParam registrationInfo);

}
