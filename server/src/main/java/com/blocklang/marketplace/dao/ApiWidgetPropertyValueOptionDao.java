package com.blocklang.marketplace.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.blocklang.marketplace.model.ApiWidgetPropertyValueOption;

public interface ApiWidgetPropertyValueOptionDao extends JpaRepository<ApiWidgetPropertyValueOption, Integer> {

	@Modifying
	@Query("delete from ApiWidgetPropertyValueOption where apiRepoVersionId = :apiRepoVersionId")
	void deleteByApiRepoVersionId(Integer apiRepoVersionId);

}
