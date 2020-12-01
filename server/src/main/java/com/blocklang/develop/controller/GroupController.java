package com.blocklang.develop.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.constant.Constant;
import com.blocklang.core.controller.SpringMvcUtil;
import com.blocklang.core.exception.InvalidRequestException;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.PropertyService;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.DeviceType;
import com.blocklang.develop.constant.RepositoryResourceType;
import com.blocklang.develop.data.CheckGroupKeyParam;
import com.blocklang.develop.data.CheckGroupNameParam;
import com.blocklang.develop.data.NewGroupParam;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.RepositoryResourceService;
import com.blocklang.marketplace.model.ApiWidget;
import com.blocklang.marketplace.service.ApiRepoService;
import com.blocklang.marketplace.service.ApiRepoVersionService;
import com.blocklang.marketplace.service.ApiWidgetService;

/**
 * 维护仓库中目录结构的控制器。
 * 
 * <p>目录结构包含两种：第一层目录称为项目；第二层及后续目录称为分组。
 * 
 * @author Zhengwei Jin
 *
 */
@RestController
public class GroupController extends AbstractRepositoryController{

	private static final Logger logger = LoggerFactory.getLogger(GroupController.class);

	@Autowired
	private RepositoryResourceService repositoryResourceService;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private PropertyService propertyService;
	@Autowired
	private ApiRepoService apiRepoService;
	@Autowired
	private ApiRepoVersionService apiRepoVersionService;
	@Autowired
	private ApiWidgetService apiWidgetService;
	
	@PostMapping("/repos/{owner}/{repoName}/groups/check-key")
	public ResponseEntity<Map<String, String>> checkKey(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("repoName") String repoName,
			@Valid @RequestBody CheckGroupKeyParam param, 
			BindingResult bindingResult){
		
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Repository repository = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canWrite(principal, repository).orElseThrow(NoAuthorizationException::new);
		
		//校验 key: 是否为空
		if(bindingResult.hasErrors()) {
			logger.error("名称不能为空");
			throw new InvalidRequestException(bindingResult);
		}
		
		String key = param.getKey().trim();
		//校验：只支持英文字母、数字、中划线(-)、下划线(_)、点(.)
		String regEx = "^[a-zA-Z0-9\\-\\w]+$";
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(key);
		if(!matcher.matches()) {
			logger.error("包含非法字符");
			bindingResult.rejectValue("key", "NotValid.groupKey");
			throw new InvalidRequestException(bindingResult);
		}
		
		// FIXME: 进一步梳理
		Integer parentId = param.getParentId();
		repositoryResourceService.findByKey(
				repository.getId(), 
				parentId, 
				RepositoryResourceType.GROUP, 
				AppType.UNKNOWN,
				key).map(resource -> {
			logger.error("key 已被占用");
			
			if(parentId == Constant.TREE_ROOT_ID) {
				return new Object[] {"根目录", key};
			}
			
			// 这里不需要做是否存在判断，因为肯定存在。
			RepositoryResource parentResource = repositoryResourceService.findById(parentId).get();
			String parentResourceName = StringUtils.isBlank(parentResource.getName()) ? parentResource.getKey() : parentResource.getName();
			return new Object[] {parentResourceName, key};
		}).ifPresent(args -> {
			bindingResult.rejectValue("key", "Duplicated.groupKey", args, null);
			throw new InvalidRequestException(bindingResult);
		});
		
		return ResponseEntity.ok(Collections.emptyMap());
	}
	
	@PostMapping("/repos/{owner}/{repoName}/groups/check-name")
	public ResponseEntity<Map<String, String>> checkName(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("repoName") String repoName,
			@Valid @RequestBody CheckGroupNameParam param, 
			BindingResult bindingResult){

		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Repository repository = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canWrite(principal, repository).orElseThrow(NoAuthorizationException::new);
		
		// FIXME: 进一步梳理以下代码
		// name 的值可以为空
		if(StringUtils.isNotBlank(param.getName())) {
			String name = param.getName().trim();
			
			Integer parentId = param.getParentId();
			repositoryResourceService.findByName(
					repository.getId(), 
					parentId, 
					RepositoryResourceType.GROUP, 
					AppType.UNKNOWN,
					name).map(resource -> {
				logger.error("name 已被占用");
				
				if(parentId == Constant.TREE_ROOT_ID) {
					return new Object[] {"根目录", name};
				}
				
				// 这里不需要做是否存在判断，因为肯定存在。
				RepositoryResource parentResource = repositoryResourceService.findById(parentId).get();
				String parentResourceName = StringUtils.isBlank(parentResource.getName()) ? parentResource.getKey() : parentResource.getName();
				
				return new Object[] {parentResourceName, name};
			}).ifPresent(args -> {
				bindingResult.rejectValue("name", "Duplicated.groupName", args, null);
				throw new InvalidRequestException(bindingResult);
			});
		}
		
		return ResponseEntity.ok(Collections.emptyMap());
	}

	@PostMapping("/repos/{owner}/{repoName}/groups")
	public ResponseEntity<RepositoryResource> newGroup(
			Principal principal, 
			@PathVariable("owner") String owner,
			@PathVariable("repoName") String repoName,
			@Valid @RequestBody NewGroupParam param, 
			BindingResult bindingResult) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Repository repository = repositoryService
			.find(owner, repoName)
			.orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService
			.canWrite(principal, repository)
			.orElseThrow(NoAuthorizationException::new);
		
		//校验 key: 
		boolean keyIsValid = true;
		// 一、是否为空
		if(bindingResult.hasErrors()) {
			logger.error("名称不能为空");
			keyIsValid = false;
		}
		
		String key = Objects.toString(param.getKey(), "").trim();
		if(keyIsValid) {
			//校验：只支持英文字母、数字、中划线(-)、下划线(_)、点(.)
			String regEx = "^[a-zA-Z0-9\\-\\w]+$";
			Pattern pattern = Pattern.compile(regEx);
			Matcher matcher = pattern.matcher(key);
			if(!matcher.matches()) {
				logger.error("包含非法字符");
				bindingResult.rejectValue("key", "NotValid.groupKey");
				keyIsValid = false;
			}
		}
		
		if(keyIsValid) {
			Integer parentId = param.getParentId();
			repositoryResourceService.findByKey(
					repository.getId(), 
					parentId, 
					RepositoryResourceType.GROUP, 
					AppType.UNKNOWN,
					key).map(resource -> {
				logger.error("key 已被占用");
				
				if(parentId == Constant.TREE_ROOT_ID) {
					return new Object[] {"根目录", key};
				}
				
				// 这里不需要做是否存在判断，因为肯定存在。
				String parentResourceName = repositoryResourceService
					.findById(parentId)
					.get()
					.getName();
				return new Object[] {parentResourceName, key};
			}).ifPresent(args -> {
				bindingResult.rejectValue("key", 
						"Duplicated.groupKey", args, null);
			});
		}
		
		// 校验 name
		// name 可以为空
		if(StringUtils.isNotBlank(param.getName())) {
			String name = param.getName().trim();
			
			Integer parentId = param.getParentId();
			repositoryResourceService.findByName(
					repository.getId(), 
					parentId, 
					RepositoryResourceType.GROUP, 
					AppType.UNKNOWN,
					name).map(resource -> {
				logger.error("name 已被占用");
				
				if(parentId == Constant.TREE_ROOT_ID) {
					return new Object[] {"根目录", name};
				}
				
				// 这里不需要做是否存在判断，因为肯定存在。
				String parentResourceName = repositoryResourceService
						.findById(parentId)
						.get()
						.getName();
				return new Object[] {parentResourceName, name};
			}).ifPresent(args -> {
				bindingResult.rejectValue("name", "Duplicated.pageName", args, null);
			});
		}
		
		if(bindingResult.hasErrors()) {
			throw new InvalidRequestException(bindingResult);
		}
		
		if(param.getResourceType().equals(RepositoryResourceType.GROUP.getKey())) {
			RepositoryResource resource = new RepositoryResource();
			resource.setRepositoryId(repository.getId());
			resource.setParentId(param.getParentId());
			resource.setAppType(AppType.fromKey(param.getAppType()));
			resource.setKey(key);
			resource.setName(param.getName() == null ? null : param.getName().trim());
			if(param.getDescription() != null) {
				resource.setDescription(param.getDescription().trim());
			}
			resource.setResourceType(RepositoryResourceType.fromKey(param.getResourceType()));
			
			UserInfo currentUser = userService
				.findByLoginName(principal.getName())
				.orElseThrow(NoAuthorizationException::new);
			resource.setCreateUserId(currentUser.getId());
			resource.setCreateTime(LocalDateTime.now());
			
			RepositoryResource savedProjectResource = repositoryResourceService.insert(repository, resource);
			savedProjectResource.setMessageSource(messageSource);
			return new ResponseEntity<RepositoryResource>(savedProjectResource, HttpStatus.CREATED);
		}
		
		if(param.getResourceType().equals(RepositoryResourceType.PROJECT.getKey())) {
			RepositoryResource resource = new RepositoryResource();
			resource.setRepositoryId(repository.getId());
			resource.setParentId(param.getParentId());
			resource.setAppType(AppType.fromKey(param.getAppType()));
			resource.setKey(key);
			resource.setName(param.getName() == null ? null : param.getName().trim());
			resource.setResourceType(RepositoryResourceType.fromKey(param.getResourceType()));
			
			UserInfo currentUser = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
			resource.setCreateUserId(currentUser.getId());
			resource.setCreateTime(LocalDateTime.now());
			
			RepositoryResource savedProjectResource = null;
			if(param.getAppType().equals(AppType.WEB.getKey())) {
				// 创建 web 项目
				// 是否注册 web 的 API 库
				// web 的 API 库中是否存在 Page 组件
				savedProjectResource = repositoryResourceService.createWebProject(repository, resource);
			} else if(param.getAppType().equals(AppType.MINI_PROGRAM.getKey())) {
				// 创建小程序
				String apiGitUrl = propertyService
					.findStringValue(CmPropKey.STD_MINI_PROGRAM_COMPONENT_API_GIT_URL)
					.orElseGet(() -> {
						bindingResult.reject("NotExist.std.repo.url");
						return null;
					});
				Integer userId = propertyService
					.findIntegerValue(CmPropKey.STD_REPO_REGISTER_USER_ID)
					.orElseGet(() -> {
						bindingResult.reject("NotExist.std.register.user.id");
						return null;
					});
				// 是否注册小程序的 API 库
				var apiRepo = apiRepoService
					.findByGitUrlAndCreateUserId(apiGitUrl, userId)
					.orElseGet(() -> {
						bindingResult.reject("NotExist.std.api.repo", new Object[] {apiGitUrl}, null);
						return null;
					});
				if(bindingResult.hasErrors()) {
					throw new InvalidRequestException(bindingResult);
				}
				var apiRepoVersion = apiRepoVersionService
						.findMasterVersion(apiRepo.getId())
						.orElseGet(() -> {
							bindingResult.reject("NotExist.std.api.repo.master");
							return null;
						});
				if(bindingResult.hasErrors()) {
					throw new InvalidRequestException(bindingResult);
				}
				// 小程序的 API 库中是否存在 APP 组件
				String appWidgetName = propertyService
					.findStringValue(CmPropKey.STD_MINI_PROGRAM_COMPONENT_APP_NAME)
					.orElseGet(() -> {
						bindingResult.reject("NotExist.std.app.name");
						return null;
					});
				ApiWidget appWidget = apiWidgetService
					.findByApiRepoVersionIdAndNameIgnoreCase(apiRepoVersion.getId(), appWidgetName)
					.orElseThrow(ResourceNotFoundException::new);
				// 小程序的 API 库中是否存在 Page 组件
				String pageWidgetName = propertyService
					.findStringValue(CmPropKey.STD_MINI_PROGRAM_COMPONENT_PAGE_NAME)
					.orElseGet(() -> {
						bindingResult.reject("NotExist.std.page.name");
						return null;
					});
				ApiWidget pageWidget = apiWidgetService
					.findByApiRepoVersionIdAndNameIgnoreCase(apiRepoVersion.getId(), pageWidgetName)
					.orElseThrow(ResourceNotFoundException::new);
				
				if(bindingResult.hasErrors()) {
					throw new InvalidRequestException(bindingResult);
				}
				
				// 在 service 中默认依赖小程序的 API 库的 master，且不允许删除该依赖
				savedProjectResource = repositoryResourceService.createMiniProgram(repository, resource, apiRepo, appWidget, pageWidget);
			} else if (param.getAppType().equals(AppType.HARMONYOS.getKey())) {
				// 创建鸿蒙应用
				// 1. Lite Wearable
				if(DeviceType.LITE_WEARABLE.getKey().equals(param.getDeviceType())) {
					String apiGitUrl = propertyService
							.findStringValue(CmPropKey.STD_HARMONYOS_LITE_WEARABLE_UI_API_GIT_URL)
							.orElseGet(() -> {
								bindingResult.reject("NotExist.std.repo.url");
								return null;
							});
					Integer userId = propertyService
							.findIntegerValue(CmPropKey.STD_REPO_REGISTER_USER_ID)
							.orElseGet(() -> {
								bindingResult.reject("NotExist.std.register.user.id");
								return null;
							});
					// 是否在组件市场注册了 Lite Wearable 的 API 库
					var apiRepo = apiRepoService
							.findByGitUrlAndCreateUserId(apiGitUrl, userId)
							.orElseGet(() -> {
								bindingResult.reject("NotExist.std.api.repo", new Object[] {apiGitUrl}, null);
								return null;
							});
					if(bindingResult.hasErrors()) {
						throw new InvalidRequestException(bindingResult);
					}
					var apiRepoVersion = apiRepoVersionService
							.findMasterVersion(apiRepo.getId())
							.orElseGet(() -> {
								bindingResult.reject("NotExist.std.api.repo.master");
								return null;
							});
					if(bindingResult.hasErrors()) {
						throw new InvalidRequestException(bindingResult);
					}
					// Lite Wearable 的 API 库中是否存在 APP 组件
					String appWidgetName = propertyService
							.findStringValue(CmPropKey.STD_HARMONYOS_LITE_WEARABLE_UI_APP_NAME)
							.orElseGet(() -> {
								bindingResult.reject("NotExist.std.app.name");
								return null;
							});
					ApiWidget appWidget = apiWidgetService
							.findByApiRepoVersionIdAndNameIgnoreCase(apiRepoVersion.getId(), appWidgetName)
							.orElseThrow(ResourceNotFoundException::new);
					// 小程序的 API 库中是否存在 Page 组件
					String pageWidgetName = propertyService
							.findStringValue(CmPropKey.STD_HARMONYOS_LITE_WEARABLE_UI_PAGE_NAME)
							.orElseGet(() -> {
								bindingResult.reject("NotExist.std.page.name");
								return null;
							});
					ApiWidget pageWidget = apiWidgetService
							.findByApiRepoVersionIdAndNameIgnoreCase(apiRepoVersion.getId(), pageWidgetName)
							.orElseThrow(ResourceNotFoundException::new);
					
					if(bindingResult.hasErrors()) {
						throw new InvalidRequestException(bindingResult);
					}
					
					// 在 service 中默认依赖 HarmonyOS Lite Wearable API 库的 master，且不允许删除该依赖
					savedProjectResource = repositoryResourceService.createHarmonyOSLiteWearableProject(repository, resource, apiRepo, appWidget, pageWidget);
				}
			}
			
			savedProjectResource.setMessageSource(messageSource);
			return new ResponseEntity<RepositoryResource>(savedProjectResource, HttpStatus.CREATED);
		}
		
		// 此方法仅支持 resourceType 的值为项目或分组
		throw new ResourceNotFoundException();
	}
	
	@GetMapping("/repos/{owner}/{repoName}/groups/**")
	public ResponseEntity<Map<String, Object>> getGroupTree(
			Principal user,
			@PathVariable String owner,
			@PathVariable String repoName,
			HttpServletRequest req) {
		
		Repository repository = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canRead(user, repository).orElseThrow(NoAuthorizationException::new);
		
		String groupPath = SpringMvcUtil.getRestUrl(req, 4);
		Map<String, Object> result = getGroupIdAndParentPath(repository.getId(), groupPath);
		Integer groupId = (Integer) result.get("id");
		
		List<RepositoryResource> children = repositoryResourceService.findChildren(repository, groupId);
		children.forEach(projectResource -> {
			projectResource.setMessageSource(messageSource);
		});
		
		result.put("childResources", children);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/repos/{owner}/{repoName}/group-path/**")
	public ResponseEntity<Map<String, Object>> getGroupPath(
			Principal user,
			@PathVariable String owner,
			@PathVariable String repoName,
			HttpServletRequest req) {
		
		Repository repository = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canRead(user, repository).orElseThrow(NoAuthorizationException::new);
		
		String groupPath = SpringMvcUtil.getRestUrl(req, 4);
		
		Map<String, Object> result = getGroupIdAndParentPath(repository.getId(), groupPath);
		return ResponseEntity.ok(result);
	}

	private Map<String, Object> getGroupIdAndParentPath(Integer repositoryId, String groupPath) {
		Integer id = null;
		List<Map<String, String>> stripedParentGroups;
		
		if(StringUtils.isBlank(groupPath)) {
			// 当前分组的标识，如果是项目的根节点，则值为 -1
			id = Constant.TREE_ROOT_ID;
			stripedParentGroups = Collections.emptyList();
		} else {
			// 要校验根据 parentPath 中的所有节点都能准确匹配
			List<RepositoryResource> parentGroups = repositoryResourceService.findParentGroupsByParentPath(repositoryId, groupPath);
			// 因为 parentPath 有值，所以理应能查到记录
			if(parentGroups.isEmpty()) {
				logger.error("根据传入的 parent path 没有找到对应的标识");
				throw new ResourceNotFoundException();
			}
			stripedParentGroups = RepositoryResourcePathUtil.combinePathes(parentGroups);
			id = parentGroups.get(parentGroups.size() - 1).getId();
		}
		
		Map<String, Object> result = new HashMap<String, Object>(2);
		result.put("id", id);
		result.put("parentGroups", stripedParentGroups);
		return result;
	}
}
