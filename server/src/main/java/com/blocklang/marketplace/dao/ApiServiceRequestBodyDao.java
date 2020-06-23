package com.blocklang.marketplace.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.blocklang.marketplace.model.ApiServiceRequestBody;

public interface ApiServiceRequestBodyDao  extends JpaRepository<ApiServiceRequestBody, Integer>{

	@Modifying
	@Query("delete from ApiServiceRequestBody where apiRepoVersionId = :apiRepoVersionId")
	void deleteByApiRepoVersionId(Integer apiRepoVersionId);

}
