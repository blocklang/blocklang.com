package com.blocklang.marketplace.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ComponentRepoVersion;

public interface ComponentRepoVersionDao extends JpaRepository<ComponentRepoVersion, Integer> {

}
