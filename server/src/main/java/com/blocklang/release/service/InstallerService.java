package com.blocklang.release.service;

import com.blocklang.release.data.RegistrationInfo;

public interface InstallerService {

	/**
	 * 登记服务器信息和安装器信息
	 * 
	 * @param registrationInfo 注册信息
	 * @param appReleaseId app 发行版标识
	 * @return 返回安装器 token
	 */
	String save(RegistrationInfo registrationInfo, Integer appReleaseId);

}
