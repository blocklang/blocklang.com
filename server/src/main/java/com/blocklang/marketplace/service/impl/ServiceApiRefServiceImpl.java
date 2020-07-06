package com.blocklang.marketplace.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.Constant;
import com.blocklang.marketplace.apirepo.RefData;
import com.blocklang.marketplace.apirepo.apiobject.ApiObject;
import com.blocklang.marketplace.apirepo.apiobject.service.data.Parameter;
import com.blocklang.marketplace.apirepo.apiobject.service.data.RequestBody;
import com.blocklang.marketplace.apirepo.apiobject.service.data.Response;
import com.blocklang.marketplace.apirepo.apiobject.service.data.Schema;
import com.blocklang.marketplace.apirepo.apiobject.service.data.ServiceData;
import com.blocklang.marketplace.dao.ApiServiceDao;
import com.blocklang.marketplace.dao.ApiServiceParameterDao;
import com.blocklang.marketplace.dao.ApiServiceRequestBodyDao;
import com.blocklang.marketplace.dao.ApiServiceResponseDao;
import com.blocklang.marketplace.dao.ApiServiceSchemaDao;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ApiService;
import com.blocklang.marketplace.model.ApiServiceParameter;
import com.blocklang.marketplace.model.ApiServiceRequestBody;
import com.blocklang.marketplace.model.ApiServiceResponse;
import com.blocklang.marketplace.model.ApiServiceSchema;
import com.blocklang.marketplace.service.ServiceApiRefService;

@Service
public class ServiceApiRefServiceImpl extends AbstractApiRefService implements ServiceApiRefService {

	@Autowired
	private ApiServiceDao apiServiceDao;
	@Autowired
	private ApiServiceParameterDao apiServiceParameterDao;
	@Autowired
	private ApiServiceRequestBodyDao apiServiceRequestBodyDao;
	@Autowired
	private ApiServiceSchemaDao apiServiceSchemaDao;
	@Autowired
	private ApiServiceResponseDao apiServiceResponseDao;
	
	@Transactional
	@Override
	public <T extends ApiObject> void save(Integer apiRepoId, RefData<T> refData) {
		ApiRepoVersion apiRepoVersion = saveApiRepoVersion(apiRepoId, refData);
		
		if(refData.getShortRefName().equals("master")) {
			clearRefApis(apiRepoVersion.getId());
		}
		saveApiServices(apiRepoVersion, refData);
	}

	private <T extends ApiObject> void saveApiServices(ApiRepoVersion apiRepoVersion, RefData<T> refData) {
		List<T> services = refData.getApiObjects();
		for(T service : services) {
			saveService(apiRepoVersion.getId(), (ServiceData)service, refData.getCreateUserId());
		}
	}

	private void saveService(Integer apiRepoVersionId, ServiceData service, Integer createUserId) {
		ApiService apiService = new ApiService();
		apiService.setApiRepoVersionId(apiRepoVersionId);
		apiService.setCode(service.getCode());
		apiService.setName(service.getName());
		apiService.setUrl(service.getUrl());
		apiService.setHttpMethod(service.getHttpMethod());
		apiService.setDescription(service.getDescription());
		apiService.setCreateTime(LocalDateTime.now());
		apiService.setCreateUserId(createUserId);
		
		apiService = apiServiceDao.save(apiService);
		
		List<Parameter> parameters = service.getParameters();
		for(Parameter param : parameters) {
			saveServiceParameter(apiRepoVersionId, apiService.getId(), param);
		}
		
		saveRequestBody(apiRepoVersionId, apiService.getId(), service.getRequestBody(), createUserId);
		
		List<Response> responses = service.getResponses();
		for(Response each : responses) {
			saveResponse(apiRepoVersionId, apiService, each, createUserId);
		}
	}

	private void saveServiceParameter(Integer apiRepoVersionId, Integer apiServiceId, Parameter param) {
		ApiServiceParameter apiParam = new ApiServiceParameter();
		apiParam.setApiRepoVersionId(apiRepoVersionId);
		apiParam.setApiServiceId(apiServiceId);
		apiParam.setCode(param.getCode());
		apiParam.setName(param.getName());
		apiParam.setParamType(param.getIn());
		apiParam.setRequired(param.isRequired());
		apiParam.setDescription(param.getDescription());
		apiParam.setValueType(param.getSchema().getType());
		
		apiServiceParameterDao.save(apiParam);
	}

	private void saveRequestBody(Integer apiRepoVersionId, 
			Integer apiServiceId, 
			RequestBody requestBody, 
			Integer createUserId) {

		if(requestBody == null) {
			return;
		}
		
		Integer rootSchemaId = saveSchema(apiRepoVersionId, requestBody.getSchema(), createUserId);
		
		ApiServiceRequestBody apiServiceRequestBody = new ApiServiceRequestBody();
		apiServiceRequestBody.setApiRepoVersionId(apiRepoVersionId);
		apiServiceRequestBody.setApiServiceId(apiServiceId);
		apiServiceRequestBody.setCode(requestBody.getCode());
		apiServiceRequestBody.setName(requestBody.getContentType());
		apiServiceRequestBody.setDescription(requestBody.getDescription());
		apiServiceRequestBody.setValueType(requestBody.getSchema().getType());
		apiServiceRequestBody.setApiServiceSchemaId(rootSchemaId);
		
		apiServiceRequestBodyDao.save(apiServiceRequestBody);
	}

	private void saveResponse(Integer apiRepoVersionId, ApiService apiService, Response response,
			Integer createUserId) {
		Integer rootSchemaId = saveSchema(apiRepoVersionId, response.getSchema(), createUserId);
		
		ApiServiceResponse apiResponse = new ApiServiceResponse();
		apiResponse.setApiRepoVersionId(apiRepoVersionId);
		apiResponse.setApiServiceId(apiService.getId());
		apiResponse.setCode(response.getCode());
		// 不需要传入 name，将 statusCode 和 contentType 组合起来形成唯一标识
		apiResponse.setName(response.getStatusCode() + "_" + response.getContentType());
		apiResponse.setStatusCode(response.getStatusCode());
		apiResponse.setContentType(response.getContentType());
		apiResponse.setDescription(response.getDescription());
		apiResponse.setValueType(response.getSchema().getType());
		apiResponse.setApiServiceSchemaId(rootSchemaId);
		
		apiServiceResponseDao.save(apiResponse);
	}
	
	/**
	 * 保存数据结构，支持树状结构的数据。
	 * 
	 * @param apiRepoVersionId
	 * @param rootSchema
	 * @param createUserId
	 * @return 返回根节点的 id
	 */
	private Integer saveSchema(Integer apiRepoVersionId, Schema rootSchema, Integer createUserId) {
		String valueType = rootSchema.getType();
		if("object".equals(valueType)) {
			// 先创建一个根节点
			ApiServiceSchema apiRootSchema = new ApiServiceSchema();
			apiRootSchema.setApiRepoVersionId(apiRepoVersionId);
			apiRootSchema.setParentId(Constant.TREE_ROOT_ID);
			apiRootSchema.setName(rootSchema.getName());
			apiRootSchema.setType(rootSchema.getType());
			apiRootSchema.setDescription(rootSchema.getDescription());
			apiRootSchema.setCreateTime(LocalDateTime.now());
			apiRootSchema.setCreateUserId(createUserId);
			
			apiRootSchema = apiServiceSchemaDao.save(apiRootSchema);
			Integer parentId = apiRootSchema.getId();
			for(Schema each : rootSchema.getProperties()) {
				saveSchemaProperty(apiRepoVersionId, parentId, each, createUserId);
			}
			
			return apiRootSchema.getId();
		}
		
		return null;
	}

	// 支持有多层 properties，则递归处理
	private void saveSchemaProperty(Integer apiRepoVersionId, Integer parentId, Schema property, Integer createUserId) {
		ApiServiceSchema apiSchema = new ApiServiceSchema();
		apiSchema.setApiRepoVersionId(apiRepoVersionId);
		apiSchema.setParentId(parentId);
		apiSchema.setName(property.getName());
		apiSchema.setType(property.getType());
		apiSchema.setDescription(property.getDescription());
		apiSchema.setCreateTime(LocalDateTime.now());
		apiSchema.setCreateUserId(createUserId);
		
		apiSchema = apiServiceSchemaDao.save(apiSchema);
		
		for(Schema each : property.getProperties()) {
			saveSchemaProperty(apiRepoVersionId, apiSchema.getId(), each, createUserId);
		}
	}
	
	@Override
	public void clearRefApis(Integer apiRepoVersionId) {
		apiServiceResponseDao.deleteByApiRepoVersionId(apiRepoVersionId);
		apiServiceRequestBodyDao.deleteByApiRepoVersionId(apiRepoVersionId);
		apiServiceParameterDao.deleteByApiRepoVersionId(apiRepoVersionId);
		apiServiceDao.deleteByApiRepoVersionId(apiRepoVersionId);
	}

}
