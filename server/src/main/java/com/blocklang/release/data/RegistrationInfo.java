package com.blocklang.release.data;

/**
 * Installer 注册信息
 * 
 * @author ZhengWei Jin
 *
 */
public class RegistrationInfo {

	private String registrationToken;
	private String ip;
	private Integer appRunPort;
	private String osType;
	private String osVersion;
	private String arch;
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
