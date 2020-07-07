package com.blocklang.marketplace.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.constant.Constant;
import com.blocklang.marketplace.apirepo.RefData;
import com.blocklang.marketplace.apirepo.apiobject.ApiObject;
import com.blocklang.marketplace.apirepo.schema.ApiSchemaData;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ApiSchemaDao;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ApiSchema;

/**
 * 操作 Git ref 一层的抽象实现类
 * 
 * @author Zhengwei Jin
 *
 */
public abstract class AbstractApiRefService {

	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	@Autowired
	private ApiSchemaDao apiSchemaDao;
	
	public boolean isPublished(String gitUrl, Integer createUserId, String shortRefName) {
		Optional<ApiRepo> apiRepoOption = apiRepoDao.findByGitRepoUrlAndCreateUserId(gitUrl, createUserId);
		if(apiRepoOption.isEmpty()) {
			return false;
		}
		return apiRepoVersionDao.findByApiRepoIdAndVersion(apiRepoOption.get().getId(), shortRefName).isPresent();
	}

	protected <T extends ApiObject> ApiRepoVersion saveApiRepoVersion(Integer apiRepoId, RefData<T> refData) {
		ApiRepoVersion version = apiRepoVersionDao.findByApiRepoIdAndVersion(apiRepoId, refData.getShortRefName()).orElse(new ApiRepoVersion());
		version.setApiRepoId(apiRepoId);
		version.setVersion(refData.getShortRefName());
		version.setGitTagName(refData.getFullRefName());
		version.setName(refData.getRepoConfig().getName());
		version.setDisplayName(refData.getRepoConfig().getDisplayName());
		version.setDescription(refData.getRepoConfig().getDescription());
		version.setLastPublishTime(LocalDateTime.now());
		version.setCreateUserId(refData.getCreateUserId());
		version.setCreateTime(LocalDateTime.now());
		
		return apiRepoVersionDao.save(version);
	}
	
	protected void clearRefSchemas(Integer apiRepoVersionId) {
		apiSchemaDao.deleteByApiRepoVersionId(apiRepoVersionId);
	}

	protected void saveSchemas(ApiRepoVersion apiRepoVersion, List<ApiSchemaData> schemas, Integer createUserId) {
		List<ApiSchema> apiSchemas = new ArrayList<>();
		
		for(ApiSchemaData each : schemas) {
			apiSchemas.addAll(mapSchema(apiRepoVersion, createUserId, Constant.TREE_ROOT_ID, each));
		}
		
		apiSchemaDao.saveAll(apiSchemas);
	}

	private List<ApiSchema> mapSchema(ApiRepoVersion apiRepoVersion, Integer createUserId, Integer parentSchemaId, ApiSchemaData apiSchemaData) {
		List<ApiSchema> result = new ArrayList<>();
		ApiSchema apiSchema = new ApiSchema();
		apiSchema.setApiRepoVersionId(apiRepoVersion.getId());
		apiSchema.setName(apiSchemaData.getName());
		apiSchema.setCode(apiSchemaData.getCode());
		apiSchema.setType(apiSchemaData.getType());
		apiSchema.setDescription(apiSchemaData.getDescription());
		apiSchema.setParentId(parentSchemaId);
		apiSchema.setCreateTime(LocalDateTime.now());
		apiSchema.setCreateUserId(createUserId);
		result.add(apiSchema);
		
		ApiSchema savedApiSchema = apiSchemaDao.save(apiSchema);
		for(ApiSchemaData prop : apiSchemaData.getProperties()) {
			this.mapSchema(apiRepoVersion, createUserId, savedApiSchema.getId(), prop);
		}
		
		return result;
	}
	
}
