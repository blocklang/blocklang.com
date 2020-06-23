package com.blocklang.marketplace.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.blocklang.marketplace.model.ApiServiceParameter;

public interface ApiServiceParameterDao extends JpaRepository<ApiServiceParameter, Integer>{

	@Modifying
	@Query("delete from ApiServiceParameter where apiRepoVersionId = :apiRepoVersionId")
	void deleteByApiRepoVersionId(Integer apiRepoVersionId);

}
