package com.blocklang.marketplace.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.apirepo.RefData;
import com.blocklang.marketplace.apirepo.apiobject.ApiObject;
import com.blocklang.marketplace.apirepo.apiobject.widget.data.WidgetData;
import com.blocklang.marketplace.apirepo.apiobject.widget.data.WidgetEvent;
import com.blocklang.marketplace.apirepo.apiobject.widget.data.WidgetEventArgument;
import com.blocklang.marketplace.apirepo.apiobject.widget.data.WidgetProperty;
import com.blocklang.marketplace.apirepo.apiobject.widget.data.WidgetPropertyOption;
import com.blocklang.marketplace.constant.WidgetPropertyValueType;
import com.blocklang.marketplace.dao.ApiWidgetDao;
import com.blocklang.marketplace.dao.ApiWidgetEventArgDao;
import com.blocklang.marketplace.dao.ApiWidgetPropertyDao;
import com.blocklang.marketplace.dao.ApiWidgetPropertyValueOptionDao;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ApiWidget;
import com.blocklang.marketplace.model.ApiWidgetEventArg;
import com.blocklang.marketplace.model.ApiWidgetProperty;
import com.blocklang.marketplace.model.ApiWidgetPropertyValueOption;
import com.blocklang.marketplace.service.WidgetApiRefService;

@Service
public class WidgetApiRefServiceImpl extends AbstractApiRefService implements WidgetApiRefService {

	@Autowired
	private ApiWidgetDao apiWidgetDao;
	@Autowired
	private ApiWidgetPropertyDao apiWidgetPropertyDao;
	@Autowired
	private ApiWidgetPropertyValueOptionDao apiWidgetPropertyValueOptionDao;
	@Autowired
	private ApiWidgetEventArgDao apiWidgetEventArgDao;
	
	@Override
	@Transactional
	public <T extends ApiObject> void save(Integer apiRepoId, RefData<T> refData) {
		ApiRepoVersion apiRepoVersion = saveApiRepoVersion(apiRepoId, refData);
		if(refData.getShortRefName().equals("master")) {
			clearRefApis(apiRepoVersion.getId());
		}
		saveApiWidgets(apiRepoVersion, refData);
	}

	private <T extends ApiObject> void saveApiWidgets(ApiRepoVersion apiRepoVersion, RefData<T> refData) {
		List<T> widgets = refData.getApiObjects();
		for(T widget : widgets) {
			saveWidget(apiRepoVersion.getId(), (WidgetData)widget, refData.getCreateUserId());
		}
	}

	private void saveWidget(Integer apiRepoVersionId, WidgetData widget, Integer createUserId) {
		ApiWidget apiWidget = new ApiWidget();
		apiWidget.setApiRepoVersionId(apiRepoVersionId);
		apiWidget.setCode(widget.getCode());
		apiWidget.setName(widget.getName());
		apiWidget.setLabel(widget.getLabel());
		apiWidget.setDescription(widget.getDescription());
		apiWidget.setCreateTime(LocalDateTime.now());
		apiWidget.setCreateUserId(createUserId);
		apiWidget = apiWidgetDao.save(apiWidget);
		
		List<WidgetProperty> properties = widget.getProperties();
		for(WidgetProperty property : properties) {
			saveWidgetProperty(apiRepoVersionId, apiWidget.getId(), property);
		}
		
		List<WidgetEvent> events = widget.getEvents();
		for(WidgetEvent event : events) {
			saveWidgetEvent(apiRepoVersionId, apiWidget.getId(), event);
		}
	}

	private void saveWidgetProperty(Integer apiRepoVersionId, Integer apiWidgetId, WidgetProperty property) {
		ApiWidgetProperty apiWidgetProperty = new ApiWidgetProperty();
		apiWidgetProperty.setApiRepoVersionId(apiRepoVersionId);
		apiWidgetProperty.setApiWidgetId(apiWidgetId);
		apiWidgetProperty.setCode(property.getCode());
		apiWidgetProperty.setName(property.getName());
		apiWidgetProperty.setLabel(property.getLabel());
		apiWidgetProperty.setDescription(property.getDescription());
		apiWidgetProperty.setValueType(WidgetPropertyValueType.fromKey(property.getValueType()));
		apiWidgetProperty.setDefaultValue(property.getDefaultValue());
		apiWidgetProperty.setRequired(property.getRequired());
		
		apiWidgetProperty = apiWidgetPropertyDao.save(apiWidgetProperty);
		
		List<WidgetPropertyOption> options = property.getOptions();
		Integer apiWidgetPropertyId = apiWidgetProperty.getId();
		for(WidgetPropertyOption each : options) {
			saveWidgetPropertyValueOption(apiRepoVersionId, apiWidgetPropertyId, each);
		}
	}

	private void saveWidgetPropertyValueOption(Integer apiRepoVersionId, Integer apiWidgetPropertyId, WidgetPropertyOption option) {
		ApiWidgetPropertyValueOption apiWidgetPropertyValueOption = new ApiWidgetPropertyValueOption();
		apiWidgetPropertyValueOption.setApiRepoVersionId(apiRepoVersionId);
		apiWidgetPropertyValueOption.setApiWidgetPropertyId(apiWidgetPropertyId);
		apiWidgetPropertyValueOption.setCode(option.getCode());
		apiWidgetPropertyValueOption.setLabel(option.getLabel());
		apiWidgetPropertyValueOption.setValue(option.getValue());
		apiWidgetPropertyValueOption.setDescription(option.getDescription());
		apiWidgetPropertyValueOption.setValueDescription(option.getValueDescription());
		
		apiWidgetPropertyValueOptionDao.save(apiWidgetPropertyValueOption);
	}

	private void saveWidgetEvent(Integer apiRepoVersionId, Integer apiWidgetId, WidgetEvent event) {
		ApiWidgetProperty apiWidgetProperty = new ApiWidgetProperty();
		apiWidgetProperty.setApiRepoVersionId(apiRepoVersionId);
		apiWidgetProperty.setApiWidgetId(apiWidgetId);
		apiWidgetProperty.setCode(event.getCode());
		apiWidgetProperty.setName(event.getName());
		apiWidgetProperty.setLabel(event.getLabel());
		apiWidgetProperty.setDescription(event.getDescription());
		apiWidgetProperty.setValueType(WidgetPropertyValueType.fromKey(event.getValueType()));
		
		apiWidgetProperty = apiWidgetPropertyDao.save(apiWidgetProperty);

		List<WidgetEventArgument> args = event.getArguments();
		for(var i = 0; i < args.size(); i++) {
			WidgetEventArgument each = args.get(i);
			ApiWidgetEventArg arg = new ApiWidgetEventArg();
			arg.setApiRepoVersionId(apiRepoVersionId);
			arg.setApiWidgetPropertyId(apiWidgetProperty.getId());
			arg.setCode(each.getCode());
			arg.setName(each.getName());
			arg.setLabel(each.getLabel());
			arg.setValueType(WidgetPropertyValueType.fromKey(each.getValueType()));
			arg.setDefaultValue(each.getDefaultValue());
			arg.setDescription(each.getDescription());
			arg.setSeq(i + 1);
			apiWidgetEventArgDao.save(arg);
		}
	}

	@Override
	public void clearRefApis(Integer apiRepoVersionId) {
		apiWidgetEventArgDao.deleteByApiRepoVersionId(apiRepoVersionId);
		apiWidgetPropertyValueOptionDao.deleteByApiRepoVersionId(apiRepoVersionId);
		apiWidgetPropertyDao.deleteByApiRepoVersionId(apiRepoVersionId);
		apiWidgetDao.deleteByApiRepoVersionId(apiRepoVersionId);
	}
	
}
