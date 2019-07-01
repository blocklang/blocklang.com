package com.blocklang.marketplace.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ApiComponent;

public interface ApiComponentDao extends JpaRepository<ApiComponent, Integer> {

}
