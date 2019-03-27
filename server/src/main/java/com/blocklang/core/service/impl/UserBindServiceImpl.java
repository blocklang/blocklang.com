package com.blocklang.core.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.dao.UserBindDao;
import com.blocklang.core.model.UserBind;
import com.blocklang.core.service.UserBindService;

@Service
public class UserBindServiceImpl implements UserBindService {

	@Autowired
	private UserBindDao userBindDao;
	
	@Override
	public Optional<UserBind> findBySiteAndOpenId(OauthSite site, String openId) {
		return userBindDao.findBySiteAndOpenId(site, openId);
	}

}
