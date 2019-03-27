package com.blocklang.core.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.dao.UserAvatarDao;
import com.blocklang.core.dao.UserBindDao;
import com.blocklang.core.dao.UserDao;
import com.blocklang.core.model.UserAvatar;
import com.blocklang.core.model.UserBind;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDao;
	@Autowired
	private UserAvatarDao userAvatarDao;
	@Autowired
	private UserBindDao userBindDao;
	
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
	
}
