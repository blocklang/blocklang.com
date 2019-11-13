package com.blocklang.develop.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.service.UserService;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectPermissionService;
import com.blocklang.develop.service.ProjectService;

public class AbstractProjectController {

	@Autowired 
	protected UserService userService;
	@Autowired
	protected ProjectService projectService;

	@Autowired
	protected ProjectPermissionService projectPermissionService;

	/**
	 * 对一条路径中的资源进行处理，并返回处理后的数据
	 * 
	 * @param resources 资源列表，按照资源路径的顺序存储，如 a/b/c/d
	 * @return
	 */
	protected List<Map<String, String>> stripResourcePathes(List<ProjectResource> resources) {
		List<Map<String, String>> stripedParentGroups = new ArrayList<Map<String, String>>();
		String relativePath = "";
		for(ProjectResource each : resources) {
			relativePath = relativePath + "/" + each.getKey();
			
			Map<String, String> map = new HashMap<String, String>();
			if(StringUtils.isBlank(each.getName())) {
				map.put("name", each.getKey());
			} else {
				map.put("name", each.getName());
			}
			
			map.put("path", relativePath);
			stripedParentGroups.add(map);
		}
		return stripedParentGroups;
	}
}
