package com.blocklang.marketplace.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.marketplace.constant.Language;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.dao.ApiChangeLogDao;
import com.blocklang.marketplace.dao.ApiComponentAttrDao;
import com.blocklang.marketplace.dao.ApiComponentAttrFunArgDao;
import com.blocklang.marketplace.dao.ApiComponentAttrValOptDao;
import com.blocklang.marketplace.dao.ApiComponentDao;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.data.ApiJson;
import com.blocklang.marketplace.data.ComponentJson;
import com.blocklang.marketplace.data.changelog.Change;
import com.blocklang.marketplace.data.changelog.ChangeLog;
import com.blocklang.marketplace.data.changelog.ComponentChangeLogs;
import com.blocklang.marketplace.data.changelog.NewWidgetChange;
import com.blocklang.marketplace.data.changelog.WidgetEvent;
import com.blocklang.marketplace.data.changelog.WidgetEventArgument;
import com.blocklang.marketplace.data.changelog.WidgetProperty;
import com.blocklang.marketplace.data.changelog.WidgetPropertyOption;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.release.constant.ReleaseResult;

public class ApiChangeLogsSetupGroupTaskTest extends AbstractServiceTest {

	private MarketplacePublishContext context;

	@Autowired
	private ComponentRepoDao componentRepoDao;
	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	@Autowired
	private ApiComponentDao apiComponentDao;
	@Autowired
	private ApiComponentAttrDao apiComponentAttrDao;
	@Autowired
	private ApiComponentAttrValOptDao apiComponentAttrValOptDao;
	@Autowired
	private ApiComponentAttrFunArgDao apiComponentAttrFunArgDao;
	@Autowired
	private ApiChangeLogDao apiChangeLogDao;

	@BeforeEach
	public void setup(@TempDir Path folder) throws IOException {
		preparePublishTask(folder.toString());
		prepareApiJson();
		prepareComponentJson();
	}

	private void prepareComponentJson() {
		ComponentJson componentJson = new ComponentJson();
		componentJson.setName("component-a");
		componentJson.setVersion("0.1.0");
		componentJson.setDisplayName("Component A");
		componentJson.setDescription("component description");
		componentJson.setCategory(RepoCategory.WIDGET.getValue());
		componentJson.setLanguage(Language.TYPESCRIPT.getValue());
		context.setComponentJson(componentJson);
		context.setComponentRepoLatestTagName("v0.1.0");
		context.setComponentRepoLatestVersion("0.1.0");
	}

	private void prepareApiJson() {
		ApiJson apiJson = new ApiJson();
		apiJson.setName("api-a");
		apiJson.setVersion("0.1.0");
		apiJson.setDisplayName("API A");
		apiJson.setDescription("api description");
		apiJson.setCategory(RepoCategory.WIDGET.getValue());
		context.setApiJson(apiJson);
		
		context.setApiRepoRefName(Constants.R_TAGS + "v0.1.0");
		context.setAllApiRepoTagNames(Collections.singletonList(Constants.R_TAGS + "v0.1.0"));
		context.setApiRepoVersions(Arrays.asList(new String[] {"0.1.0"}));
	}

	private void preparePublishTask(String folder) {
		ComponentRepoPublishTask publishTask = new ComponentRepoPublishTask();
		publishTask.setGitUrl("https://a.com/user/component.repo.git");
		publishTask.setStartTime(LocalDateTime.now());
		publishTask.setPublishResult(ReleaseResult.INITED);
		publishTask.setCreateUserId(1);
		publishTask.setCreateTime(LocalDateTime.now());
		
		context = new MarketplacePublishContext(folder, publishTask);
		context.parseApiGitUrl("https://a.com/user/api.repo.git");
		
		TaskLogger logger = new TaskLogger(context.getRepoPublishLogFile());
		context.setLogger(logger);
	}
	
	@Test
	public void run_one_change_with_one_new_widget() {
		prepareChangeLogs_1();
		
		ApiChangeLogsSetupGroupTask task = new ApiChangeLogsSetupGroupTask(
				context,
				componentRepoDao,
				componentRepoVersionDao,
				apiRepoDao,
				apiRepoVersionDao,
				apiComponentDao,
				apiComponentAttrDao,
				apiComponentAttrValOptDao,
				apiComponentAttrFunArgDao,
				apiChangeLogDao);
		assertThat(task.run()).isPresent();

		assertThat(countRowsInTable("API_REPO")).isEqualTo(1);
		assertThat(countRowsInTable("API_REPO_VERSION")).isEqualTo(1);
		assertThat(countRowsInTable("COMPONENT_REPO")).isEqualTo(1);
		assertThat(countRowsInTable("COMPONENT_REPO_VERSION")).isEqualTo(1);
		assertThat(countRowsInTable("API_COMPONENT")).isEqualTo(1);
		assertThat(countRowsInTable("API_COMPONENT_ATTR")).isEqualTo(2);
		assertThat(countRowsInTable("API_COMPONENT_ATTR_VAL_OPT")).isEqualTo(1);
		assertThat(countRowsInTable("API_COMPONENT_ATTR_FUN_ARG")).isEqualTo(1);
		assertThat(countRowsInTable("API_CHANGELOG")).isEqualTo(1);
		
		assertThat(apiComponentDao.findAll().get(0).getCode()).isEqualTo("0001");
		assertThat(apiComponentAttrDao.findAll()
				.stream()
				.map(apiComponentAttr -> apiComponentAttr.getCode()).collect(Collectors.toList()))
			.startsWith("0001")
			.endsWith("0002");
		assertThat(apiComponentAttrValOptDao.findAll().get(0).getCode()).isEqualTo("0001");
		assertThat(apiComponentAttrFunArgDao.findAll().get(0).getCode()).isEqualTo("0001");
	}
	
	private void prepareChangeLogs_1() {
		List<ComponentChangeLogs> allComponentChangeLogs = new ArrayList<ComponentChangeLogs>();
		ComponentChangeLogs componentChangeLogs = new ComponentChangeLogs();
		componentChangeLogs.setComponentName("components/text-input");
		List<ChangeLog> changeLogs = new ArrayList<ChangeLog>();
		ChangeLog changeLog = new ChangeLog();
		changeLog.setId("1");
		changeLog.setAuthor("jack");
		changeLog.setFileName("0_1_0.json");
		changeLog.setMd5Sum("md5Sum");
		List<Change> changes = new ArrayList<Change>();
		NewWidgetChange change1 = new NewWidgetChange();
		change1.setName("Widget1");
		change1.setLabel("Widget 1");
		change1.setCanHasChildren(false);
		
		List<WidgetProperty> properties = new ArrayList<WidgetProperty>();
		WidgetProperty property1 = new WidgetProperty();
		property1.setName("Prop1");
		property1.setLabel("Prop 1");
		property1.setDefaultValue("Default value 1");
		property1.setValueType("string");
		List<WidgetPropertyOption> propertyOptions = new ArrayList<WidgetPropertyOption>();
		WidgetPropertyOption propertyOption1 = new WidgetPropertyOption();
		propertyOption1.setLabel("optionLabel1");
		propertyOption1.setDescription("optionDescription1");
		propertyOption1.setValue("optionValue1");
		propertyOptions.add(propertyOption1);
		property1.setOptions(propertyOptions);
		properties.add(property1);
		change1.setProperties(properties);
		
		List<WidgetEvent> events = new ArrayList<WidgetEvent>();
		WidgetEvent event1 = new WidgetEvent();
		event1.setName("Event1");
		event1.setLabel("Event 1");
		event1.setValueType("function");
		List<WidgetEventArgument> eventArguments = new ArrayList<WidgetEventArgument>();
		WidgetEventArgument eventArgument1 = new WidgetEventArgument();
		eventArgument1.setName("eventArgumentName");
		eventArgument1.setLabel("Event Argument Label");
		eventArgument1.setDefaultValue("eventArgumentDefaultValue");
		eventArgument1.setValueType("string");
		eventArguments.add(eventArgument1);
		event1.setArguments(eventArguments);
		events.add(event1);
		change1.setEvents(events);
		
		changes.add(change1);
		
		changeLog.setVersion("0.1.0");
		changeLog.setChanges(changes);
		changeLogs.add(changeLog);
		componentChangeLogs.setChangeLogs(changeLogs);
		allComponentChangeLogs.add(componentChangeLogs);
		context.setChangeLogs(allComponentChangeLogs);
	}
	
}
