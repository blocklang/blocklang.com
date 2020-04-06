package com.blocklang.marketplace.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ApiComponentAttrFunArg;

public interface ApiComponentAttrFunArgDao extends JpaRepository<ApiComponentAttrFunArg, Integer> {

	List<ApiComponentAttrFunArg> findAllByApiComponentAttrId(Integer apiComponentAttrId);

}
