package com.blocklang.develop.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

import com.blocklang.core.constant.Constant;
import com.blocklang.core.controller.SpringMvcUtil;
import com.blocklang.core.exception.InvalidRequestException;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.UserService;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.data.CheckGroupKeyParam;
import com.blocklang.develop.data.CheckGroupNameParam;
import com.blocklang.develop.data.NewGroupParam;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectAuthorization;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectAuthorizationService;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.develop.service.ProjectService;

@RestController
public class GroupController {

	private static final Logger logger = LoggerFactory.getLogger(GroupController.class);
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ProjectAuthorizationService projectAuthorizationService;
	@Autowired
	private ProjectResourceService projectResourceService;
	@Autowired
	private UserService userService;
	@Autowired
	private MessageSource messageSource;
	
	@PostMapping("/projects/{owner}/{projectName}/groups/check-key")
	public ResponseEntity<Map<String, String>> checkKey(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName,
			@Valid @RequestBody CheckGroupKeyParam param, 
			BindingResult bindingResult){
		
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		
		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		
		List<ProjectAuthorization> authes = projectAuthorizationService.findAllByUserIdAndProjectId(project.getCreateUserId(), project.getId());
		boolean canWrite = authes.stream().anyMatch(item -> item.getAccessLevel() == AccessLevel.WRITE || item.getAccessLevel() == AccessLevel.ADMIN);
		if(!canWrite) {
			throw new NoAuthorizationException();
		}
		
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
		
		Integer parentId = param.getParentId();
		projectResourceService.findByKey(
				project.getId(), 
				parentId, 
				ProjectResourceType.GROUP, 
				AppType.UNKNOWN,
				key).map(resource -> {
			logger.error("key 已被占用");
			
			if(parentId == Constant.TREE_ROOT_ID) {
				return new Object[] {"根目录", key};
			}
			
			// 这里不需要做是否存在判断，因为肯定存在。
			return new Object[] {projectResourceService.findById(parentId).get().getName(), key};
		}).ifPresent(args -> {
			bindingResult.rejectValue("key", "Duplicated.groupKey", args, null);
			throw new InvalidRequestException(bindingResult);
		});
		
		return ResponseEntity.ok(new HashMap<String, String>());
	}
	
	@PostMapping("/projects/{owner}/{projectName}/groups/check-name")
	public ResponseEntity<Map<String, String>> checkName(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName,
			@Valid @RequestBody CheckGroupNameParam param, 
			BindingResult bindingResult){

		if(principal == null) {
			throw new NoAuthorizationException();
		}
		
		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		
		List<ProjectAuthorization> authes = projectAuthorizationService.findAllByUserIdAndProjectId(project.getCreateUserId(), project.getId());
		boolean canWrite = authes.stream().anyMatch(item -> item.getAccessLevel() == AccessLevel.WRITE || item.getAccessLevel() == AccessLevel.ADMIN);
		if(!canWrite) {
			throw new NoAuthorizationException();
		}
		
		// name 的值可以为空
		if(StringUtils.isNotBlank(param.getName())) {
			String name = param.getName().trim();
			
			Integer parentId = param.getParentId();
			projectResourceService.findByName(
					project.getId(), 
					parentId, 
					ProjectResourceType.GROUP, 
					AppType.UNKNOWN,
					name).map(resource -> {
				logger.error("name 已被占用");
				
				if(parentId == Constant.TREE_ROOT_ID) {
					return new Object[] {"根目录", name};
				}
				
				// 这里不需要做是否存在判断，因为肯定存在。
				return new Object[] {projectResourceService.findById(parentId).get().getName(), name};
			}).ifPresent(args -> {
				bindingResult.rejectValue("name", "Duplicated.groupName", args, null);
				throw new InvalidRequestException(bindingResult);
			});
		}
		
		return ResponseEntity.ok(new HashMap<String, String>());
	}

	@PostMapping("/projects/{owner}/{projectName}/groups")
	public ResponseEntity<ProjectResource> newGroup(
			Principal principal, 
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName,
			@Valid @RequestBody NewGroupParam param, 
			BindingResult bindingResult) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		
		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		
		List<ProjectAuthorization> authes = projectAuthorizationService.findAllByUserIdAndProjectId(project.getCreateUserId(), project.getId());
		boolean canWrite = authes.stream().anyMatch(item -> item.getAccessLevel() == AccessLevel.WRITE || item.getAccessLevel() == AccessLevel.ADMIN);
		if(!canWrite) {
			throw new NoAuthorizationException();
		}
		
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
			projectResourceService.findByKey(
					project.getId(), 
					parentId, 
					ProjectResourceType.GROUP, 
					AppType.UNKNOWN,
					key).map(resource -> {
				logger.error("key 已被占用");
				
				if(parentId == Constant.TREE_ROOT_ID) {
					return new Object[] {"根目录", key};
				}
				
				// 这里不需要做是否存在判断，因为肯定存在。
				return new Object[] {projectResourceService.findById(parentId).get().getName(), key};
			}).ifPresent(args -> {
				bindingResult.rejectValue("key", "Duplicated.groupKey", args, null);
			});
		}
		
		// 校验 name
		// name 可以为空
		if(StringUtils.isNotBlank(param.getName())) {
			String name = param.getName().trim();
			
			Integer parentId = param.getParentId();
			projectResourceService.findByName(
					project.getId(), 
					parentId, 
					ProjectResourceType.GROUP, 
					AppType.UNKNOWN,
					name).map(resource -> {
				logger.error("name 已被占用");
				
				if(parentId == Constant.TREE_ROOT_ID) {
					return new Object[] {"根目录", name};
				}
				
				// 这里不需要做是否存在判断，因为肯定存在。
				return new Object[] {projectResourceService.findById(parentId).get().getName(), name};
			}).ifPresent(args -> {
				bindingResult.rejectValue("name", "Duplicated.pageName", args, null);
			});
		}
		
		if(bindingResult.hasErrors()) {
			throw new InvalidRequestException(bindingResult);
		}
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(project.getId());
		resource.setParentId(param.getParentId());
		resource.setAppType(AppType.UNKNOWN);
		resource.setKey(key);
		resource.setName(param.getName() == null ? null : param.getName().trim());
		if(param.getDescription() != null) {
			resource.setDescription(param.getDescription().trim());
		}
		resource.setResourceType(ProjectResourceType.GROUP);
		
		UserInfo currentUser = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		resource.setCreateUserId(currentUser.getId());
		resource.setCreateTime(LocalDateTime.now());
		
		ProjectResource savedProjectResource = projectResourceService.insert(project, resource);
		savedProjectResource.setMessageSource(messageSource);
		return new ResponseEntity<ProjectResource>(savedProjectResource, HttpStatus.CREATED);
	}
	
	@GetMapping("/projects/{owner}/{projectName}/groups/**")
	public ResponseEntity<Map<String, Object>> getGroup(
			Principal user,
			@PathVariable String owner,
			@PathVariable String projectName,
			HttpServletRequest req) {
		
		String parentPath = SpringMvcUtil.getRestUrl(req, 4);

		return projectService.find(owner, projectName).map((project) -> {
			if(!project.getIsPublic()) {
				// 1. 用户未登录时不能访问私有项目
				// 2. 用户虽然登录，但是不是项目的拥有者且没有访问权限，则不能访问
				if((user == null) || (user!= null && !owner.equals(user.getName()))) {
					throw new ResourceNotFoundException();
				}
			}
			
			Map<String, Object> result = new HashMap<String, Object>();
			Integer parentResourceId = null;
			if(StringUtils.isBlank(parentPath)) {
				parentResourceId = Constant.TREE_ROOT_ID;
			} else {
				// 要校验根据 parentPath 中的所有节点都能准确匹配
				List<ProjectResource> parentGroups = projectResourceService.findParentGroupsByParentPath(project.getId(), parentPath);
				// 因为 parentPath 有值，所以理应能查到记录
				if(parentGroups.isEmpty()) {
					logger.error("根据传入的 parent path 没有找到对应的标识");
					throw new ResourceNotFoundException();
				}
				
				List<Map<String, String>> stripedParentGroups = stripParentGroups(parentGroups);
				result.put("parentGroups", stripedParentGroups);
				parentResourceId = parentGroups.get(parentGroups.size() - 1).getId();
			}
			List<ProjectResource> tree = projectResourceService.findChildren(project, parentResourceId);
			tree.forEach(projectResource -> {
				projectResource.setMessageSource(messageSource);
			});
			
			result.put("parentId", parentResourceId);
			result.put("resources", tree);
			return ResponseEntity.ok(result);
		}).orElseThrow(ResourceNotFoundException::new);
	}

	@GetMapping("/projects/{owner}/{projectName}/group-path/**")
	public ResponseEntity<Map<String, Object>> getGroupPath(
			Principal user,
			@PathVariable String owner,
			@PathVariable String projectName,
			HttpServletRequest req) {
		
		String parentPath = SpringMvcUtil.getRestUrl(req, 4);
		
		return projectService.find(owner, projectName).map((project) -> {
			
			if(!project.getIsPublic()) {
				// 1. 用户未登录时不能访问私有项目
				// 2. 用户虽然登录，但是不是项目的拥有者且没有访问权限，则不能访问
				if((user == null) || (user!= null && !owner.equals(user.getName()))) {
					throw new ResourceNotFoundException();
				}
			}
			
			Map<String, Object> result = new HashMap<String, Object>();
			
			if(StringUtils.isBlank(parentPath)) {
				result.put("parentId", Constant.TREE_ROOT_ID);
				result.put("parentPath", "");
				result.put("parentGroups", new String[] {});
			} else {
				// 要校验根据 parentPath 中的所有节点都能准确匹配
				List<ProjectResource> parentGroups = projectResourceService.findParentGroupsByParentPath(project.getId(), parentPath);
				// 因为 parentPath 有值，所以理应能查到记录
				if(parentGroups.isEmpty()) {
					logger.error("根据传入的 parent path 没有找到对应的标识");
					throw new ResourceNotFoundException();
				}
				List<Map<String, String>> stripedParentGroups = stripParentGroups(parentGroups);
				result.put("parentGroups", stripedParentGroups);
				result.put("parentId", parentGroups.get(parentGroups.size() - 1).getId());
				result.put("parentPath", parentPath);
			}
			return ResponseEntity.ok(result);
		}).orElseThrow(ResourceNotFoundException::new);
		
	}

	private List<Map<String, String>> stripParentGroups(List<ProjectResource> parentGroups) {
		List<Map<String, String>> stripedParentGroups = new ArrayList<Map<String, String>>();
		String relativePath = "";
		for(ProjectResource each : parentGroups) {
			relativePath = relativePath + "/" + each.getKey();
			
			Map<String, String> map = new HashMap<String, String>();
			if(StringUtils.isBlank(each.getName())) {
				map.put("name", each.getKey());
			} else {
				map.put("name", each.getName());
			}
			
			map.put("path", relativePath);
			stripedParentGroups.add(map);
		}
		return stripedParentGroups;
	}
}
