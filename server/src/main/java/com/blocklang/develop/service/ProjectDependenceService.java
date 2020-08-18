package com.blocklang.develop.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.data.ProjectDependenceData;
import com.blocklang.develop.designer.data.RepoWidgetList;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.marketplace.model.ComponentRepo;

public interface ProjectDependenceService {

	/**
	 *  dev 仓库不存在 profile 一说
	 *  
	 * @param projectId
	 * @param componentRepoId
	 * @return
	 */
	Boolean devDependenceExists(Integer projectId, Integer componentRepoId);
	
	Boolean buildDependenceExists(Integer projectId, Integer componentRepoId, AppType appType, String profileName);

	/**
	 * 本方法会将依赖添加到默认的 Profile 下
	 * 
	 * @param projectId
	 * @param componentRepo
	 * @param user
	 * @return 如果保存失败则返回 <code>null</code>；否则返回保存后的项目依赖信息
	 */
	ProjectDependence save(Integer repoId, Integer projectId, ComponentRepo componentRepo, Integer createUserId);

	/**
	 * 补充上系统使用的标准库的依赖
	 * 
	 * @param repoId 仓库标识
	 * @param project 仓库中的项目信息
	 * @return 指定项目的所有依赖，包括标准库
	 */
	List<ProjectDependence> findAllByProjectId(Integer repoId, RepositoryResource project);
	
	/**
	 * 注意：返回的依赖中不包含标准库，相当于 {@code this.findProjectDependences(projectId, false)}
	 * 
	 * @param projectId
	 * @return
	 */
	List<ProjectDependenceData> findProjectDependences(Integer repoId, Integer projectId);
	
	List<ProjectDependenceData> findProjectBuildDependences(Integer projectId);

	/**
	 * 返回子项目的依赖列表，不包含标准库依赖
	 * 
	 * @param projectId 仓库中的子项目标识
	 * @param includeStd 如果值为 <code>true</code>，则返回标准库
	 * @return 子项目的依赖列表
	 */
	List<ProjectDependenceData> findProjectDependences(Integer projectId);
	
	/**
	 * 获取项目的 IDE 依赖，不包含标准库依赖。
	 * 
	 * @param project 项目信息
	 * @return 子项目的依赖列表
	 */
	List<ProjectDependenceData> findIdeDependences(RepositoryResource project);
	
	/**
	 * 获取项目的 IDE 版标准库依赖
	 * @param project 项目信息
	 * @return 子项目的依赖列表
	 */
	List<ProjectDependenceData> findStdIdeDependences(RepositoryResource project);

	void delete(Integer dependenceId);

	ProjectDependence update(ProjectDependence dependence);

	Optional<ProjectDependence> findById(Integer dependenceId);

	/**
	 * 从 API 项目中找出 Widget 类型的项目，然后分组罗列其中的部件
	 * 
	 * @param projectId 项目标识
	 * @return 先按仓库分组，在根据 category 分组的部件列表
	 */
	List<RepoWidgetList> findAllWidgets(Integer projectId);

}
