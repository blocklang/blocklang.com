package com.blocklang.release.service;

import java.util.Optional;

import com.blocklang.release.model.App;

public interface AppService {

	Optional<App> findById(int appId);

	Optional<App> findByAppName(String appName);

	Optional<App> findByProjectId(Integer projectId);

}
