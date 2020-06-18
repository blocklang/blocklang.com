package com.blocklang.marketplace.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.marketplace.model.ComponentRepoVersion;

public interface ComponentRepoVersionDao extends JpaRepository<ComponentRepoVersion, Integer> {

	List<ComponentRepoVersion> findAllByComponentRepoId(Integer componentRepoId);

	Optional<ComponentRepoVersion> findByComponentIdAndVersion(Integer componentId, Integer createUserId);

}
