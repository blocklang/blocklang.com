package com.blocklang.release.data;

import javax.validation.constraints.NotBlank;

/**
 * Installer 注册信息
 * 
 * @author ZhengWei Jin
 *
 */
public class RegistrationInfo {

	@NotBlank(message = "注册 Token 不能为空")
	private String registrationToken;
	@NotBlank(message = "服务器的 IP 地址不能为空")
	private String ip;
	private Integer appRunPort = 80;
	@NotBlank(message = "服务器的操作系统具体类型不能为空")
	private String osType;
	@NotBlank(message = "服务器的操作系统版本不能为空")
	private String osVersion;
	@NotBlank(message = "服务器的操作系统类型不能为空")
	private String targetOs;
	@NotBlank(message = "服务器的 CPU 架构不能为空")
	private String arch;
	@NotBlank(message = "服务器的 Token 不能为空")
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
}
