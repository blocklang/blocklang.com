package com.blocklang.marketplace.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ApiWidgetProperty;

public interface ApiWidgetPropertyDao extends JpaRepository<ApiWidgetProperty, Integer> {

	List<ApiWidgetProperty> findAllByApiWidgetIdOrderByCode(Integer apiWidgetId);
}
