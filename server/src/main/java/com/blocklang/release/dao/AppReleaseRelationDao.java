package com.blocklang.release.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.release.model.AppReleaseRelation;

public interface AppReleaseRelationDao extends JpaRepository<AppReleaseRelation, Integer>{

	List<AppReleaseRelation> findByAppReleaseId(int appReleaseId);

}
