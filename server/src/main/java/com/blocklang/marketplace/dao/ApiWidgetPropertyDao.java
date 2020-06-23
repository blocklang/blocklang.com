package com.blocklang.marketplace.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.blocklang.marketplace.model.ApiWidgetProperty;

public interface ApiWidgetPropertyDao extends JpaRepository<ApiWidgetProperty, Integer> {

	List<ApiWidgetProperty> findAllByApiWidgetIdOrderByCode(Integer apiWidgetId);

	@Modifying
	@Query("delete from ApiWidgetProperty where apiRepoVersionId = :apiRepoVersionId")
	void deleteByApiRepoVersionId(Integer apiRepoVersionId);
}
