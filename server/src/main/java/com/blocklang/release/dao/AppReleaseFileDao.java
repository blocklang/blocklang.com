package com.blocklang.release.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.release.model.AppReleaseFile;

public interface AppReleaseFileDao  extends JpaRepository<AppReleaseFile, Integer> {

	List<AppReleaseFile> findByAppReleaseId(int appReleaseId);

}
