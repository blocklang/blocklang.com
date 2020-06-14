package com.blocklang.marketplace.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.blocklang.marketplace.model.ApiServiceResponse;

public interface ApiServiceResponseDao extends JpaRepository<ApiServiceResponse, Integer>{

	@Modifying
	@Query("delete from ApiServiceResponse where apiRepoVersionId = :apiRepoVersionId")
	void deleteByApiRepoVersionId(Integer apiRepoVersionId);

}
