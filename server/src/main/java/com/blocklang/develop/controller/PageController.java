package com.blocklang.develop.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.data.CheckPageKeyParam;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectAuthorization;
import com.blocklang.develop.service.ProjectAuthorizationService;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.develop.service.ProjectService;

@RestController
public class PageController {
	private static final Logger logger = LoggerFactory.getLogger(PageController.class);
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ProjectAuthorizationService projectAuthorizationService;
	@Autowired
	private ProjectResourceService projectResourceService;

	@PostMapping("/projects/{owner}/{projectName}/pages/check-key")
	public ResponseEntity<Map<String, String>> checkKey(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("projectName") String projectName,
			@Valid @RequestBody CheckPageKeyParam param, 
			BindingResult bindingResult){
		
		if(principal == null) {
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
			bindingResult.rejectValue("key", "NotValid.pageKey");
			throw new InvalidRequestException(bindingResult);
		}
		
		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
		List<ProjectAuthorization> authes = projectAuthorizationService.findAllByUserIdAndProjectId(project.getCreateUserId(), project.getId());
		
		boolean canWrite = authes.stream().anyMatch(item -> item.getAccessLevel() == AccessLevel.WRITE || item.getAccessLevel() == AccessLevel.ADMIN);
		
		if(!canWrite) {
			throw new NoAuthorizationException();
		}
		
		Integer groupId = param.getGroupId();
		projectResourceService.find(
				project.getId(), 
				param.getGroupId(), 
				ProjectResourceType.PAGE, 
				param.getAppType(),
				key).map(resource -> {
			logger.error("key 已被占用");
			
			if(groupId == Constant.TREE_ROOT_ID) {
				return new Object[] {"根目录", key};
			}
			
			// 这里不需要做是否存在判断，因为肯定存在。
			return new Object[] {projectResourceService.findById(groupId).get().getName(), key};
		}).ifPresent(args -> {
			bindingResult.rejectValue("key", "Duplicated.pageKey", args, null);
			throw new InvalidRequestException(bindingResult);
		});
		
		return ResponseEntity.ok(new HashMap<String, String>());
	}
}
