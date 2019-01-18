package com.blocklang.release.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.release.dao.AppReleaseDao;
import com.blocklang.release.model.AppRelease;
import com.blocklang.release.service.AppReleaseService;

@Service
public class AppReleaseServiceImpl implements AppReleaseService {

	@Autowired
	private AppReleaseDao appReleaseDao;
	
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

}
