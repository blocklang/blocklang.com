package com.blocklang.core.service.impl;

import java.time.LocalDateTime;
import java.util.List;

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
	public Integer create(UserInfo userInfo, UserBind userBind, List<UserAvatar> userAvatars) {
		Integer userId = userDao.save(userInfo).getId();
		
		userAvatars.forEach(userAvatar -> {
			userAvatar.setUserId(userId);
		});
		userAvatarDao.saveAll(userAvatars);
		
		userBind.setUserId(userId);
		userBindDao.save(userBind);
		return userId;
	}
	
	@Transactional
	@Override
	public void update(Integer savedUserId, UserInfo newUserInfo, List<UserAvatar> newUserAvatars) {
		userDao.findById(savedUserId).ifPresent(savedUserInfo -> {
			savedUserInfo.setLoginName(newUserInfo.getLoginName());
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
			
			userDao.save(savedUserInfo);
			
			if(newUserAvatars.isEmpty()) {
				return;
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
		});
	}
	
}
