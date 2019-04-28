package com.blocklang.develop.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.model.ProjectAuthorization;

public interface ProjectAuthorizationDao extends JpaRepository<ProjectAuthorization, Integer> {

	List<ProjectAuthorization> findAllByUserId(Integer userId);

	List<ProjectAuthorization> findAllByUserIdAndProjectId(Integer userId, Integer projectId);

}
