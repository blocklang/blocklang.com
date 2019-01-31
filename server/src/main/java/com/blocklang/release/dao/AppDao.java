package com.blocklang.release.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.release.model.App;

public interface AppDao extends JpaRepository<App, Integer>{

	Optional<App> findByRegistrationToken(String registrationToken);

	Optional<App> findByAppName(String appName);

	Optional<App> findByProjectId(Integer id);

}
