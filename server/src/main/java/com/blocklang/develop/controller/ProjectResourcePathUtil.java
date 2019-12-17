package com.blocklang.develop.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.blocklang.develop.model.ProjectResource;

public class ProjectResourcePathUtil {
	
	/**
	 * 对一条路径中的资源进行合并处理，并返回处理后的数据。
	 * 
	 * <p>
	 * 如一个分组下有一个页面，分组的 key 为 key1，页面的 key 为 key2，
	 * 则合并后的路径信息为：分组的值为 {name:'key1', path: '/key1'}，页面的值为 {name: 'key2', path: '/key1/key2'}
	 * </p>
	 * 
	 * @param resources 资源列表，按照资源路径的顺序存储，如 /a/b/c/d
	 * @return 返回为 Map 列表，Map 中的 key 分别为 name 和 path
	 */
	public static List<Map<String, String>> combinePathes(List<ProjectResource> resources) {
		List<Map<String, String>> stripedParentGroups = new ArrayList<Map<String, String>>();
		String relativePath = "";
		for(ProjectResource each : resources) {
			relativePath = relativePath + "/" + each.getKey();
			String name = "";
			if(StringUtils.isBlank(each.getName())) {
				name = each.getKey();
			} else {
				name = each.getName();
			}
			
			Map<String, String> map = new HashMap<String, String>();
			map.put("name", name);
			map.put("path", relativePath);
			stripedParentGroups.add(map);
		}
		return stripedParentGroups;
	}
}