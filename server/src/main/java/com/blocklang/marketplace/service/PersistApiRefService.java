package com.blocklang.marketplace.service;

import com.blocklang.marketplace.apirepo.ApiObject;
import com.blocklang.marketplace.apirepo.RefData;

public interface PersistApiRefService<T extends ApiObject> {

	void save(RefData<T> refData);
	
	void clearRefApis(Integer apiRepoVersionId);

	boolean isPublished(String gitUrl, Integer createUserId, String shortRefName);
}
