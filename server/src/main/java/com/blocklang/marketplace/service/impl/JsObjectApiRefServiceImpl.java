package com.blocklang.marketplace.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.apirepo.ApiObject;
import com.blocklang.marketplace.apirepo.RefData;
import com.blocklang.marketplace.apirepo.webapi.data.JsFunction;
import com.blocklang.marketplace.apirepo.webapi.data.JsObjectData;
import com.blocklang.marketplace.apirepo.webapi.data.Parameter;
import com.blocklang.marketplace.dao.ApiJsFunctionArgumentDao;
import com.blocklang.marketplace.dao.ApiJsFunctionDao;
import com.blocklang.marketplace.dao.ApiJsObjectDao;
import com.blocklang.marketplace.model.ApiJsFunction;
import com.blocklang.marketplace.model.ApiJsFunctionArgument;
import com.blocklang.marketplace.model.ApiJsObject;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.service.JsObjectApiRefService;

@Service
public class JsObjectApiRefServiceImpl extends AbstractApiRefService implements JsObjectApiRefService {

	@Autowired
	private ApiJsObjectDao apiJsObjectDao;
	@Autowired
	private ApiJsFunctionDao apiJsFunctionDao;
	@Autowired
	private ApiJsFunctionArgumentDao apiJsFunctionArgumentDao;
	
	
	// FIXME: 先删除 master 分支中的所有内容
	// 更新 master 的 last_publish_time
	// 清除所有发布的 API
	
	@Override
	@Transactional
	public <T extends ApiObject> void save(Integer apiRepoId, RefData<T> refData) {
		ApiRepoVersion apiRepoVersion = saveApiRepoVersion(apiRepoId, refData);
		
		if(refData.getShortRefName().equals("master")) {
			clearRefApis(apiRepoVersion.getId());
		}
		saveApiJsObjects(apiRepoVersion, refData);
	}

	private <T extends ApiObject> void saveApiJsObjects(ApiRepoVersion apiRepoVersion, RefData<T> refData) {
		List<T> jsObjects = refData.getApiObjects();
		for(T jsObject : jsObjects) {
			saveJsObject(apiRepoVersion.getId(), (JsObjectData)jsObject, refData.getCreateUserId());
		}
	}

	private void saveJsObject(Integer apiRepoVersionId, JsObjectData jsObject, Integer createUserId) {
		ApiJsObject apiJsObject = new ApiJsObject();
		apiJsObject.setApiRepoVersionId(apiRepoVersionId);
		apiJsObject.setCode(jsObject.getCode());
		apiJsObject.setName(jsObject.getName());
		apiJsObject.setDescription(jsObject.getDescription());
		apiJsObject.setCreateTime(LocalDateTime.now());
		apiJsObject.setCreateUserId(createUserId);
		
		apiJsObject = apiJsObjectDao.save(apiJsObject);
		
		List<JsFunction> funcs = jsObject.getFunctions();
		for(JsFunction each : funcs) {
			saveJsFunction(apiRepoVersionId, apiJsObject.getId(), each);
		}
	}

	private void saveJsFunction(Integer apiRepoVersionId, Integer apiJsObjectId, JsFunction func) {
		ApiJsFunction apiJsFunc = new ApiJsFunction();
		apiJsFunc.setApiRepoVersionId(apiRepoVersionId);
		apiJsFunc.setApiJsObjectId(apiJsObjectId);
		apiJsFunc.setCode(func.getCode());
		apiJsFunc.setName(func.getName());
		apiJsFunc.setReturnType(func.getReturnType());
		apiJsFunc.setDescription(func.getDescription());
		
		apiJsFunc = apiJsFunctionDao.save(apiJsFunc);
		
		List<Parameter> args = func.getParameters();
		
		int seq = 1;
		for(Parameter arg : args) {
			ApiJsFunctionArgument apiArg = new ApiJsFunctionArgument();
			apiArg.setApiRepoVersionId(apiRepoVersionId);
			apiArg.setApiJsFunctionId(apiJsFunc.getId());
			apiArg.setCode(arg.getCode());
			apiArg.setName(arg.getName());
			apiArg.setType(arg.getType());
			apiArg.setOptional(arg.isOptional());
			apiArg.setVariable(arg.isVariable());
			apiArg.setSeq(seq);
			
			apiJsFunctionArgumentDao.save(apiArg);
			seq++;
		}
	}

	@Override
	public void clearRefApis(Integer apiRepoVersionId) {
		apiJsFunctionArgumentDao.deleteByApiRepoVersionId(apiRepoVersionId);
		apiJsFunctionDao.deleteByApiRepoVersionId(apiRepoVersionId);
		apiJsObjectDao.deleteByApiRepoVersionId(apiRepoVersionId);
	}

}
