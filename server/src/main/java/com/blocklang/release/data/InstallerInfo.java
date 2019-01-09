package com.blocklang.release.data;

/**
 * installer 信息
 * 
 * @author Zhengwei Jin
 *
 */
public class InstallerInfo {

	private String installerToken;
	private String appName;
	private String appVersion;
	private String appFileName;
	private String appRunPort;
	private String jdkName;
	private String jdkVersion;
	private String jdkFileName;

	public String getInstallerToken() {
		return installerToken;
	}

	public void setInstallerToken(String installerToken) {
		this.installerToken = installerToken;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getAppFileName() {
		return appFileName;
	}

	public void setAppFileName(String appFileName) {
		this.appFileName = appFileName;
	}

	public String getAppRunPort() {
		return appRunPort;
	}

	public void setAppRunPort(String appRunPort) {
		this.appRunPort = appRunPort;
	}

	public String getJdkName() {
		return jdkName;
	}

	public void setJdkName(String jdkName) {
		this.jdkName = jdkName;
	}

	public String getJdkVersion() {
		return jdkVersion;
	}

	public void setJdkVersion(String jdkVersion) {
		this.jdkVersion = jdkVersion;
	}

	public String getJdkFileName() {
		return jdkFileName;
	}

	public void setJdkFileName(String jdkFileName) {
		this.jdkFileName = jdkFileName;
	}

}
