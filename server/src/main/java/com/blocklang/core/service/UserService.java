package com.blocklang.core.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.core.model.UserAvatar;
import com.blocklang.core.model.UserBind;
import com.blocklang.core.model.UserInfo;

public interface UserService {

	/**
	 * 创建新用户
	 * 
	 * 默认添加最近登录时间为当前时间。
	 * 
	 * @param userInfo 用户基本信息
	 * @param userBind 用户与第三方社交帐号绑定信息
	 * @param userAvatars 用户头像信息
	 * @return 新建的用户信息
	 */
	UserInfo create(UserInfo userInfo, UserBind userBind, List<UserAvatar> userAvatars);
	
	UserInfo update(Integer savedUserId, UserInfo newUserInfo, List<UserAvatar> newUserAvatars);

	Optional<UserInfo> findByLoginName(String owner);
}
