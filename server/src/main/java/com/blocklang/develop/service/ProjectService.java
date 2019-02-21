package com.blocklang.develop.service;

import java.util.Optional;

import com.blocklang.core.model.UserInfo;
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

	/**
	 * 创建一个项目
	 * 
	 * 包括以下步骤：
	 * <ol>
	 * <li>在数据库中保存项目基本信息
	 * <li>在数据库中保存 APP 基本信息
	 * <li>为项目生成默认模块：Main 页面
	 * <li>为项目生成 README.md 文件
	 * <li>创建一个 git 仓库
	 * </ol>
	 * 
	 * <p>注意，这里将“创建一个 git 仓库”放在最后一步，如果出现创建 git 仓库失败的情况，则在日志中记录，
	 * 然后启动项目检查服务，根据已保存的项目信息重新生成
	 * 
	 * @param userInfo
	 * @param project
	 * @return 已持久化的 project
	 */
	Project create(UserInfo userInfo, Project project);

}
