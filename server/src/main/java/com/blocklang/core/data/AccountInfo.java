package com.blocklang.core.data;

import java.util.List;

import com.blocklang.core.model.UserAvatar;
import com.blocklang.core.model.UserBind;
import com.blocklang.core.model.UserInfo;

/**
 * 帐号信息。
 * 
 * 是一个视图对象，包含用户基本信息，用户头像列表，以及与第三方帐号的对应关系。
 * 
 * @author Zhengwei Jin
 *
 */
public class AccountInfo {
	private UserInfo userInfo;
	private List<UserAvatar> avatarList;
	private UserBind userBind;

	public AccountInfo(UserInfo userInfo, List<UserAvatar> avatarList, UserBind userBind) {
		this.userInfo = userInfo;
		this.avatarList = avatarList;
		this.userBind = userBind;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public List<UserAvatar> getAvatarList() {
		return avatarList;
	}

	public void setAvatarList(List<UserAvatar> avatarList) {
		this.avatarList = avatarList;
	}

	public UserBind getUserBind() {
		return userBind;
	}

	public void setUserBind(UserBind userBind) {
		this.userBind = userBind;
	}
}
