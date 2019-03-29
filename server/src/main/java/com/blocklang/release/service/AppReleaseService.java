package com.blocklang.release.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.release.model.AppRelease;

/**
 * APP 发行版业务逻辑结构
 * 
 * @author Zhengwei Jin
 */
public interface AppReleaseService {

	Optional<AppRelease> findById(int releaseId);
	
	Optional<AppRelease> findLatestReleaseApp(Integer appId);
	
	Optional<AppRelease> findLatestReleaseAppByAppName(String appName);

	Optional<AppRelease> findByAppIdAndVersion(Integer appId, String version);

	List<AppRelease> findAllByAppName(String appName);

}
