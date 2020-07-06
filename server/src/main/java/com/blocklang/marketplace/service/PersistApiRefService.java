package com.blocklang.marketplace.service;

import com.blocklang.marketplace.apirepo.RefData;
import com.blocklang.marketplace.apirepo.apiobject.ApiObject;

public interface PersistApiRefService {

	<T extends ApiObject> void save(Integer apiRepoId, RefData<T> refData);
	
	void clearRefApis(Integer apiRepoVersionId);

	boolean isPublished(String gitUrl, Integer createUserId, String shortRefName);
}
