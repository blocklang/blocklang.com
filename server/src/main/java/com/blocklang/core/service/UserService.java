package com.blocklang.core.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.core.constant.OauthSite;
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
	
	UserInfo update(UserInfo newUserInfo);
	
	/**
	 * 
	 * @param savedUserId
	 * @param newUserInfo
	 * @param newUserAvatars
	 * @param excludeUserInfoFields 目前只支持排除 loginName
	 * @return
	 */
	UserInfo update(Integer savedUserId, UserInfo newUserInfo, List<UserAvatar> newUserAvatars, String... excludeUserInfoFields);

	Optional<UserInfo> findByLoginName(String loginName);

	Optional<UserInfo> findById(Integer userId);

	/**
	 * 根据登录 token 获取用户信息。
	 * 
	 * 注意：每次获取用户信息时，都要更新 loginToken 的最近使用时间
	 * 
	 * @param loginToken
	 * @return
	 */
	Optional<UserInfo> findByLoginToken(String loginToken);
	
	/**
	 * 为登录用户生成唯一的登录 token
	 * 
	 * 注意：浏览器中存储的 token 和数据库中存储的 token 的值不相同
	 * 
	 * @param site
	 * @param loginName
	 * @return
	 */
	String generateLoginToken(OauthSite site, String loginName);
}
