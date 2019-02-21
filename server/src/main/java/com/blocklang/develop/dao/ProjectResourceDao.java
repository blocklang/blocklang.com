package com.blocklang.develop.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.model.ProjectResource;

public interface ProjectResourceDao extends JpaRepository<ProjectResource, Integer> {

	Optional<ProjectResource> findByProjectIdAndParentIdAndKeyIgnoreCase(Integer projectId, Integer parentModuleId, String resourceKey);

	List<ProjectResource> findByProjectIdAndParentIdOrderByResourceTypeAscSeqAsc(Integer projectId, Integer parentId);

}
