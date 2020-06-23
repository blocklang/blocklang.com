package com.blocklang.marketplace.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.service.UserService;
import com.blocklang.core.util.GitUrlSegment;
import com.blocklang.core.util.LogFileReader;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.model.GitRepoPublishTask;
import com.blocklang.marketplace.service.GitRepoPublishTaskService;

@RestController
public class GitRepoPublishTaskController {

	@Autowired
	private UserService userService;
	@Autowired
	private GitRepoPublishTaskService gitRepoPublishTaskService;
	@Autowired
	private PropertyService propertyService;
	
	/**
	 * 获取登录用户的正在运行的组件库发布任务
	 * 
	 * @param principal
	 * @return
	 */
	@GetMapping("/user/component-repos/publishing-tasks")
	public ResponseEntity<List<GitRepoPublishTask>> listMyComponentRepoPublishTasks(
			Principal principal) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		
		UserInfo userInfo = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		List<GitRepoPublishTask> result = gitRepoPublishTaskService.findUserPublishingTasks(userInfo.getId());
		
		return ResponseEntity.ok(result);
	}
	
	@GetMapping("/marketplace/publish/{taskId}")
	public ResponseEntity<GitRepoPublishTask> getComponentRepoPublishTask(
			Principal principal,
			@PathVariable("taskId") Integer taskId) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		
		GitRepoPublishTask task = gitRepoPublishTaskService.findById(taskId).orElseThrow(ResourceNotFoundException::new);
		if(!task.getCreateUserName().equalsIgnoreCase(principal.getName())) {
			throw new NoAuthorizationException();
		}
		
		var segment = GitUrlSegment.of(task.getGitUrl());
		if(segment != null) {
			task.setWebsite(segment.getWebsite());
			task.setOwner(segment.getOwner());
			task.setRepoName(segment.getRepoName());
		}
		
		return ResponseEntity.ok(task);
	}
	
	@GetMapping("/marketplace/publish/{taskId}/log")
	public ResponseEntity<List<String>> getPublishLog(
			Principal principal,
			@PathVariable("taskId") Integer taskId) {
		
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		
		GitRepoPublishTask task = gitRepoPublishTaskService.findById(taskId)
				.orElseThrow(ResourceNotFoundException::new);
		if(!task.getCreateUserName().equalsIgnoreCase(principal.getName())) {
			throw new NoAuthorizationException();
		}
		
		String dataRootPath = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)
				.orElseThrow(ResourceNotFoundException::new);
		MarketplaceStore store = new MarketplaceStore(dataRootPath, task.getGitUrl());
		List<String> logContent = LogFileReader.readAllLines(store.getLogFilePath(task.getLogFileName()));
		return ResponseEntity.ok(logContent);
	}
	
}
