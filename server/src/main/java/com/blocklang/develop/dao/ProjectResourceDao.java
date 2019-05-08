package com.blocklang.develop.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.model.ProjectResource;

public interface ProjectResourceDao extends JpaRepository<ProjectResource, Integer> {

	Optional<ProjectResource> findByProjectIdAndParentIdAndResourceTypeAndAppTypeAndKeyIgnoreCase(
			Integer projectId, 
			Integer parentId, 
			ProjectResourceType resourceType,
			AppType appType,
			String resourceKey);

	Optional<ProjectResource> findByProjectIdAndParentIdAndResourceTypeAndAppTypeAndNameIgnoreCase(
			Integer projectId,
			Integer parentId, 
			ProjectResourceType resourceType, 
			AppType appType, 
			String resourceName);
	
	List<ProjectResource> findByProjectIdAndParentIdOrderByResourceTypeAscSeqAsc(Integer projectId, Integer parentId);

	Optional<ProjectResource> findFirstByProjectIdAndParentIdOrderBySeqDesc(Integer projectId, Integer parentId);

	List<ProjectResource> findAllByProjectId(Integer projectId);

}
