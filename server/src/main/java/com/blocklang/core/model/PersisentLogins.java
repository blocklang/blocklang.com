package com.blocklang.core.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "persistent_logins")
public class PersisentLogins extends PartialIdField {

	private static final long serialVersionUID = 4413595086315827359L;
	
	@Column(name="login_name", nullable = false, length = 32, unique = true)
	private String loginName;
	
	@Column(name="token", nullable = false, length = 64, unique = true)
	private String token;
	
	@Column(name = "last_used_time", nullable = false)
	private LocalDateTime lastUsedTime;

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public LocalDateTime getLastUsedTime() {
		return lastUsedTime;
	}

	public void setLastUsedTime(LocalDateTime lastUsedTime) {
		this.lastUsedTime = lastUsedTime;
	}
	
}