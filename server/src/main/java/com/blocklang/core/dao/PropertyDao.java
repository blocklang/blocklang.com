package com.blocklang.core.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.core.model.CmProperty;

public interface PropertyDao extends JpaRepository<CmProperty, Integer>{

	Optional<CmProperty> findByKeyAndParentIdAndValid(String key, Integer parentId, boolean valid);

}
