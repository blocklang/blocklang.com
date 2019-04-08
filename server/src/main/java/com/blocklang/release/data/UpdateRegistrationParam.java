package com.blocklang.release.data;

import javax.validation.constraints.NotBlank;

/**
 * 升级 APP 并更新服务器信息的参数
 * 
 * @author Zhengwei Jin
 *
 */
public class UpdateRegistrationParam {

	@NotBlank(message = "{NotBlank.installerToken}")
	private String installerToken;
	@NotBlank(message = "{NotBlank.ip}")
	private String ip;
	private Integer appRunPort = 80;
	@NotBlank(message = "{NotBlank.osType}")
	private String osType;
	@NotBlank(message = "{NotBlank.osVersion}")
	private String osVersion;
	@NotBlank(message = "{NotBlank.targetOs}")
	private String targetOs;
	@NotBlank(message = "{NotBlank.arch}")
	private String arch;
	@NotBlank(message = "{NotBlank.serverToken}")
	private String serverToken;

	public String getInstallerToken() {
		return installerToken;
	}

	public void setInstallerToken(String installerToken) {
		this.installerToken = installerToken;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getAppRunPort() {
		return appRunPort;
	}

	public void setAppRunPort(Integer appRunPort) {
		this.appRunPort = appRunPort;
	}

	public String getOsType() {
		return osType;
	}

	public void setOsType(String osType) {
		this.osType = osType;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getTargetOs() {
		return targetOs;
	}

	public void setTargetOs(String targetOs) {
		this.targetOs = targetOs;
	}

	public String getArch() {
		return arch;
	}

	public void setArch(String arch) {
		this.arch = arch;
	}

	public String getServerToken() {
		return serverToken;
	}

	public void setServerToken(String serverToken) {
		this.serverToken = serverToken;
	}

	@Override
	public String toString() {
		return "UpdateRegistrationParam [installerToken=" + installerToken + ", ip=" + ip + ", appRunPort=" + appRunPort
				+ ", osType=" + osType + ", osVersion=" + osVersion + ", targetOs=" + targetOs + ", arch=" + arch
				+ ", serverToken=" + serverToken + "]";
	}
	
}
