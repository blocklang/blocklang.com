package com.blocklang.release.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.release.dao.AppDao;
import com.blocklang.release.model.App;
import com.blocklang.release.service.AppService;

@Service
public class AppServiceImpl implements AppService {

	@Autowired
	private AppDao appDao;

	@Override
	public Optional<App> findById(int appId) {
		return appDao.findById(appId);
	}

	@Override
	public Optional<App> findByAppName(String appName) {
		return appDao.findByAppName(appName);
	}

	@Override
	public Optional<App> findByProjectId(Integer projectId) {
		return appDao.findByProjectId(projectId);
	}

}
