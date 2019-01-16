package com.blocklang.release.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Installer extends PartialOperateFields {

	private static final long serialVersionUID = -3654504462446169345L;

	@Column(name = "web_server_id", nullable = false)
	private Integer webServerId;

	@Column(name = "app_release_id", nullable = false)
	private Integer appReleaseId;

	@Column(name = "app_run_port", nullable = false)
	private Integer appRunPort;

	@Column(name = "installer_token", nullable = false, unique = true, length = 22)
	private String installerToken;

	public Integer getWebServerId() {
		return webServerId;
	}

	public void setWebServerId(Integer webServerId) {
		this.webServerId = webServerId;
	}

	public Integer getAppReleaseId() {
		return appReleaseId;
	}

	public void setAppReleaseId(Integer appReleaseId) {
		this.appReleaseId = appReleaseId;
	}

	public Integer getAppRunPort() {
		return appRunPort;
	}

	public void setAppRunPort(Integer appRunPort) {
		this.appRunPort = appRunPort;
	}

	public String getInstallerToken() {
		return installerToken;
	}

	public void setInstallerToken(String installerToken) {
		this.installerToken = installerToken;
	}

}
