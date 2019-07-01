package com.blocklang.marketplace.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ApiChangeLog;

public interface ApiChangeLogDao extends JpaRepository<ApiChangeLog, Integer> {

	List<ApiChangeLog> findAllByApiRepoId(Integer apiRepoId);

}
