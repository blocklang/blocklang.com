package com.blocklang.develop.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.constant.Constant;
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
		
		String key = param.getKey().trim();
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
		
		if(bindingResult.hasErrors()) {
			throw new InvalidRequestException(bindingResult);
		}
		
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(project.getId());
		resource.setParentId(param.getParentId());
		resource.setAppType(AppType.UNKNOWN);
		resource.setKey(key);
		resource.setName(name);
		if(param.getDescription() != null) {
			resource.setDescription(param.getDescription().trim());
		}
		resource.setResourceType(ProjectResourceType.GROUP);
		
		UserInfo currentUser = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		resource.setCreateUserId(currentUser.getId());
		resource.setCreateTime(LocalDateTime.now());
		
		ProjectResource savedProjectResource = projectResourceService.insert(resource);
		savedProjectResource.setMessageSource(messageSource);
		return new ResponseEntity<ProjectResource>(savedProjectResource, HttpStatus.CREATED);
	}
}
