package com.blocklang.marketplace.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ComponentRepoRegistry;

public interface ComponentRepoRegistryDao extends JpaRepository<ComponentRepoRegistry, Integer>{

	Page<ComponentRepoRegistry> findAllByLastPublishTimeNotNullAndNameContainingIgnoreCaseOrLastPublishTimeNotNullAndLabelContainingIgnoreCase(String queryForName, String queryForLabel, Pageable page);

	Page<ComponentRepoRegistry> findAllByLastPublishTimeNotNull(Pageable page);

}
