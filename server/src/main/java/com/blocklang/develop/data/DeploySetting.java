package com.blocklang.develop.data;

public class DeploySetting {
	private Integer id;
	private Integer projectId;
	private Integer userId;
	private String registrationToken;
	private String deployState;
	private String url;
	private String installerLinuxUrl;
	private String installerWindowsUrl;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getProjectId() {
		return projectId;
	}
	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getRegistrationToken() {
		return registrationToken;
	}
	public void setRegistrationToken(String registrationToken) {
		this.registrationToken = registrationToken;
	}
	public String getDeployState() {
		return deployState;
	}
	public void setDeployState(String deployState) {
		this.deployState = deployState;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getInstallerLinuxUrl() {
		return installerLinuxUrl;
	}
	public void setInstallerLinuxUrl(String installerLinuxUrl) {
		this.installerLinuxUrl = installerLinuxUrl;
	}
	public String getInstallerWindowsUrl() {
		return installerWindowsUrl;
	}
	public void setInstallerWindowsUrl(String installerWindowsUrl) {
		this.installerWindowsUrl = installerWindowsUrl;
	}
}
