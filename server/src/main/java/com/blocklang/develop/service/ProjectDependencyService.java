package com.blocklang.develop.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.develop.data.ProjectDependencyData;
import com.blocklang.develop.designer.data.RepoWidgetList;
import com.blocklang.develop.model.ProjectDependency;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.marketplace.model.ComponentRepo;

public interface ProjectDependencyService {

	/**
	 *  校验项目依赖中是否已存在该 dev 版组件库。
	 *  
	 * 注意：dev 依赖不存在 profile 一说
	 *  
	 * @param projectId 仓库中某一个项目的标识
	 * @param componentRepoId 组件库标识
	 * @return 只要依赖组件库的任何一个版本，就返回 <code>true</code>；否则返回 <code>false</code>
	 */
	Boolean devDependencyExists(Integer projectId, Integer componentRepoId);
	
	/**
	 * 校验项目依赖中是否存在该 build 版组件库
	 * 
	 * 注意：build 依赖中存在 profile
	 * 
	 * @param projectId 仓库中某一个项目的标识
	 * @param buildProfileId build profile 标识，用来区分开不同的目标平台和框架
	 * @param componentRepoId 组件库标识
	 * @return 只要依赖组件库的任何一个版本，就返回 <code>true</code>；否则返回 <code>false</code>
	 */
	Boolean buildDependencyExists(Integer projectId, Integer buildProfileId, Integer componentRepoId);

	/**
	 * 本方法会将依赖添加到默认的 Profile 下
	 * 
	 * @param projectId
	 * @param componentRepo
	 * @param user
	 * @return 如果保存失败则返回 <code>null</code>；否则返回保存后的项目依赖信息
	 * 
	 * @deprecated
	 */
	ProjectDependency save(Integer repoId, Integer projectId, ComponentRepo componentRepo, Integer createUserId);
	
	ProjectDependency save(ProjectDependency dependency);

	/**
	 * 补充上系统使用的标准库的依赖
	 * 
	 * @param repoId 仓库标识
	 * @param project 仓库中的项目信息
	 * @return 指定项目的所有依赖，包括标准库
	 */
	List<ProjectDependency> findAllByProjectId(Integer repoId, RepositoryResource project);
	
	/**
	 * 注意：返回的依赖中不包含标准库，相当于 {@code this.findProjectDependences(projectId, false)}
	 * 
	 * @param projectId
	 * @return
	 */
	List<ProjectDependencyData> findProjectDependencies(Integer repoId, Integer projectId);
	
	List<ProjectDependencyData> findProjectBuildDependencies(Integer projectId);
	
	/**
	 * 获取项目的 IDE 依赖，不包含标准库依赖。
	 * 
	 * @param project 项目信息
	 * @return 子项目的依赖列表
	 */
	List<ProjectDependencyData> findIdeDependencies(RepositoryResource project);

	/**
	 * 返回子项目所有配置的依赖列表。不包含标准库依赖
	 * 
	 * 只包含手工配置的依赖。
	 * 
	 * @param projectId 仓库中的子项目标识
	 * @return 子项目的依赖列表
	 */
	List<ProjectDependencyData> findAllConfigDependencies(Integer projectId);

	/**
	 * 获取项目的 IDE 版标准库依赖
	 * @param project 项目信息
	 * @return 子项目的依赖列表
	 */
	List<ProjectDependencyData> findStdIdeDependencies(RepositoryResource project);
	
	List<ProjectDependencyData> findStdBuildDependencies(RepositoryResource project);

	void delete(Integer dependenceId);

	ProjectDependency update(ProjectDependency dependency);

	Optional<ProjectDependency> findById(Integer dependencyId);

	/**
	 * 从 API 项目中找出 Widget 类型的项目，然后分组罗列其中的部件
	 * 
	 * @param projectId 项目标识
	 * @return 先按仓库分组，在根据 category 分组的部件列表
	 */
	List<RepoWidgetList> findAllWidgets(Integer projectId);

}
