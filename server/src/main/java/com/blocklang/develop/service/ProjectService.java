package com.blocklang.develop.service;

import java.util.Optional;

import com.blocklang.develop.model.Project;

public interface ProjectService {

	/**
	 * 根据项目拥有者的登录名和项目名查找项目
	 * 
	 * @param loginName 用户登录名，忽略大小写
	 * @param projectName 项目名，忽略大小写
	 * @return 项目基本信息
	 */
	Optional<Project> find(String loginName, String projectName);

}
