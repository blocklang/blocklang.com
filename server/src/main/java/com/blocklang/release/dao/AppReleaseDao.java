package com.blocklang.release.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.release.model.AppRelease;

public interface AppReleaseDao extends JpaRepository<AppRelease, Integer>{

	Optional<AppRelease> findFirstByAppIdOrderByIdDesc(Integer appId);

}
