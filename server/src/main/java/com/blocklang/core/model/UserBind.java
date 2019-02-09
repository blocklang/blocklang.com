package com.blocklang.core.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.constant.converter.OauthSiteConverter;

@Entity
@Table(name="user_bind", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "site" }))
public class UserBind extends PartialIdField{

	private static final long serialVersionUID = -4064487359705338991L;

	@Column(name="user_id", nullable = false)
	private Integer userId;
	
	@Column(name="site", nullable = false, length = 2)
	@Convert(converter = OauthSiteConverter.class)
	private OauthSite site;
	
	@Column(name="open_id", nullable = false, length = 64)
	private String openId;
	
	@Column(name = "create_time", insertable = true, updatable = false, nullable = false)
	private LocalDateTime createTime = LocalDateTime.now();
	
	@Column(name = "last_update_time", insertable = false)
	private LocalDateTime lastUpdateTime;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public OauthSite getSite() {
		return site;
	}

	public void setSite(OauthSite site) {
		this.site = site;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
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
