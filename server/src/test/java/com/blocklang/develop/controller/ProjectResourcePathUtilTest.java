package com.blocklang.develop.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.blocklang.develop.model.ProjectResource;

public class ProjectResourcePathUtilTest {
	
	@DisplayName("stripResourcePathes: when no resources")
	@Test
	public void stripResourcePathes_no_resources() {
		assertThat(ProjectResourcePathUtil.combinePathes(Collections.emptyList())).isEmpty();
	}
	
	@DisplayName("stripResourcePathes: a page has one group and page's name is not blank")
	@Test 
	public void stripResourcePathes_one_group_one_page_and_page_name_is_not_blank() {
		List<ProjectResource> resources = new ArrayList<ProjectResource>();
		
		ProjectResource resource1 = new ProjectResource();
		resource1.setName("group1");
		resource1.setKey("key1");
		
		ProjectResource resource2 = new ProjectResource();
		resource2.setName("page1");
		resource2.setKey("key2");
		
		resources.add(resource1);
		resources.add(resource2);
		
		List<Map<String, String>> results = ProjectResourcePathUtil.combinePathes(resources);
		
		assertThat(results).hasSize(2);
		
		Map<String, String> first = results.get(0);
		assertThat(first.get("name")).isEqualTo("group1");
		assertThat(first.get("path")).isEqualTo("/key1");
		
		Map<String, String> second = results.get(1);
		assertThat(second.get("name")).isEqualTo("page1");
		assertThat(second.get("path")).isEqualTo("/key1/key2");
	}
	
	@DisplayName("stripResourcePathes: a page has one group and page's name is blank")
	@Test 
	public void stripResourcePathes_one_group_one_page_and_page_name_is_blank() {
		List<ProjectResource> resources = new ArrayList<ProjectResource>();
		
		ProjectResource resource1 = new ProjectResource();
		resource1.setKey("key1");
		
		ProjectResource resource2 = new ProjectResource();
		resource2.setKey("key2");
		
		resources.add(resource1);
		resources.add(resource2);
		
		List<Map<String, String>> results = ProjectResourcePathUtil.combinePathes(resources);
		
		assertThat(results).hasSize(2);
		
		Map<String, String> first = results.get(0);
		assertThat(first.get("name")).isEqualTo("key1");
		assertThat(first.get("path")).isEqualTo("/key1");
		
		Map<String, String> second = results.get(1);
		assertThat(second.get("name")).isEqualTo("key2");
		assertThat(second.get("path")).isEqualTo("/key1/key2");
	}
}
