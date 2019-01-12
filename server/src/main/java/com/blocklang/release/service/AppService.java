package com.blocklang.release.service;

import java.util.Optional;

import com.blocklang.release.model.App;

public interface AppService {

	Optional<App> findByRegistratioToken(String registrationToken);

	Optional<App> find(int appId);

}
