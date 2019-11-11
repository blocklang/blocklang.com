package com.blocklang.marketplace.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ApiComponent;

public interface ApiComponentDao extends JpaRepository<ApiComponent, Integer> {

	List<ApiComponent> findAllByApiRepoVersionId(Integer apiRepoVersionId);

	Optional<ApiComponent> findByApiRepoVersionIdAndNameIgnoreCase(Integer apiRepoVersionId, String name);
}
