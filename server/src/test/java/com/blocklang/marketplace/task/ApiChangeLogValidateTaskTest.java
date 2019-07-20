package com.blocklang.marketplace.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.util.Arrays;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.blocklang.marketplace.data.changelog.ChangeLog;
import com.blocklang.marketplace.data.changelog.NewWidgetChange;
import com.blocklang.marketplace.data.changelog.WidgetEvent;
import com.blocklang.marketplace.data.changelog.WidgetEventArgument;
import com.blocklang.marketplace.data.changelog.WidgetProperty;
import com.blocklang.marketplace.data.changelog.WidgetPropertyOption;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.release.constant.ReleaseResult;

public class ApiChangeLogValidateTaskTest {
	
	private MarketplacePublishContext context;

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Before
	public void setup() throws IOException {
		String folder = temp.newFolder().getPath();
		ComponentRepoPublishTask publishTask = new ComponentRepoPublishTask();
		publishTask.setGitUrl("https://github.com/blocklang/blocklang.com.git");
		publishTask.setStartTime(LocalDateTime.now());
		publishTask.setPublishResult(ReleaseResult.INITED);
		context = new MarketplacePublishContext(folder, publishTask);
		
		TaskLogger logger = new TaskLogger(context.getRepoPublishLogFile());
		context.setLogger(logger);
	}

	@Test
	public void run_can_not_null() {
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, null);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_can_not_empty() {
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, Collections.emptyMap());
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_invalid_key_at_root() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("a", "1");
		changelogMap.put("b", "2");
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_require_id_at_root() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("author", "1");
		changelogMap.put("changes", new ArrayList<Object>());
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_require_author_at_root() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("changes", new ArrayList<Object>());
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_require_changes_at_root() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_id_should_be_string_and_author_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", 1);
		changelogMap.put("author", 2);
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		change1.put("newWidget", new Object());
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_changes_should_be_list() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		changelogMap.put("changes", "I am a String, Not a List");
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_should_has_one_or_more_change() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		changelogMap.put("changes", Collections.emptyList());
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_one_change_should_only_map_to_one_operator() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		change1.put("operator-1", new Object());
		changes.add(change1);
		Map<String, Object> change2 = new HashMap<String, Object>();
		change1.put("operator-2", new Object());
		changes.add(change2);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_should_be_supported_operator() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		change1.put("not-supported-operator", new Object());
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_invalid_keys() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("invalid-key", "a");
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_name_should_be_required_and_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", 1);
		newWidget.put("label", "a");
		newWidget.put("iconClass", "b");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_label_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", 1);
		newWidget.put("iconClass", "b");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_description_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("description", 1);
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_iconClass_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", 1);
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_app_type_should_be_string_array() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", "web");
		newWidget.put("properties", Collections.emptyList());
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_app_type_value_should_be_web() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"not-web"}));
		newWidget.put("properties", Collections.emptyList());
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_can_be_null() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isPresent();
	}
	
	@Test
	public void run_changes_new_widget_properties_should_be_list() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", "a");
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_can_be_null() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isPresent();
	}
	
	@Test
	public void run_changes_new_widget_events_should_be_list() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		newWidget.put("events", "a");
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_invalid_keys() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("invalid-property-key", "a");
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_name_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", 1);
		propertyMap.put("label", "a");
		propertyMap.put("value", "b");
		propertyMap.put("valueType", "string");
		propertyMap.put("options", Collections.emptyList());
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_label_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", 1);
		propertyMap.put("value", "b");
		propertyMap.put("valueType", "string");
		propertyMap.put("options", Collections.emptyList());
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_if_value_type_is_string_then_default_value_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("defaultValue", 1);
		propertyMap.put("valueType", "string");
		propertyMap.put("options", Collections.emptyList());
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_if_value_type_is_boolean_then_default_value_should_be_boolean() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("defaultValue", "true"); // 应该为 boolean 类型
		propertyMap.put("valueType", "boolean");
		propertyMap.put("options", Collections.emptyList());
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_if_value_type_is_number_then_default_value_should_be_number() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("defaultValue", "1"); // 应该为 number 类型
		propertyMap.put("valueType", "number");
		propertyMap.put("options", Collections.emptyList());
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_value_type_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", 1);
		propertyMap.put("options", Collections.emptyList());
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_value_type_should_be_the_defined_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", "invalid-type");
		propertyMap.put("options", Collections.emptyList());
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_description_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", "string");
		propertyMap.put("description", 1);
		propertyMap.put("options", Collections.emptyList());
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_options_can_be_null() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("defaultValue", "c");
		propertyMap.put("valueType", "string");
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isPresent();
	}
	
	@Test
	public void run_changes_new_widget_properties_options_should_be_list() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", "string");
		propertyMap.put("options", "not-a-array");
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_options_invalid_key() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", "string");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("invalid-key", "a");
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	// value 必填
	@Test
	public void run_changes_new_widget_properties_options_value_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", "string");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", 1);
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	// label 必填
	@Test
	public void run_changes_new_widget_properties_options_label_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", "string");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "a");
		optionMap.put("label", 1);
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_options_description_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", "string");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "a");
		optionMap.put("label", "b");
		optionMap.put("description", 1);
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_options_icon_class_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", "string");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "a");
		optionMap.put("label", "b");
		optionMap.put("iconClass", 1);
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}

	@Test
	public void run_changes_new_widget_events_invalid_keys() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("invalid-event-key", "a");
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	// name 必填
	@Test
	public void run_changes_new_widget_events_name_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", 1);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	// label 必填
	@Test
	public void run_changes_new_widget_events_label_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", 1);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_value_type_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", 1);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_value_type_can_be_null() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		// 不设置 valueType
		// eventMap.put("valueType", "function");
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isPresent();
	}
	
	@Test
	public void run_changes_new_widget_events_value_type_can_only_be_function() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "not-function");
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_description_type_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		eventMap.put("description", 1);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_should_be_list() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		eventMap.put("arguments", "not-a-array");
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_can_be_null() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isPresent();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_keys_should_be_valid() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("invalid-key", "a");
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_name_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", 1);
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_label_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "a");
		argumentMap.put("label", 1);
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_default_value_if_value_type_is_string_then_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "a");
		argumentMap.put("label", "b");
		argumentMap.put("defaultValue", 1);
		argumentMap.put("valueType", "string");
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_default_value_if_value_type_is_booleanstring_then_should_be_boolean() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "a");
		argumentMap.put("label", "b");
		argumentMap.put("defaultValue", "true"); // 应该是 boolean 类型
		argumentMap.put("valueType", "boolean");
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_default_value_if_value_type_is_number_then_should_be_number() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "a");
		argumentMap.put("label", "b");
		argumentMap.put("defaultValue", "1"); // 应该是 number 类型
		argumentMap.put("valueType", "number");
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_value_type_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "a");
		argumentMap.put("label", "b");
		argumentMap.put("value", "c");
		argumentMap.put("valueType", 1);
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_value_type_value_should_be_valid() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "a");
		argumentMap.put("label", "b");
		argumentMap.put("value", "c");
		argumentMap.put("valueType", "invalid-value");
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_description_value_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "a");
		argumentMap.put("label", "b");
		argumentMap.put("value", "c");
		argumentMap.put("valueType", "string");
		argumentMap.put("description", 1);
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_validate_max_length_for_id_at_root() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "a".repeat(256)); // max length 255
		changelogMap.put("author", "change_author");
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("/id 不能超过 255 个字节(一个汉字占两个字节)，当前包含 256 个字节");
	}
	
	@Test
	public void run_validate_max_length_for_author_at_root() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "a".repeat(256)); // max length 255
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("/author 不能超过 255 个字节(一个汉字占两个字节)，当前包含 256 个字节");
	}
	
	@Test
	public void run_validate_max_length_for_new_widget_name() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "changelog_author");
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "n".repeat(65)); // max length 64
		
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("name 不能超过 64 个字节(一个汉字占两个字节)，当前包含 65 个字节");
	}
	
	@Test
	public void run_validate_max_length_for_new_widget_label() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "changelog_author");
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "n".repeat(65)); // max length 64
		
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("label 不能超过 64 个字节(一个汉字占两个字节)，当前包含 65 个字节");
	}
	
	@Test
	public void run_validate_max_length_for_new_widget_description() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "changelog_author");
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "d".repeat(513)); // max length 512
		
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("description 不能超过 512 个字节(一个汉字占两个字节)，当前包含 513 个字节");
	}

	@Test
	public void run_validate_max_length_for_new_widget_property_name() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "changelog_author");
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "widget_description");
		newWidget.put("iconClass", "widget_iconClass");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "n".repeat(65)); // max length 64
		
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("name 不能超过 64 个字节(一个汉字占两个字节)，当前包含 65 个字节");
	}
	
	@Test
	public void run_validate_max_length_for_new_widget_property_label() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "changelog_author");
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "widget_description");
		newWidget.put("iconClass", "widget_iconClass");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "widget_prop_name");
		propertyMap.put("label", "l".repeat(65)); // max length 64
		
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("label 不能超过 64 个字节(一个汉字占两个字节)，当前包含 65 个字节");
	}
	
	@Test
	public void run_validate_max_length_for_new_widget_property_description() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "changelog_author");
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "widget_description");
		newWidget.put("iconClass", "widget_iconClass");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "widget_prop_name");
		propertyMap.put("label", "widget_prop_label");
		propertyMap.put("defaultValue", "widget_prop_default_value");
		propertyMap.put("valueType", "string");
		propertyMap.put("description", "d".repeat(513)); // max length 512
		
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("description 不能超过 512 个字节(一个汉字占两个字节)，当前包含 513 个字节");
	}
	
	@Test
	public void run_validate_max_length_for_new_widget_property_default_value() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "changelog_author");
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "widget_description");
		newWidget.put("iconClass", "widget_iconClass");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "widget_prop_name");
		propertyMap.put("label", "widget_prop_label");
		propertyMap.put("defaultValue", "d".repeat(33)); // max length 32
		propertyMap.put("valueType", "string");
		propertyMap.put("description", "widget_prop_description");
		
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("defaultValue 不能超过 32 个字节(一个汉字占两个字节)，当前包含 33 个字节");
	}
	
	
	@Test
	public void run_validate_max_length_for_new_widget_property_option_value() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "changelog_author");
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "widget_description");
		newWidget.put("iconClass", "widget_iconClass");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "widget_prop_name");
		propertyMap.put("label", "widget_prop_label");
		propertyMap.put("defaultValue", "widget_prop_default_value");
		propertyMap.put("valueType", "string");
		propertyMap.put("description", "widget_prop_description"); // max length 512
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "v".repeat(33));
		optionMap.put("label", "widget_prop_option_label");
		options.add(optionMap);
		propertyMap.put("options", options);
		
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("value 不能超过 32 个字节(一个汉字占两个字节)，当前包含 33 个字节");
	}
	
	@Test
	public void run_validate_max_length_for_new_widget_property_option_label() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "changelog_author");
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "widget_description");
		newWidget.put("iconClass", "widget_iconClass");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "widget_prop_name");
		propertyMap.put("label", "widget_prop_label");
		propertyMap.put("defaultValue", "widget_prop_default_value");
		propertyMap.put("valueType", "string");
		propertyMap.put("description", "widget_prop_description"); // max length 512
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "widget_prop_option_value");
		optionMap.put("label", "l".repeat(33));
		options.add(optionMap);
		propertyMap.put("options", options);
		
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("label 不能超过 32 个字节(一个汉字占两个字节)，当前包含 33 个字节");
	}
	
	@Test
	public void run_validate_max_length_for_new_widget_property_option_description() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "changelog_author");
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "widget_description");
		newWidget.put("iconClass", "widget_iconClass");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "widget_prop_name");
		propertyMap.put("label", "widget_prop_label");
		propertyMap.put("defaultValue", "widget_prop_default_value");
		propertyMap.put("valueType", "string");
		propertyMap.put("description", "widget_prop_description");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "widget_prop_option_value");
		optionMap.put("label", "widget_prop_option_label");
		optionMap.put("description", "d".repeat(513));  // max length 512
		options.add(optionMap);
		propertyMap.put("options", options);
		
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("description 不能超过 512 个字节(一个汉字占两个字节)，当前包含 513 个字节");
	}
	
	// TODO: 目前 property - option - iconClass 未存入数据库，当做到界面设计部分时再确认是否需要此字段
	
	@Test
	public void run_validate_max_length_for_new_widget_event_name() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "changelog_author");
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "widget_description");
		newWidget.put("iconClass", "widget_iconClass");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "widget_prop_name");
		propertyMap.put("label", "widget_prop_label");
		propertyMap.put("defaultValue", "widget_prop_default_value");
		propertyMap.put("valueType", "string");
		propertyMap.put("description", "widget_prop_description");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "widget_prop_option_value");
		optionMap.put("label", "widget_prop_option_label");
		optionMap.put("description", "widget_prop_option_description");
		optionMap.put("iconClass", "widget_prop_option_iconClass");
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "n".repeat(33)); // max length 32
		
		events.add(eventMap);
		newWidget.put("events", events);
		
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("name 不能超过 32 个字节(一个汉字占两个字节)，当前包含 33 个字节");
	}
	
	@Test
	public void run_validate_max_length_for_new_widget_event_label() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "changelog_author");
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "widget_description");
		newWidget.put("iconClass", "widget_iconClass");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "widget_prop_name");
		propertyMap.put("label", "widget_prop_label");
		propertyMap.put("defaultValue", "widget_prop_default_value");
		propertyMap.put("valueType", "string");
		propertyMap.put("description", "widget_prop_description");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "widget_prop_option_value");
		optionMap.put("label", "widget_prop_option_label");
		optionMap.put("description", "widget_prop_option_description");
		optionMap.put("iconClass", "widget_prop_option_iconClass");
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "widget_event_name");
		eventMap.put("label", "l".repeat(33)); // max length 32
		
		events.add(eventMap);
		newWidget.put("events", events);
		
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("label 不能超过 32 个字节(一个汉字占两个字节)，当前包含 33 个字节");
	}
	
	@Test
	public void run_validate_max_length_for_new_widget_event_description() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "changelog_author");
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "widget_description");
		newWidget.put("iconClass", "widget_iconClass");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "widget_prop_name");
		propertyMap.put("label", "widget_prop_label");
		propertyMap.put("defaultValue", "widget_prop_default_value");
		propertyMap.put("valueType", "string");
		propertyMap.put("description", "widget_prop_description");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "widget_prop_option_value");
		optionMap.put("label", "widget_prop_option_label");
		optionMap.put("description", "widget_prop_option_description");
		optionMap.put("iconClass", "widget_prop_option_iconClass");
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "widget_event_name");
		eventMap.put("label", "widget_event_label");
		eventMap.put("description", "d".repeat(513)); // max length 512
		
		events.add(eventMap);
		newWidget.put("events", events);
		
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("description 不能超过 512 个字节(一个汉字占两个字节)，当前包含 513 个字节");
	}
	
	@Test
	public void run_validate_max_length_for_new_widget_event_argument_name() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "changelog_author");
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "widget_description");
		newWidget.put("iconClass", "widget_iconClass");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "widget_prop_name");
		propertyMap.put("label", "widget_prop_label");
		propertyMap.put("defaultValue", "widget_prop_default_value");
		propertyMap.put("valueType", "string");
		propertyMap.put("description", "widget_prop_description");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "widget_prop_option_value");
		optionMap.put("label", "widget_prop_option_label");
		optionMap.put("description", "widget_prop_option_description");
		optionMap.put("iconClass", "widget_prop_option_iconClass");
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "widget_event_name");
		eventMap.put("label", "widget_event_label");
		eventMap.put("description", "widget_event_description");
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "n".repeat(33)); // max length 32
		argumentMap.put("label", "widget_event_argument_label");
		argumentMap.put("valueType", "string");
		
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("name 不能超过 32 个字节(一个汉字占两个字节)，当前包含 33 个字节");
	}
	
	@Test
	public void run_validate_max_length_for_new_widget_event_argument_label() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "changelog_author");
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "widget_description");
		newWidget.put("iconClass", "widget_iconClass");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "widget_prop_name");
		propertyMap.put("label", "widget_prop_label");
		propertyMap.put("defaultValue", "widget_prop_default_value");
		propertyMap.put("valueType", "string");
		propertyMap.put("description", "widget_prop_description");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "widget_prop_option_value");
		optionMap.put("label", "widget_prop_option_label");
		optionMap.put("description", "widget_prop_option_description");
		optionMap.put("iconClass", "widget_prop_option_iconClass");
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "widget_event_name");
		eventMap.put("label", "widget_event_label");
		eventMap.put("description", "widget_event_description");
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "widget_event_argument_name");
		argumentMap.put("label", "l".repeat(33)); // max length 32
		argumentMap.put("valueType", "string");
		
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("label 不能超过 32 个字节(一个汉字占两个字节)，当前包含 33 个字节");
	}
	
	@Test
	public void run_validate_max_length_for_new_widget_event_argument_description() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "changelog_author");
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "widget_description");
		newWidget.put("iconClass", "widget_iconClass");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "widget_prop_name");
		propertyMap.put("label", "widget_prop_label");
		propertyMap.put("defaultValue", "widget_prop_default_value");
		propertyMap.put("valueType", "string");
		propertyMap.put("description", "widget_prop_description");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "widget_prop_option_value");
		optionMap.put("label", "widget_prop_option_label");
		optionMap.put("description", "widget_prop_option_description");
		optionMap.put("iconClass", "widget_prop_option_iconClass");
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "widget_event_name");
		eventMap.put("label", "widget_event_label");
		eventMap.put("description", "widget_event_description");
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "widget_event_argument_name");
		argumentMap.put("label", "widget_event_argument_label");
		argumentMap.put("defaultValue", "widget_event_argument_default_value");
		argumentMap.put("valueType", "string");
		argumentMap.put("description", "d".repeat(513)); // max length 512
		
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("description 不能超过 512 个字节(一个汉字占两个字节)，当前包含 513 个字节");
	}
	
	@Test
	public void run_validate_max_length_for_new_widget_event_argument_default_value() throws IOException {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "changelog_author");
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "widget_description");
		newWidget.put("iconClass", "widget_iconClass");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "widget_prop_name");
		propertyMap.put("label", "widget_prop_label");
		propertyMap.put("defaultValue", "widget_prop_default_value");
		propertyMap.put("valueType", "string");
		propertyMap.put("description", "widget_prop_description");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "widget_prop_option_value");
		optionMap.put("label", "widget_prop_option_label");
		optionMap.put("description", "widget_prop_option_description");
		optionMap.put("iconClass", "widget_prop_option_iconClass");
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "widget_event_name");
		eventMap.put("label", "widget_event_label");
		eventMap.put("description", "widget_event_description");
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "widget_event_argument_name");
		argumentMap.put("label", "widget_event_argument_label");
		argumentMap.put("defaultValue", "d".repeat(33)); // max length 32
		argumentMap.put("valueType", "string");
		argumentMap.put("description", "widget_event_argument_description"); 
		
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
		assertThat(Files.readString(context.getRepoPublishLogFile())).contains("defaultValue 不能超过 32 个字节(一个汉字占两个字节)，当前包含 33 个字节");
	}

	// 上面的用例都是校验
	// 下面的用例都是获取值
	// 只需要一个包含最全数据的测试用例
	@Test
	public void run_parse_data_value_type_is_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "change_author");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "widget_description");
		newWidget.put("iconClass", "widget_iconClass");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "widget_prop_name");
		propertyMap.put("label", "widget_prop_label");
		propertyMap.put("defaultValue", "widget_prop_default_value");
		propertyMap.put("description", "widget_prop_description");
		propertyMap.put("valueType", "string");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "widget_prop_option_value");
		optionMap.put("label", "widget_prop_option_label");
		optionMap.put("description", "widget_prop_option_description");
		optionMap.put("iconClass", "widget_prop_option_iconClass");
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "widget_event_name");
		eventMap.put("label", "widget_event_label");
		eventMap.put("description", "widget_event_description");
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "widget_event_argument_name");
		argumentMap.put("label", "widget_event_argument_label");
		argumentMap.put("defaultValue", "widget_event_arg_default_value");
		argumentMap.put("valueType", "string");
		argumentMap.put("description", "widget_event_argument_description");
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		Optional<ChangeLog> changelogOption = task.run();
		assertThat(changelogOption).isPresent();
		
		// 注意，event 的 valueType 的值为 function
		ChangeLog changelog = changelogOption.get();
		assertThat(changelog.getId()).isEqualTo("change_id");
		assertThat(changelog.getAuthor()).isEqualTo("change_author");
		assertThat(changelog.getChanges()).hasSize(1);
		
		NewWidgetChange newWidgetChange = (NewWidgetChange) changelog.getChanges().get(0);
		assertThat(newWidgetChange.getName()).isEqualTo("widget_name");
		assertThat(newWidgetChange.getLabel()).isEqualTo("widget_label");
		assertThat(newWidgetChange.getDescription()).isEqualTo("widget_description");
		assertThat(newWidgetChange.getIconClass()).isEqualTo("widget_iconClass");
		assertThat(newWidgetChange.getAppType()).hasSize(1);
		assertThat(newWidgetChange.getAppType().get(0)).isEqualTo("web");
		assertThat(newWidgetChange.getProperties()).hasSize(1);
		assertThat(newWidgetChange.getEvents()).hasSize(1);
		
		WidgetProperty property = newWidgetChange.getProperties().get(0);
		assertThat(property.getName()).isEqualTo("widget_prop_name");
		assertThat(property.getLabel()).isEqualTo("widget_prop_label");
		assertThat(property.getDefaultValue()).isEqualTo("widget_prop_default_value");
		assertThat(property.getDescription()).isEqualTo("widget_prop_description");
		assertThat(property.getValueType()).isEqualTo("string");
		assertThat(property.getOptions()).hasSize(1);
		
		WidgetPropertyOption propertyOption = property.getOptions().get(0);
		assertThat(propertyOption.getValue()).isEqualTo("widget_prop_option_value");
		assertThat(propertyOption.getLabel()).isEqualTo("widget_prop_option_label");
		assertThat(propertyOption.getDescription()).isEqualTo("widget_prop_option_description");
		assertThat(propertyOption.getIconClass()).isEqualTo("widget_prop_option_iconClass");
		
		WidgetEvent event = newWidgetChange.getEvents().get(0);
		assertThat(event.getName()).isEqualTo("widget_event_name");
		assertThat(event.getLabel()).isEqualTo("widget_event_label");
		assertThat(event.getValueType()).isEqualTo("function");
		assertThat(event.getDescription()).isEqualTo("widget_event_description");
		assertThat(event.getArguments()).hasSize(1);
		
		WidgetEventArgument eventArgument = event.getArguments().get(0);
		assertThat(eventArgument.getName()).isEqualTo("widget_event_argument_name");
		assertThat(eventArgument.getLabel()).isEqualTo("widget_event_argument_label");
		assertThat(eventArgument.getDefaultValue()).isEqualTo("widget_event_arg_default_value");
		assertThat(eventArgument.getValueType()).isEqualTo("string");
		assertThat(eventArgument.getDescription()).isEqualTo("widget_event_argument_description");
	}
	
	@Test
	public void run_parse_data_value_type_is_boolean() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "change_author");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "widget_description");
		newWidget.put("iconClass", "widget_iconClass");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "widget_prop_name");
		propertyMap.put("label", "widget_prop_label");
		propertyMap.put("defaultValue", true);
		propertyMap.put("description", "widget_prop_description");
		propertyMap.put("valueType", "boolean");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "widget_prop_option_value");
		optionMap.put("label", "widget_prop_option_label");
		optionMap.put("description", "widget_prop_option_description");
		optionMap.put("iconClass", "widget_prop_option_iconClass");
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "widget_event_name");
		eventMap.put("label", "widget_event_label");
		eventMap.put("description", "widget_event_description");
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "widget_event_argument_name");
		argumentMap.put("label", "widget_event_argument_label");
		argumentMap.put("defaultValue", true);
		argumentMap.put("valueType", "boolean");
		argumentMap.put("description", "widget_event_argument_description");
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		Optional<ChangeLog> changelogOption = task.run();
		assertThat(changelogOption).isPresent();
		
		// 注意，event 的 valueType 的值为 function
		ChangeLog changelog = changelogOption.get();
		assertThat(changelog.getId()).isEqualTo("change_id");
		assertThat(changelog.getAuthor()).isEqualTo("change_author");
		assertThat(changelog.getChanges()).hasSize(1);
		
		NewWidgetChange newWidgetChange = (NewWidgetChange) changelog.getChanges().get(0);
		assertThat(newWidgetChange.getName()).isEqualTo("widget_name");
		assertThat(newWidgetChange.getLabel()).isEqualTo("widget_label");
		assertThat(newWidgetChange.getDescription()).isEqualTo("widget_description");
		assertThat(newWidgetChange.getIconClass()).isEqualTo("widget_iconClass");
		assertThat(newWidgetChange.getAppType()).hasSize(1);
		assertThat(newWidgetChange.getAppType().get(0)).isEqualTo("web");
		assertThat(newWidgetChange.getProperties()).hasSize(1);
		assertThat(newWidgetChange.getEvents()).hasSize(1);
		
		WidgetProperty property = newWidgetChange.getProperties().get(0);
		assertThat(property.getName()).isEqualTo("widget_prop_name");
		assertThat(property.getLabel()).isEqualTo("widget_prop_label");
		assertThat(property.getDefaultValue()).isEqualTo(true);
		assertThat(property.getDescription()).isEqualTo("widget_prop_description");
		assertThat(property.getValueType()).isEqualTo("boolean");
		assertThat(property.getOptions()).hasSize(1);
		
		WidgetPropertyOption propertyOption = property.getOptions().get(0);
		assertThat(propertyOption.getValue()).isEqualTo("widget_prop_option_value");
		assertThat(propertyOption.getLabel()).isEqualTo("widget_prop_option_label");
		assertThat(propertyOption.getDescription()).isEqualTo("widget_prop_option_description");
		assertThat(propertyOption.getIconClass()).isEqualTo("widget_prop_option_iconClass");
		
		WidgetEvent event = newWidgetChange.getEvents().get(0);
		assertThat(event.getName()).isEqualTo("widget_event_name");
		assertThat(event.getLabel()).isEqualTo("widget_event_label");
		assertThat(event.getValueType()).isEqualTo("function");
		assertThat(event.getDescription()).isEqualTo("widget_event_description");
		assertThat(event.getArguments()).hasSize(1);
		
		WidgetEventArgument eventArgument = event.getArguments().get(0);
		assertThat(eventArgument.getName()).isEqualTo("widget_event_argument_name");
		assertThat(eventArgument.getLabel()).isEqualTo("widget_event_argument_label");
		assertThat(eventArgument.getDefaultValue()).isEqualTo(true);
		assertThat(eventArgument.getValueType()).isEqualTo("boolean");
		assertThat(eventArgument.getDescription()).isEqualTo("widget_event_argument_description");
	}
	
	@Test
	public void run_parse_data_value_type_is_number() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "change_author");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "widget_description");
		newWidget.put("iconClass", "widget_iconClass");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "widget_prop_name");
		propertyMap.put("label", "widget_prop_label");
		propertyMap.put("defaultValue", 1);
		propertyMap.put("description", "widget_prop_description");
		propertyMap.put("valueType", "number");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "widget_prop_option_value");
		optionMap.put("label", "widget_prop_option_label");
		optionMap.put("description", "widget_prop_option_description");
		optionMap.put("iconClass", "widget_prop_option_iconClass");
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "widget_event_name");
		eventMap.put("label", "widget_event_label");
		eventMap.put("description", "widget_event_description");
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "widget_event_argument_name");
		argumentMap.put("label", "widget_event_argument_label");
		argumentMap.put("defaultValue", 1);
		argumentMap.put("valueType", "number");
		argumentMap.put("description", "widget_event_argument_description");
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		Optional<ChangeLog> changelogOption = task.run();
		assertThat(changelogOption).isPresent();
		
		// 注意，event 的 valueType 的值为 function
		ChangeLog changelog = changelogOption.get();
		assertThat(changelog.getId()).isEqualTo("change_id");
		assertThat(changelog.getAuthor()).isEqualTo("change_author");
		assertThat(changelog.getChanges()).hasSize(1);
		
		NewWidgetChange newWidgetChange = (NewWidgetChange) changelog.getChanges().get(0);
		assertThat(newWidgetChange.getName()).isEqualTo("widget_name");
		assertThat(newWidgetChange.getLabel()).isEqualTo("widget_label");
		assertThat(newWidgetChange.getDescription()).isEqualTo("widget_description");
		assertThat(newWidgetChange.getIconClass()).isEqualTo("widget_iconClass");
		assertThat(newWidgetChange.getAppType()).hasSize(1);
		assertThat(newWidgetChange.getAppType().get(0)).isEqualTo("web");
		assertThat(newWidgetChange.getProperties()).hasSize(1);
		assertThat(newWidgetChange.getEvents()).hasSize(1);
		
		WidgetProperty property = newWidgetChange.getProperties().get(0);
		assertThat(property.getName()).isEqualTo("widget_prop_name");
		assertThat(property.getLabel()).isEqualTo("widget_prop_label");
		assertThat(property.getDefaultValue()).isEqualTo(1);
		assertThat(property.getDescription()).isEqualTo("widget_prop_description");
		assertThat(property.getValueType()).isEqualTo("number");
		assertThat(property.getOptions()).hasSize(1);
		
		WidgetPropertyOption propertyOption = property.getOptions().get(0);
		assertThat(propertyOption.getValue()).isEqualTo("widget_prop_option_value");
		assertThat(propertyOption.getLabel()).isEqualTo("widget_prop_option_label");
		assertThat(propertyOption.getDescription()).isEqualTo("widget_prop_option_description");
		assertThat(propertyOption.getIconClass()).isEqualTo("widget_prop_option_iconClass");
		
		WidgetEvent event = newWidgetChange.getEvents().get(0);
		assertThat(event.getName()).isEqualTo("widget_event_name");
		assertThat(event.getLabel()).isEqualTo("widget_event_label");
		assertThat(event.getValueType()).isEqualTo("function");
		assertThat(event.getDescription()).isEqualTo("widget_event_description");
		assertThat(event.getArguments()).hasSize(1);
		
		WidgetEventArgument eventArgument = event.getArguments().get(0);
		assertThat(eventArgument.getName()).isEqualTo("widget_event_argument_name");
		assertThat(eventArgument.getLabel()).isEqualTo("widget_event_argument_label");
		assertThat(eventArgument.getDefaultValue()).isEqualTo(1);
		assertThat(eventArgument.getValueType()).isEqualTo("number");
		assertThat(eventArgument.getDescription()).isEqualTo("widget_event_argument_description");
	}
}
