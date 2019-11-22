package com.blocklang.core.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.dao.PersisentLoginsDao;
import com.blocklang.core.dao.UserAvatarDao;
import com.blocklang.core.dao.UserBindDao;
import com.blocklang.core.dao.UserDao;
import com.blocklang.core.model.PersisentLogins;
import com.blocklang.core.model.UserAvatar;
import com.blocklang.core.model.UserBind;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.UserService;
import com.blocklang.core.util.IdGenerator;
import com.blocklang.core.util.LoginToken;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDao;
	@Autowired
	private UserAvatarDao userAvatarDao;
	@Autowired
	private UserBindDao userBindDao;
	@Autowired
	private PersisentLoginsDao persisentLoginsDao;
	
	@Transactional
	@Override
	public UserInfo create(UserInfo userInfo, UserBind userBind, List<UserAvatar> userAvatars) {
		userInfo.setCreateTime(LocalDateTime.now());
		userInfo.setLastSignInTime(LocalDateTime.now()); // 设置最近登录时间。
		UserInfo savedUserInfo = userDao.save(userInfo);
		Integer userId = savedUserInfo.getId();
		
		userAvatars.forEach(userAvatar -> {
			userAvatar.setCreateTime(LocalDateTime.now());
			userAvatar.setUserId(userId);
		});
		userAvatarDao.saveAll(userAvatars);
		
		userBind.setUserId(userId);
		userBind.setCreateTime(LocalDateTime.now());
		
		userBindDao.save(userBind);
		return savedUserInfo;
	}
	
	@Transactional
	@Override
	public UserInfo update(Integer savedUserId, UserInfo newUserInfo, List<UserAvatar> newUserAvatars, String... excludeUserInfoFields) {
		boolean excludeLoginName = false;
		if(excludeUserInfoFields != null && Arrays.stream(excludeUserInfoFields).anyMatch(item -> item.equals("loginName"))) {
			excludeLoginName = true;
		}
		
		boolean finalExcludeLoginName = excludeLoginName;
		return userDao.findById(savedUserId).map(savedUserInfo -> {
			if(!finalExcludeLoginName) {
				savedUserInfo.setLoginName(newUserInfo.getLoginName());
			}
			
			savedUserInfo.setNickname(newUserInfo.getNickname());
			savedUserInfo.setAvatarUrl(newUserInfo.getAvatarUrl());
			savedUserInfo.setEmail(newUserInfo.getEmail());
			savedUserInfo.setLocation(newUserInfo.getLocation());
			savedUserInfo.setWebsiteUrl(newUserInfo.getWebsiteUrl());
			savedUserInfo.setCompany(newUserInfo.getCompany());
			savedUserInfo.setBio(newUserInfo.getBio());
			savedUserInfo.setLastUpdateTime(LocalDateTime.now());
			// 重新设置最近登录时间
			savedUserInfo.setLastSignInTime(LocalDateTime.now());
			
			UserInfo updatedUserInfo = userDao.save(savedUserInfo);
			
			if(newUserAvatars.isEmpty()) {
				return updatedUserInfo;
			}
			
			List<UserAvatar> savedUserAvatars = userAvatarDao.findByUserId(savedUserId);
			savedUserAvatars.forEach(savedUserAvatar -> {
				for(UserAvatar newUserAvatar : newUserAvatars) {
					if(savedUserAvatar.getSizeType() == newUserAvatar.getSizeType()) {
						savedUserAvatar.setAvatarUrl(newUserAvatar.getAvatarUrl());
						break;
					}
				}
			});

			userAvatarDao.saveAll(savedUserAvatars);
			return updatedUserInfo;
		}).orElse(null);
	}

	// 如果值为空，则不缓存
	// 如果不增加 unless，则在用户第一次登录成功前（用户信息未保存到数据库中），
	// 调用该方法判断用户是否已存在，则就会将 null 缓存给给用户
	// 则即使用户登录成功，也每次都返回空值
	@Cacheable(value = "users", unless = "#result==null")
	@Override
	public Optional<UserInfo> findByLoginName(String owner) {
		return userDao.findByLoginName(owner);
	}

	@Override
	public Optional<UserInfo> findById(Integer userId) {
		return userDao.findById(userId);
	}

	@Override
	public UserInfo update(UserInfo newUserInfo) {
		return userDao.save(newUserInfo);
	}

	@Override
	public Optional<UserInfo> findByLoginToken(String loginToken) {
		return persisentLoginsDao.findByToken(loginToken).flatMap(persisentLogins -> {
			persisentLogins.setLastUsedTime(LocalDateTime.now());
			persisentLoginsDao.save(persisentLogins);
			return userDao.findByLoginName(persisentLogins.getLoginName());
		});
	}

	@Override
	public String generateLoginToken(OauthSite site, String loginName) {
		String internalToken = IdGenerator.uuid();
		LoginToken loginToken = new LoginToken();
		
		PersisentLogins pl = persisentLoginsDao.findByLoginName(loginName).map(login -> {
			login.setToken(internalToken);
			return login;
		}).orElseGet(() -> {
			PersisentLogins login = new PersisentLogins();
			login.setLoginName(loginName);
			login.setToken(internalToken);
			login.setLastUsedTime(LocalDateTime.now());
			return login;
		});
		
		persisentLoginsDao.save(pl);
		
		return loginToken.encode(site.getValue().toLowerCase(), internalToken);
	}
	
}
