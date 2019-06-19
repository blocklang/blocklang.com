package com.blocklang.marketplace.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ChangelogParseTaskTest {
	
	private MarketplacePublishContext context;

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Before
	public void setup() throws IOException {
		String folder = temp.newFolder().getPath();
		context = new MarketplacePublishContext(folder, "https://github.com/blocklang/blocklang.com.git");
	}

	@Test
	public void run_can_not_null() {
		ChangelogParseTask task = new ChangelogParseTask(context, null);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_can_not_empty() {
		ChangelogParseTask task = new ChangelogParseTask(context, Collections.emptyMap());
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_invalid_key_at_root() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("a", "1");
		changelogMap.put("b", "2");
		ChangelogParseTask task = new ChangelogParseTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_require_id_at_root() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("author", "1");
		changelogMap.put("changes", new ArrayList<Object>());
		ChangelogParseTask task = new ChangelogParseTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_require_author_at_root() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("changes", new ArrayList<Object>());
		ChangelogParseTask task = new ChangelogParseTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_require_changes_at_root() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		ChangelogParseTask task = new ChangelogParseTask(context, changelogMap);
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
		
		ChangelogParseTask task = new ChangelogParseTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_changes_should_be_list() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		changelogMap.put("changes", "I am a String, Not a List");
		
		ChangelogParseTask task = new ChangelogParseTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_should_has_one_or_more_change() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		changelogMap.put("changes", Collections.emptyList());
		
		ChangelogParseTask task = new ChangelogParseTask(context, changelogMap);
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
		
		ChangelogParseTask task = new ChangelogParseTask(context, changelogMap);
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
		
		ChangelogParseTask task = new ChangelogParseTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
}
