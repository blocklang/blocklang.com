package com.blocklang.marketplace.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.blocklang.marketplace.model.ApiWidgetEventArg;

public interface ApiWidgetEventArgDao extends JpaRepository<ApiWidgetEventArg, Integer> {

	List<ApiWidgetEventArg> findAllByApiWidgetPropertyId(Integer apiWidgetPropertyId);

	@Modifying
	@Query("delete from ApiWidgetEventArg where apiRepoVersionId = :apiRepoVersionId")
	void deleteByApiRepoVersionId(Integer apiRepoVersionId);

}
