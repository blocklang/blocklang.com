package com.blocklang.marketplace.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ApiServiceRequestBody;

public interface ApiServiceRequestBodyDao  extends JpaRepository<ApiServiceRequestBody, Integer>{

}
