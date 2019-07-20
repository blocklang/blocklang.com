package com.blocklang.core.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.nimbusds.oauth2.sdk.util.StringUtils;

@Entity
@Table(name="user_info")
public class UserInfo extends PartialIdField{
	
	private static final long serialVersionUID = -5836364016416829732L;

	@Column(name="login_name", nullable = false, length = 32, unique = true)
	private String loginName;
	
	@Column(name="nickname", length = 64)
	private String nickname;
	
	@Column(name="enabled", nullable = false)
	private Boolean enabled = false;

	@Column(name="is_system_admin", nullable = false)
	private Boolean admin = false;

	@Column(name="avatar_url", length = 256, nullable = false)
	private String avatarUrl;

	@Column(name="email", length = 64, unique = true)
	private String email;
	
	@Column(name="mobile", length = 11, unique = true)
	private String mobile;
	
	@Column(name="location", length = 256)
	private String location;
	
	@Column(name="website_url", length = 128)
	private String websiteUrl;
	
	@Column(name="company", length = 128)
	private String company;
	
	@Column(name="bio", length = 512)
	private String bio;

	@Column(name="last_sign_in_time")
	private LocalDateTime lastSignInTime;
	
	@Column(name = "create_time", insertable = true, updatable = false, nullable = false)
	private LocalDateTime createTime;
	
	@Column(name = "last_update_time", insertable = false)
	private LocalDateTime lastUpdateTime;

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getEmail() {
		if(StringUtils.isBlank(email)) {
			// 注意，这个邮箱地址不会存到数据库中
			// 当使用 qq 用户登录时，不会有用户邮箱
			// 当使用 github 用户登录时，大部分会有用户邮箱，除了禁止用户访问邮箱信息的
			return this.loginName + "@blocklang.com";
		}
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getWebsiteUrl() {
		return websiteUrl;
	}

	public void setWebsiteUrl(String websiteUrl) {
		this.websiteUrl = websiteUrl;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}
	
	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public LocalDateTime getLastSignInTime() {
		return lastSignInTime;
	}

	public void setLastSignInTime(LocalDateTime lastSignInTime) {
		this.lastSignInTime = lastSignInTime;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

	public LocalDateTime getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

}
