package com.blocklang.marketplace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.constant.Constant;
import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.marketplace.apirepo.RefData;
import com.blocklang.marketplace.apirepo.service.data.Parameter;
import com.blocklang.marketplace.apirepo.service.data.RequestBody;
import com.blocklang.marketplace.apirepo.service.data.Response;
import com.blocklang.marketplace.apirepo.service.data.Schema;
import com.blocklang.marketplace.apirepo.service.data.ServiceData;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ApiServiceDao;
import com.blocklang.marketplace.dao.ApiServiceParameterDao;
import com.blocklang.marketplace.dao.ApiServiceRequestBodyDao;
import com.blocklang.marketplace.dao.ApiServiceResponseDao;
import com.blocklang.marketplace.dao.ApiServiceSchemaDao;
import com.blocklang.marketplace.data.RepoConfigJson;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ApiService;
import com.blocklang.marketplace.model.ApiServiceParameter;
import com.blocklang.marketplace.model.ApiServiceRequestBody;
import com.blocklang.marketplace.model.ApiServiceResponse;
import com.blocklang.marketplace.model.ApiServiceSchema;
import com.blocklang.marketplace.service.ServiceApiRefService;

public class ServiceApiRefServiceImplTest extends AbstractServiceTest{

	@Autowired
	private ServiceApiRefService serviceApiRefService;
	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
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
	
	@Test
	public void save_success() {
		Integer createUserId = 1;
		String gitUrl = "https://github.com/you/your-repo.git";
		String version = "1.0.0";
		RepoConfigJson repoConfig = new RepoConfigJson();
		repoConfig.setName("name1");
		repoConfig.setRepo("API");
		repoConfig.setDisplayName("display name");
		repoConfig.setDescription("description");
		repoConfig.setCategory("Service");
		
		List<ServiceData> services = new ArrayList<>();
		
		ServiceData service1 = new ServiceData();
		service1.setCode("0001");
		service1.setName("service1");
		service1.setUrl("url1");
		service1.setHttpMethod("GET");
		service1.setDescription("description1");
		
		Parameter parameter1 = new Parameter();
		parameter1.setCode("0001");
		parameter1.setName("param1");
		parameter1.setIn("query");
		parameter1.setRequired(true);
		parameter1.setDescription("description1");
		Schema paramSchema = new Schema();
		paramSchema.setType("string");
		parameter1.setSchema(paramSchema);
		
		service1.setParameters(Collections.singletonList(parameter1));
		
		RequestBody requestBody = new RequestBody();
		requestBody.setCode("0001");
		requestBody.setContentType("contentType1");
		requestBody.setDescription("description");
		
		// 测试 3 层结构
		// object
		//    object
		//        string a
		Schema bodySchema1 = new Schema();
		bodySchema1.setType("object");
			Schema bodySchema11 = new Schema();
			bodySchema11.setType("object");
				Schema bodySchema111 = new Schema();
				bodySchema111.setType("string");
				bodySchema111.setName("a");
		
		bodySchema11.setProperties(Collections.singletonList(bodySchema111));
		bodySchema1.setProperties(Collections.singletonList(bodySchema11));
		
		requestBody.setSchema(bodySchema1);
		service1.setRequestBody(requestBody);
		
		Response response1 = new Response();
		response1.setCode("0001");
		response1.setContentType("contentType1");
		response1.setStatusCode("200");
		response1.setDescription("response description");
		Schema responseSchema = new Schema();
		responseSchema.setType("string");
		response1.setSchema(responseSchema);
		service1.setResponses(Collections.singletonList(response1));
		
		services.add(service1);
		
		RefData<ServiceData> refData = new RefData<>();
		refData.setGitUrl(gitUrl);
		refData.setShortRefName(version);
		refData.setFullRefName("refs/tags/v" + version);
		refData.setRepoConfig(repoConfig);
		refData.setApiObjects(services);
		refData.setCreateUserId(createUserId);
		
		serviceApiRefService.save(refData);
		
		Optional<ApiRepo> expectedApiRepoOption = apiRepoDao.findByGitRepoUrlAndCreateUserId(gitUrl, createUserId);
		assertThat(expectedApiRepoOption).isPresent();
		assertThat(expectedApiRepoOption.get()).hasNoNullFieldsOrPropertiesExcept("lastUpdateTime", "lastUpdateUserId");
		
		Optional<ApiRepoVersion> expectedVersionOption = apiRepoVersionDao.findByApiRepoIdAndVersion(expectedApiRepoOption.get().getId(), version);
		assertThat(expectedVersionOption).isPresent();
		assertThat(expectedVersionOption.get()).hasNoNullFieldsOrProperties();
		
		List<ApiService> expectedApiServices = apiServiceDao.findAll();
		assertThat(expectedApiServices).hasSize(1);
		assertThat(expectedApiServices.get(0)).hasNoNullFieldsOrPropertiesExcept("lastUpdateTime", "lastUpdateUserId");
		
		List<ApiServiceParameter> expectedParameters = apiServiceParameterDao.findAll();
		assertThat(expectedParameters).hasSize(1);
		assertThat(expectedParameters.get(0)).hasNoNullFieldsOrPropertiesExcept("apiServiceSchemaId");
		assertThat(expectedParameters.get(0).getValueType()).isEqualTo("string");
		
		List<ApiServiceRequestBody> expectedRequestBodies = apiServiceRequestBodyDao.findAll();
		assertThat(expectedRequestBodies).hasSize(1);
		assertThat(expectedRequestBodies.get(0).getApiServiceSchemaId()).isNotNull();
		
		List<ApiServiceSchema> expectedRequestBodySchema = apiServiceSchemaDao.findAll();
		assertThat(expectedRequestBodySchema).hasSize(3);
		assertThat(expectedRequestBodySchema.get(0).getParentId()).isEqualTo(Constant.TREE_ROOT_ID);
		assertThat(expectedRequestBodySchema.get(1).getParentId()).isEqualTo(expectedRequestBodySchema.get(0).getId());
		assertThat(expectedRequestBodySchema.get(2).getParentId()).isEqualTo(expectedRequestBodySchema.get(1).getId());
		
		List<ApiServiceResponse> expectedResponses = apiServiceResponseDao.findAll();
		assertThat(expectedResponses).hasSize(1);
		assertThat(expectedResponses.get(0)).hasNoNullFieldsOrPropertiesExcept("apiServiceSchemaId");
	}
}
