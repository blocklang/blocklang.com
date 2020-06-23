package com.blocklang.marketplace.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.blocklang.marketplace.model.ApiJsFunction;

public interface ApiJsFunctionDao extends JpaRepository<ApiJsFunction, Integer>{

	@Modifying
	@Query("delete from ApiJsFunction where apiRepoVersionId = :apiRepoVersionId")
	void deleteByApiRepoVersionId(Integer apiRepoVersionId);

}
