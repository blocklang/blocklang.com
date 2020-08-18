package com.blocklang.develop.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.core.model.UserInfo;
import com.blocklang.develop.data.GitCommitInfo;
import com.blocklang.develop.model.Repository;

public interface RepositoryService {

	/**
	 * 根据仓库拥有者的登录名和项目名查找项目
	 * 
	 * @param loginName 用户登录名，忽略大小写
	 * @param repoName 仓库名，忽略大小写
	 * @return 仓库基本信息
	 */
	Optional<Repository> find(String loginName, String repoName);

	
//	  <ol>
//	  <li>在数据库中保存项目基本信息
//	  <li>在数据库中保存 APP 基本信息
//	  <li>为项目生成默认模块：Main 页面
//	  <li>为项目生成 README.md 文件
//	  <li>创建一个 git 仓库
//	  </ol>

	/**
	 * 创建一个仓库，一个仓库下可包含多个软件项目。
	 * 
	 * 包括以下步骤：
	 * 
	 * <ol>
	 * <li>在数据库中保存仓库基本信息
	 * <li>为仓库生成 README.md 文件
	 * <li>为仓库生成 BUILD.json 文件
	 * <li>创建一个 git 仓库
	 * </ol>
	 * 
	 * <p>注意，这里将“创建一个 git 仓库”放在最后一步，如果出现创建 git 仓库失败的情况，则在日志中记录，
	 * 然后启动项目检查服务，根据已保存的项目信息重新生成
	 * 
	 * @param userInfo
	 * @param repository
	 * @return 已持久化的 project
	 */
	Repository createRepository(UserInfo userInfo, Repository repository);

	/**
	 * 获取用户有权访问的仓库列表
	 * 
	 * 有三种权限：read、write、admin。
	 * 
	 * 已支持：
	 * <ol>
	 * <li>用户创建的项目，拥有 admin 权限
	 * </ol>
	 * 
	 * @param userId 用户标识
	 * @return 仓库列表，默认按照项目最近活动时间倒排
	 */
	List<Repository> findCanAccessRepositoriesByUserId(Integer userId);

	Optional<GitCommitInfo> findLatestCommitInfo(Repository repository, String relativeFilePath);

	Optional<Repository> findById(Integer repositoryId);
}
