package com.blocklang.marketplace.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ComponentRepoVersion;

public interface ComponentRepoVersionDao extends JpaRepository<ComponentRepoVersion, Integer> {

	List<ComponentRepoVersion> findAllByComponentRepoId(Integer componentRepoId);

}
