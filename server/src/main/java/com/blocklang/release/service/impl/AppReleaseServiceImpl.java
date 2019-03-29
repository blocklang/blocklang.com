package com.blocklang.release.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.release.dao.AppDao;
import com.blocklang.release.dao.AppReleaseDao;
import com.blocklang.release.model.AppRelease;
import com.blocklang.release.service.AppReleaseService;

@Service
public class AppReleaseServiceImpl implements AppReleaseService {

	@Autowired
	private AppReleaseDao appReleaseDao;
	@Autowired
	private AppDao appDao;
	
	@Override
	public Optional<AppRelease> findById(int releaseId) {
		return appReleaseDao.findById(releaseId);
	}
	
	@Override
	public Optional<AppRelease> findLatestReleaseApp(Integer appId) {
		return appReleaseDao.findFirstByAppIdOrderByIdDesc(appId);
	}

	@Override
	public Optional<AppRelease> findByAppIdAndVersion(Integer appId, String version) {
		return appReleaseDao.findByAppIdAndVersion(appId, version);
	}

	@Override
	public List<AppRelease> findAllByAppName(String appName) {
		List<AppRelease> result = appDao.findByAppName(appName).map(app -> {
			return appReleaseDao.findByAppIdOrderByIdDesc(app.getId());
		}).orElse(Collections.emptyList());
		
		result.forEach(release -> {
			release.setName(appName);
		});
		
		return result;
	}

	@Override
	public Optional<AppRelease> findLatestReleaseAppByAppName(String appName) {
		return appDao.findByAppName(appName).flatMap(app -> {
			return appReleaseDao.findFirstByAppIdOrderByIdDesc(app.getId());
		});
	}

}
