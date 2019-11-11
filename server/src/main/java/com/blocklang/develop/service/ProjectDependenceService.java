package com.blocklang.develop.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.data.ProjectDependenceData;
import com.blocklang.develop.designer.data.WidgetRepo;
import com.blocklang.develop.model.ProjectDependence;
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
	ProjectDependence save(Integer projectId, ComponentRepo componentRepo, Integer createUserId);

	/**
	 * 补充上系统使用的标准库的依赖
	 * @param projectId
	 * @return
	 */
	List<ProjectDependence> findAllByProjectId(Integer projectId);
	
	List<ProjectDependenceData> findProjectDependences(Integer projectId);

	void delete(Integer dependenceId);

	ProjectDependence update(ProjectDependence dependence);

	Optional<ProjectDependence> findById(Integer dependenceId);

	/**
	 * 从 API 项目中找出 Widget 类型的项目，然后分组罗列其中的部件
	 * 
	 * @param projectId 项目标识
	 * @return 先按仓库分组，在根据 category 分组的部件列表
	 */
	List<WidgetRepo> findAllWidgets(Integer projectId);

}
