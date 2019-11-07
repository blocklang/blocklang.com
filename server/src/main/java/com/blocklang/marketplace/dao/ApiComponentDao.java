package com.blocklang.marketplace.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ApiComponent;

public interface ApiComponentDao extends JpaRepository<ApiComponent, Integer> {

	List<ApiComponent> findAllByApiRepoVersionId(Integer apiVersionId);

}
