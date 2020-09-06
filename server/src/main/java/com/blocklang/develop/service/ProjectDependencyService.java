package com.blocklang.develop.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.data.ProjectDependencyData;
import com.blocklang.develop.designer.data.RepoWidgetList;
import com.blocklang.develop.model.ProjectDependency;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryResource;

/**
 * 在 DENPENDENCY.json 文件中，将依赖分为两种： dev 和 build
 * 
 * @author jinzw
 *
 */
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

	ProjectDependency save(Repository repository, RepositoryResource project, ProjectDependency dependency);

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
	 * 获取项目的 IDE 依赖，不包含标准库依赖。
	 * 
	 * 判断依据是，projectBuildProfileId 的值为 null，则是 IDE 依赖（即 dev 依赖）；如果 projectBuildProfileId 的值不为 null，则是 PROD 依赖（即BUILD依赖）
	 * 
	 * @param projectId 项目标识
	 * @return 子项目的依赖列表
	 */
	List<ProjectDependencyData> findDevDependencies(Integer projectId);

	/**
	 * 获取项目的 IDE 版标准库依赖
	 * @param projectId 项目信息标识
	 * @return 子项目的依赖列表
	 */
	List<ProjectDependencyData> findStdDevDependencies(Integer projectId, AppType appType);
	
	List<ProjectDependencyData> findStdBuildDependencies(Integer projectId, AppType appType);

	void delete(Repository repository, RepositoryResource project, Integer dependencyId);

	ProjectDependency update(Repository repository, RepositoryResource project, ProjectDependency dependency);

	Optional<ProjectDependency> findById(Integer dependencyId);

	/**
	 * 从 API 项目中找出 Widget 类型的项目，然后分组罗列其中的部件
	 * 
	 * @param projectId 项目标识，注意不是仓库标识
	 * @return 先按仓库分组，在根据 category 分组的部件列表
	 */
	List<RepoWidgetList> findAllWidgets(Integer projectId);

}
