package com.blocklang.marketplace.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ApiComponentAttr;

public interface ApiComponentAttrDao extends JpaRepository<ApiComponentAttr, Integer> {

	List<ApiComponentAttr> findAllByApiComponentIdOrderByCode(Integer apiComponentId);
}
