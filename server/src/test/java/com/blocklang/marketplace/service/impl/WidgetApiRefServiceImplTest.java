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
import com.blocklang.marketplace.apirepo.widget.data.WidgetData;
import com.blocklang.marketplace.apirepo.widget.data.WidgetEvent;
import com.blocklang.marketplace.apirepo.widget.data.WidgetEventArgument;
import com.blocklang.marketplace.apirepo.widget.data.WidgetProperty;
import com.blocklang.marketplace.apirepo.widget.data.WidgetPropertyOption;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ApiWidgetDao;
import com.blocklang.marketplace.dao.ApiWidgetEventArgDao;
import com.blocklang.marketplace.dao.ApiWidgetPropertyDao;
import com.blocklang.marketplace.dao.ApiWidgetPropertyValueOptionDao;
import com.blocklang.marketplace.data.RepoConfigJson;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ApiWidget;
import com.blocklang.marketplace.model.ApiWidgetEventArg;
import com.blocklang.marketplace.model.ApiWidgetProperty;
import com.blocklang.marketplace.model.ApiWidgetPropertyValueOption;
import com.blocklang.marketplace.service.WidgetApiRefService;

public class WidgetApiRefServiceImplTest extends AbstractServiceTest{

	@Autowired
	private WidgetApiRefService widgetApiRefService;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	@Autowired
	private ApiWidgetDao apiWidgetDao;
	@Autowired
	private ApiWidgetPropertyDao apiWidgetPropertyDao;
	@Autowired
	private ApiWidgetPropertyValueOptionDao apiWidgetPropertyValueOptionDao;
	@Autowired
	private ApiWidgetEventArgDao apiWidgetEventArgDao;
	
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
		repoConfig.setCategory("Widget");
		
		List<WidgetData> widgets = new ArrayList<>();
		WidgetData widget1 = new WidgetData();
		// 注意，这里的 ID 应该是数据表中的 id，而不是时间戳
		widget1.setCode("0001");
		widget1.setName("widget1");
		widget1.setLabel("label1");
		widget1.setDescription("description1");
		
		WidgetProperty property1 = new WidgetProperty();
		property1.setCode("0001");
		property1.setName("prop1");
		property1.setValueType("string");
		property1.setLabel("prop 1");
		property1.setDefaultValue("default value 1");
		property1.setDescription("description 1");
		WidgetPropertyOption propOption1 = new WidgetPropertyOption();
		propOption1.setCode("0001");
		propOption1.setLabel("option1");
		propOption1.setValue("optionValue1");
		propOption1.setDescription("option description");
		propOption1.setValueDescription("value description");
		property1.setOptions(Collections.singletonList(propOption1));
		widget1.setProperties(Collections.singletonList(property1));
		
		WidgetEvent event1 = new WidgetEvent();
		event1.setCode("0002");
		event1.setName("event1");
		event1.setLabel("event 1");
		event1.setDescription("description 2");
		
		WidgetEventArgument eventArg1 = new WidgetEventArgument();
		eventArg1.setCode("0001");
		eventArg1.setName("eventArg1");
		eventArg1.setValueType("function");
		eventArg1.setLabel("event label");
		eventArg1.setDefaultValue("default value");
		eventArg1.setDescription("option description");
		
		event1.setArguments(Collections.singletonList(eventArg1));
		widget1.setEvents(Collections.singletonList(event1));
		
		widgets.add(widget1);
		
		RefData<WidgetData> refData = new RefData<>();
		refData.setGitUrl(gitUrl);
		refData.setShortRefName(version);
		refData.setFullRefName("refs/tags/v" + version);
		refData.setRepoConfig(repoConfig);
		refData.setApiObjects(widgets);
		refData.setCreateUserId(createUserId);
		
		Integer apiRepoId = 1;
		widgetApiRefService.save(apiRepoId, refData);
		
		Optional<ApiRepoVersion> expectedVersionOption = apiRepoVersionDao.findByApiRepoIdAndVersion(apiRepoId, version);
		assertThat(expectedVersionOption).isPresent();
		assertThat(expectedVersionOption.get()).hasNoNullFieldsOrProperties();
		
		Optional<ApiWidget> apiWidgetOption = apiWidgetDao.findByApiRepoVersionIdAndNameIgnoreCase(expectedVersionOption.get().getId(), "widget1");
		assertThat(apiWidgetOption).isPresent();
		assertThat(apiWidgetOption.get()).hasNoNullFieldsOrPropertiesExcept("lastUpdateTime", "lastUpdateUserId");
		
		ApiWidget apiWidget = apiWidgetOption.get();
		List<ApiWidgetProperty> widgetProperties = apiWidgetPropertyDao.findAllByApiWidgetIdOrderByCode(apiWidget.getId());
		assertThat(widgetProperties).hasSize(2);
		assertThat(widgetProperties).first().hasNoNullFieldsOrProperties();
		assertThat(widgetProperties).last().hasNoNullFieldsOrPropertiesExcept("defaultValue");
		
		List<ApiWidgetPropertyValueOption> options = apiWidgetPropertyValueOptionDao.findAll();
		assertThat(options).hasSize(1);
		assertThat(options.get(0)).hasNoNullFieldsOrProperties();
		
		List<ApiWidgetEventArg> eventArgs = apiWidgetEventArgDao.findAll();
		assertThat(eventArgs).hasSize(1);
		assertThat(eventArgs.get(0)).hasNoNullFieldsOrProperties();
	}
	
	@Test
	public void save_rollback() {
		
	}
}
