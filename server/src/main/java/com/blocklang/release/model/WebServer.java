package com.blocklang.release.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;

import com.blocklang.core.model.PartialOperateFields;
import com.blocklang.release.constant.Arch;
import com.blocklang.release.constant.OsType;
import com.blocklang.release.constant.converter.ArchConverter;
import com.blocklang.release.constant.converter.OsTypeConverter;

@Entity
public class WebServer extends PartialOperateFields {

	private static final long serialVersionUID = 2717397611614243623L;

	@Column(name = "server_token", nullable = false, length = 50, unique = true)
	private String serverToken;

	@Column(name = "ip", nullable = false, length = 50)
	private String ip;

	@Convert(converter = OsTypeConverter.class)
	@Column(name = "os_type", length = 2, nullable = false)
	private OsType osType;

	@Column(name = "os_version", nullable = false, length = 32)
	private String osVersion;

	@Convert(converter = ArchConverter.class)
	@Column(name = "arch", length = 2, nullable = false)
	private Arch arch;

	public String getServerToken() {
		return serverToken;
	}

	public void setServerToken(String serverToken) {
		this.serverToken = serverToken;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public OsType getOsType() {
		return osType;
	}

	public void setOsType(OsType osType) {
		this.osType = osType;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public Arch getArch() {
		return arch;
	}

	public void setArch(Arch arch) {
		this.arch = arch;
	}

}
