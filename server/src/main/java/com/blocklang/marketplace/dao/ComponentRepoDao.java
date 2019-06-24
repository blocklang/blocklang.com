package com.blocklang.marketplace.dao;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ComponentRepo;

public interface ComponentRepoDao extends JpaRepository<ComponentRepo, Integer>{

	Page<ComponentRepo> findAllByLastPublishTimeNotNullAndNameContainingIgnoreCaseOrLastPublishTimeNotNullAndLabelContainingIgnoreCase(String queryForName, String queryForLabel, Pageable page);

	Page<ComponentRepo> findAllByLastPublishTimeNotNull(Pageable page);

	Optional<ComponentRepo> findByNameAndCreateUserId(String name, Integer userId);

}
