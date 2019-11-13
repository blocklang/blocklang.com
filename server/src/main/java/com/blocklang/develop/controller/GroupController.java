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

import com.blocklang.core.constant.Constant;
import com.blocklang.core.controller.SpringMvcUtil;
import com.blocklang.core.exception.InvalidRequestException;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.UserService;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.data.CheckGroupKeyParam;
import com.blocklang.develop.data.CheckGroupNameParam;
import com.blocklang.develop.data.NewGroupParam;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectResourceService;

@RestController
public class GroupController extends AbstractProjectController{

	private static final Logger logger = LoggerFactory.getLogger(GroupController.class);

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
		projectPermissionService.canWrite(principal, project).orElseThrow(NoAuthorizationException::new);
		
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
			ProjectResource parentResource = projectResourceService.findById(parentId).get();
			String parentResourceName = StringUtils.isBlank(parentResource.getName()) ? parentResource.getKey() : parentResource.getName();
			return new Object[] {parentResourceName, key};
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
		projectPermissionService.canWrite(principal, project).orElseThrow(NoAuthorizationException::new);
		
		// FIXME: 进一步梳理以下代码
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
				ProjectResource parentResource = projectResourceService.findById(parentId).get();
				String parentResourceName = StringUtils.isBlank(parentResource.getName()) ? parentResource.getKey() : parentResource.getName();
				
				return new Object[] {parentResourceName, name};
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
		projectPermissionService.canWrite(principal, project).orElseThrow(NoAuthorizationException::new);
		
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
	public ResponseEntity<Map<String, Object>> getGroupTree(
			Principal user,
			@PathVariable String owner,
			@PathVariable String projectName,
			HttpServletRequest req) {
		
		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		projectPermissionService.canRead(user, project).orElseThrow(NoAuthorizationException::new);
		
		String groupPath = SpringMvcUtil.getRestUrl(req, 4);
		Map<String, Object> result = getGroupIdAndParentPath(project.getId(), groupPath);
		Integer groupId = (Integer) result.get("id");
		
		List<ProjectResource> children = projectResourceService.findChildren(project, groupId);
		children.forEach(projectResource -> {
			projectResource.setMessageSource(messageSource);
		});
		
		result.put("childResources", children);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/projects/{owner}/{projectName}/group-path/**")
	public ResponseEntity<Map<String, Object>> getGroupPath(
			Principal user,
			@PathVariable String owner,
			@PathVariable String projectName,
			HttpServletRequest req) {
		
		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		projectPermissionService.canRead(user, project).orElseThrow(NoAuthorizationException::new);
		
		String groupPath = SpringMvcUtil.getRestUrl(req, 4);
		
		Map<String, Object> result = getGroupIdAndParentPath(project.getId(), groupPath);
		return ResponseEntity.ok(result);
	}

	private Map<String, Object> getGroupIdAndParentPath(Integer projectId, String groupPath) {
		Integer id = null;
		List<Map<String, String>> stripedParentGroups;
		
		if(StringUtils.isBlank(groupPath)) {
			// 当前分组的标识，如果是项目的根节点，则值为 -1
			id = Constant.TREE_ROOT_ID;
			stripedParentGroups = Collections.emptyList();
		} else {
			// 要校验根据 parentPath 中的所有节点都能准确匹配
			List<ProjectResource> parentGroups = projectResourceService.findParentGroupsByParentPath(projectId, groupPath);
			// 因为 parentPath 有值，所以理应能查到记录
			if(parentGroups.isEmpty()) {
				logger.error("根据传入的 parent path 没有找到对应的标识");
				throw new ResourceNotFoundException();
			}
			stripedParentGroups = stripResourcePathes(parentGroups);
			id = parentGroups.get(parentGroups.size() - 1).getId();
		}
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("id", id);
		result.put("parentGroups", stripedParentGroups);
		return result;
	}
}
