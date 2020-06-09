package com.blocklang.marketplace.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import com.blocklang.marketplace.model.ApiRepo;
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
	
	@Override
	@Transactional
	public void save(RefData<JsObjectData> refData) {
		ApiRepo apiRepo = saveApoRepo(refData);
		ApiRepoVersion apiRepoVersion = saveApiRepoVersion(apiRepo.getId(), refData);
		saveApiJsObjects(apiRepoVersion, refData);
	}

	private void saveApiJsObjects(ApiRepoVersion apiRepoVersion, RefData<JsObjectData> refData) {
		List<JsObjectData> jsObjects = refData.getApiObjects();
		for(JsObjectData jsObject : jsObjects) {
			saveJsObject(apiRepoVersion.getId(), jsObject, refData.getCreateUserId());
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
			saveJsFunction(apiJsObject.getId(), each);
		}
	}

	private void saveJsFunction(Integer apiJsObjectId, JsFunction func) {
		ApiJsFunction apiJsFunc = new ApiJsFunction();
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

}
