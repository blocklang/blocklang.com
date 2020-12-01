package com.blocklang.develop.service;

import java.util.List;
import java.util.Optional;

import com.blocklang.core.model.UserInfo;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.RepositoryResourceType;
import com.blocklang.develop.data.UncommittedFile;
import com.blocklang.develop.designer.data.PageModel;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiWidget;

/**
 * 管理仓库资源的业务逻辑接口
 * 
 * @author Zhengwei Jin
 *
 */
public interface RepositoryResourceService {

	RepositoryResource insert(Repository repository, RepositoryResource resource);
	
	RepositoryResource createWebProject(Repository project, RepositoryResource resource);
	
	/**
	 * 在仓库中创建小程序项目
	 * 
	 * @param repository 仓库信息
	 * @param project 小程序项目的基本信息
	 * @param apiRepo API 仓库
	 * @param appWidget API 仓库中的 App 部件
	 * @param pageWidget API 仓库中的 Page 部件
	 * @return 小程序项目的基本信息
	 */
	RepositoryResource createMiniProgram(
			Repository repository, 
			RepositoryResource project, 
			ApiRepo apiRepo, 
			ApiWidget appWidget, 
			ApiWidget pageWidget);

	/**
	 * 创建鸿蒙的 Lite Wearable 项目
	 * 
	 * @param repository 仓库信息
	 * @param project 鸿蒙 Lite Wearable 项目的基本信息
	 * @param apiRepo API 仓库
	 * @param appWidget API 仓库中的 App 部件
	 * @param pageWidget API 仓库中的 Page 部件
	 * @return 鸿蒙 Lite Wearable 项目的基本信息
	 */
	RepositoryResource createHarmonyOSLiteWearableProject(
			Repository repository, 
			RepositoryResource project,
			ApiRepo apiRepo, 
			ApiWidget appWidget, 
			ApiWidget pageWidget);
	
	/**
	 * 获取项目结构，其中包含模块的提交信息
	 * 
	 * @param project
	 * @param parentResourceId
	 * @return 项目结构
	 */
	List<RepositoryResource> findChildren(Repository project, Integer parentResourceId);
	
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
	 * @param repositoryId
	 * @param parentId
	 * @param resourceType
	 * @param appType
	 * @param key
	 * @return
	 */
	Optional<RepositoryResource> findByKey(
			Integer repositoryId, 
			Integer parentId, 
			RepositoryResourceType resourceType,
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
	Optional<RepositoryResource> findByName(
			Integer projectId, 
			Integer parentId, 
			RepositoryResourceType resourceType,
			AppType appType,
			String name);

	Optional<RepositoryResource> findById(Integer resourceId);

	/**
	 * 只要存在仓库标识，则就假定该仓库肯定存在
	 * findParentIdByParentPath 中不校验仓库是否存在，
	 * 在调用此方法前，就应校验过仓库是否存在。
	 * 
	 * @param repositoryId
	 * @param parentPath
	 * @return 如果有一个分组匹配不上，则返回空数组
	 */
	List<RepositoryResource> findParentGroupsByParentPath(Integer repositoryId, String parentPath);
	
	/**
	 * 获取仓库中所有页面。
	 * 
	 * FIXME: 是不是应该调整为获取仓库中某一个项目的所有页面呢？
	 * 
	 * @param repositoryId 项目标识
	 * @param appType 程序类型
	 * @return 页面信息列表
	 */
	List<RepositoryResource> findAllPages(Integer repositoryId, AppType appType);

	List<UncommittedFile> findChanges(Repository repository);

	void stageChanges(Repository repository, String[] filePathes);

	void unstageChanges(Repository repository, String[] filePathes);

	/**
	 * 提交变更的模型
	 * 
	 * @param user
	 * @param project
	 * @param commitMessage
	 * @return 返回 commitId
	 */
	String commit(UserInfo user, Repository project, String commitMessage);

	void updatePageModel(Repository repository, RepositoryResource projectResource, PageModel pageModel);

	/**
	 * 获取页面模型
	 * 
	 * @param page 页面信息
	 * @return 返回页面模型，不能返回 null，如果页面模型中没有内容，则返回空的 PageModel 对象
	 */
	PageModel getPageModel(RepositoryResource page);

	/**
	 * 默认创建的空页面中包含一个 Page 部件
	 * 
	 * @param page 页面基本信息
	 * @return 页面模型
	 */
	PageModel createPageModelWithStdPage(RepositoryResource page);

	/**
	 * 获取项目基本信息，一个仓库的根目录下可存放多个项目。
	 * 
	 * @param repositoryId 仓库标识
	 * @param projectKey 项目的 key 值
	 * @return 项目基本信息
	 */
	Optional<RepositoryResource> findProject(Integer repositoryId, String projectKey);
	
	/**
	 * 获取一个仓库下的所有项目
	 * 
	 * @param repositoryId 仓库标识
	 * @return 项目列表，没有项目时返回空列表
	 */
	List<RepositoryResource> findAllProject(Integer repositoryId);
}
