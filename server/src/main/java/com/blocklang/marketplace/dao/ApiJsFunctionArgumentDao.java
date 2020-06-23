package com.blocklang.marketplace.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.blocklang.marketplace.model.ApiJsFunctionArgument;

public interface ApiJsFunctionArgumentDao extends JpaRepository<ApiJsFunctionArgument, Integer> {

	@Modifying
	@Query("delete from ApiJsFunctionArgument where apiRepoVersionId = :apiRepoVersionId")
	void deleteByApiRepoVersionId(Integer apiRepoVersionId);

}
