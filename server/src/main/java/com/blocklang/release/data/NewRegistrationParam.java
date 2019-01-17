package com.blocklang.release.data;

import javax.validation.constraints.NotBlank;

/**
 * Installer 注册信息
 * 
 * @author ZhengWei Jin
 *
 */
public class NewRegistrationParam {

	@NotBlank(message = "{NotBlank.registrationToken}")
	private String registrationToken;
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

	public String getRegistrationToken() {
		return registrationToken;
	}

	public void setRegistrationToken(String registrationToken) {
		this.registrationToken = registrationToken;
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
		return "RegistrationInfo [registrationToken=" + registrationToken + ", ip=" + ip + ", appRunPort=" + appRunPort
				+ ", osType=" + osType + ", osVersion=" + osVersion + ", targetOs=" + targetOs + ", arch=" + arch
				+ ", serverToken=" + serverToken + "]";
	}
	
}
