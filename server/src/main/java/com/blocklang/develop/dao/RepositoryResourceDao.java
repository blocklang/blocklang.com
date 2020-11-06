package com.blocklang.develop.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.RepositoryResourceType;
import com.blocklang.develop.model.RepositoryResource;

public interface RepositoryResourceDao extends JpaRepository<RepositoryResource, Integer> {

	Optional<RepositoryResource> findByRepositoryIdAndParentIdAndResourceTypeAndAppTypeAndKeyIgnoreCase(
			Integer repositoryId, 
			Integer parentId, 
			RepositoryResourceType resourceType,
			AppType appType,
			String resourceKey);
	
	Optional<RepositoryResource> findByRepositoryIdAndParentIdAndResourceTypeAndKeyIgnoreCase(
			Integer repositoryId, 
			Integer parentId, 
			RepositoryResourceType resourceType,
			String resourceKey);

	Optional<RepositoryResource> findByRepositoryIdAndParentIdAndResourceTypeAndAppTypeAndNameIgnoreCase(
			Integer repositoryId,
			Integer parentId, 
			RepositoryResourceType resourceType, 
			AppType appType, 
			String resourceName);
	
	List<RepositoryResource> findByRepositoryIdAndParentIdOrderByResourceTypeAscSeqAsc(Integer repositoryId, Integer parentId);

	Optional<RepositoryResource> findFirstByRepositoryIdAndParentIdOrderBySeqDesc(Integer repositoryId, Integer parentId);

	List<RepositoryResource> findAllByRepositoryId(Integer repositoryId);

	List<RepositoryResource> findAllByRepositoryIdAndAppTypeAndResourceType(Integer repositoryId, AppType appType, RepositoryResourceType resourceType);

	List<RepositoryResource> findAllByRepositoryIdAndResourceType(Integer repositoryId, RepositoryResourceType resourceType);

}
