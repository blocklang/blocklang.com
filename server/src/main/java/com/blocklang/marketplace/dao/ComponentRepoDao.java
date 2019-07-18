package com.blocklang.marketplace.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ComponentRepo;

public interface ComponentRepoDao extends JpaRepository<ComponentRepo, Integer>{

	Page<ComponentRepo> findAllByNameContainingIgnoreCaseOrLabelContainingIgnoreCase(String queryForName, String queryForLabel, Pageable page);

	Page<ComponentRepo> findAll(Pageable page);

	Optional<ComponentRepo> findByNameAndCreateUserId(String name, Integer userId);

	Optional<ComponentRepo> findByGitRepoUrlAndCreateUserId(String gitUrl, Integer userId);

	List<ComponentRepo> findAllByCreateUserIdOrderByName(Integer userId);

}
