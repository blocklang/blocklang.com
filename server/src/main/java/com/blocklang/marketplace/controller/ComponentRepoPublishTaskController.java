package com.blocklang.marketplace.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.UserService;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.service.ComponentRepoPublishTaskService;

@RestController
public class ComponentRepoPublishTaskController {

	@Autowired
	private UserService userService;
	@Autowired
	private ComponentRepoPublishTaskService componentRepoPublishTaskService;
	
	/**
	 * 获取登录用户的正在运行的组件库发布任务
	 * 
	 * @param principal
	 * @return
	 */
	@GetMapping("/user/component-repos/publishing-tasks")
	public ResponseEntity<List<ComponentRepoPublishTask>> listMyComponentRepoPublishTasks(
			Principal principal) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		
		UserInfo userInfo = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		List<ComponentRepoPublishTask> result = componentRepoPublishTaskService.findUserPublishingTasks(userInfo.getId());
		
		return ResponseEntity.ok(result);
	}
	
}
