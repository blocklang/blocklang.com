package com.blocklang.core.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.constant.AvatarSizeType;
import com.blocklang.core.constant.converter.AvatarSizeTypeConverter;

@Entity
@Table(name="user_avatar", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "size_type" }))
public class UserAvatar extends PartialIdField{

	private static final long serialVersionUID = -4340620729347834346L;

	@Column(name="user_id", nullable = false)
	private Integer userId;
	
	@Column(name="avatar_url", nullable = false, length = 256)
	private String avatarUrl;
	
	@Column(name="size_type", nullable = false, length = 2)
	@Convert(converter = AvatarSizeTypeConverter.class)
	private AvatarSizeType sizeType;

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

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public AvatarSizeType getSizeType() {
		return sizeType;
	}

	public void setSizeType(AvatarSizeType sizeType) {
		this.sizeType = sizeType;
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
