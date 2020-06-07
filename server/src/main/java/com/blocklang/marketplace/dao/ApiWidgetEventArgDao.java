package com.blocklang.marketplace.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ApiWidgetEventArg;

public interface ApiWidgetEventArgDao extends JpaRepository<ApiWidgetEventArg, Integer> {

	List<ApiWidgetEventArg> findAllByApiWidgetPropertyId(Integer apiWidgetPropertyId);

}
