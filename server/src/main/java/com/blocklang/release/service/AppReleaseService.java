package com.blocklang.release.service;

import java.util.Optional;

import com.blocklang.release.model.AppRelease;

/**
 * APP 发行版业务逻辑结构
 * 
 * @author Zhengwei Jin
 */
public interface AppReleaseService {

	Optional<AppRelease> findLatestReleaseApp(Integer appId);

	Optional<AppRelease> find(int releaseId);

}
