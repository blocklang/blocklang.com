package com.blocklang.release.service;

import java.util.Optional;

import com.blocklang.release.model.App;

public interface AppService {

	Optional<App> findByRegistrationToken(String registrationToken);

	Optional<App> findById(int appId);

}
