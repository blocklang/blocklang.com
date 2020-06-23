package com.blocklang.marketplace.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.blocklang.marketplace.model.ApiService;

public interface ApiServiceDao extends JpaRepository<ApiService, Integer>{

	@Modifying
	@Query("delete from ApiService where apiRepoVersionId = :apiRepoVersionId")
	void deleteByApiRepoVersionId(Integer apiRepoVersionId);

}
