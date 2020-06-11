package com.blocklang.marketplace.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ApiService;

public interface ApiServiceDao extends JpaRepository<ApiService, Integer>{

}
