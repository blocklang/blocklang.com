package com.blocklang.develop.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.core.model.UserInfo;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.data.UncommittedFile;
import com.blocklang.develop.designer.data.PageModel;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectResource;

public interface ProjectResourceService {

	ProjectResource insert(Project project, ProjectResource resource);

	/**
	 * 获取项目结构，其中包含模块的提交信息
	 * 
	 * @param project
	 * @param parentResourceId
	 * @return 项目结构
	 */
	List<ProjectResource> findChildren(Project project, Integer parentResourceId);
	
	/**
	 * 获取资源的父路径，包含当前资源，如果是根目录，则返回空数组。
	 * 
	 * 注意，本方法返回的数组中最后一个元素是 <code>resourceId</code> 对应的资源 key 或 name (只用于 dependences.json 文件)
	 * 
	 * @param resourceId
	 * @return
	 */
	List<String> findParentPathes(Integer resourceId);

	/**
	 * 在同一层级下，根据 key 查找
	 * 
	 * @param projectId
	 * @param parentId
	 * @param resourceType
	 * @param appType
	 * @param key
	 * @return
	 */
	Optional<ProjectResource> findByKey(
			Integer projectId, 
			Integer parentId, 
			ProjectResourceType resourceType,
			AppType appType,
			String key);
	
	/**
	 * 在同一层级下，根据 name 查找
	 * 
	 * @param projectId
	 * @param parentId
	 * @param resourceType
	 * @param appType
	 * @param name
	 * @return
	 */
	Optional<ProjectResource> findByName(
			Integer projectId, 
			Integer parentId, 
			ProjectResourceType resourceType,
			AppType appType,
			String name);

	Optional<ProjectResource> findById(Integer resourceId);

	/**
	 * 只要存在项目标识，则就假定该项目肯定存在
	 * findParentIdByParentPath 中不校验项目是否存在，
	 * 在调用此方法前，就应校验过项目是否存在。
	 * 
	 * @param projectId
	 * @param parentPath
	 * @return 如果有一个分组匹配不上，则返回空数组
	 */
	List<ProjectResource> findParentGroupsByParentPath(Integer projectId, String parentPath);
	
	/**
	 * 获取项目中所有页面
	 * 
	 * @param projectId 项目标识
	 * @param appType 程序类型
	 * @return 页面信息列表
	 */
	List<ProjectResource> findAllPages(Integer projectId, AppType appType);

	List<UncommittedFile> findChanges(Project project);

	void stageChanges(Project project, String[] filePathes);

	void unstageChanges(Project project, String[] filePathes);

	/**
	 * 
	 * @param user
	 * @param project
	 * @param commitMessage
	 * @return 返回 commitId
	 */
	String commit(UserInfo user, Project project, String commitMessage);

	void updatePageModel(Project project, ProjectResource projectResource, PageModel pageModel);

	/**
	 * 获取页面模型
	 * 
	 * @param projectId 项目标识
	 * @param pageId 页面标识
	 * @return 返回页面模型，不能返回 null，如果页面模型中没有内容，则返回空的 PageModel 对象
	 */
	PageModel getPageModel(Integer projectId, Integer pageId);

	/**
	 * 默认创建的空页面中包含一个 Page 部件
	 * 
	 * @param pageId
	 * @return
	 */
	PageModel createPageModelWithStdPage(Integer pageId);
}
