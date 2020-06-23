package com.blocklang.marketplace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.marketplace.apirepo.RefData;
import com.blocklang.marketplace.apirepo.webapi.data.JsFunction;
import com.blocklang.marketplace.apirepo.webapi.data.JsObjectData;
import com.blocklang.marketplace.apirepo.webapi.data.Parameter;
import com.blocklang.marketplace.dao.ApiJsFunctionArgumentDao;
import com.blocklang.marketplace.dao.ApiJsFunctionDao;
import com.blocklang.marketplace.dao.ApiJsObjectDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.data.RepoConfigJson;
import com.blocklang.marketplace.model.ApiJsFunction;
import com.blocklang.marketplace.model.ApiJsFunctionArgument;
import com.blocklang.marketplace.model.ApiJsObject;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.service.JsObjectApiRefService;

public class JsObjectApiRefServiceImplTest extends AbstractServiceTest{

	@Autowired
	private JsObjectApiRefService jsObjectApiRefService;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	@Autowired
	private ApiJsObjectDao apiJsObjectDao;
	@Autowired
	private ApiJsFunctionDao apiJsFunctionDao;
	@Autowired
	private ApiJsFunctionArgumentDao apiJsFunctionArgumentDao;
	
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
		repoConfig.setCategory("WebApi");
		
		List<JsObjectData> jsObjects = new ArrayList<>();
		JsObjectData jsObject1 = new JsObjectData();
		jsObject1.setCode("0001");
		jsObject1.setName("jsObject1");
		jsObject1.setDescription("js obj desc");
		
		JsFunction func1 = new JsFunction();
		func1.setCode("0001");
		func1.setName("func1");
		func1.setDescription("func desc");
		func1.setReturnType("void");
		
		Parameter param1 = new Parameter();
		param1.setCode("0001");
		param1.setName("param1");
		param1.setType("string");
		func1.setParameters(Collections.singletonList(param1));
		
		jsObject1.setFunctions(Collections.singletonList(func1));
		
		jsObjects.add(jsObject1);
		
		RefData<JsObjectData> refData = new RefData<>();
		refData.setGitUrl(gitUrl);
		refData.setShortRefName(version);
		refData.setFullRefName("refs/tags/v" + version);
		refData.setRepoConfig(repoConfig);
		refData.setApiObjects(jsObjects);
		refData.setCreateUserId(createUserId);
		
		Integer apiRepoId = 1;
		jsObjectApiRefService.save(apiRepoId, refData);
		
		Optional<ApiRepoVersion> expectedVersionOption = apiRepoVersionDao.findByApiRepoIdAndVersion(apiRepoId, version);
		assertThat(expectedVersionOption).isPresent();
		assertThat(expectedVersionOption.get()).hasNoNullFieldsOrProperties();
		
		List<ApiJsObject> expectedJsObjects = apiJsObjectDao.findAll();
		assertThat(expectedJsObjects).hasSize(1);
		assertThat(expectedJsObjects).first().hasNoNullFieldsOrPropertiesExcept("lastUpdateTime", "lastUpdateUserId");
		
		List<ApiJsFunction> expectedJsFunctions = apiJsFunctionDao.findAll();
		assertThat(expectedJsFunctions).hasSize(1);
		assertThat(expectedJsFunctions).first().hasNoNullFieldsOrProperties();
		
		List<ApiJsFunctionArgument> expectedFuncArgs = apiJsFunctionArgumentDao.findAll();
		assertThat(expectedFuncArgs).hasSize(1);
		assertThat(expectedFuncArgs).first().hasNoNullFieldsOrProperties();
	}

}
