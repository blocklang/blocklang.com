package com.blocklang.marketplace.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.blocklang.marketplace.model.ApiJsObject;

public interface ApiJsObjectDao extends JpaRepository<ApiJsObject, Integer>{

	@Modifying
	@Query("delete from ApiJsObject where apiRepoVersionId = :apiRepoVersionId")
	void deleteByApiRepoVersionId(Integer apiRepoVersionId);

}
