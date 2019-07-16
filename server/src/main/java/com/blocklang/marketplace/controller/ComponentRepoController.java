package com.blocklang.marketplace.controller;

import java.net.URISyntaxException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.transport.URIish;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.exception.InvalidRequestException;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.UserService;
import com.blocklang.core.util.GitUrlParser;
import com.blocklang.core.util.GitUrlSegment;
import com.blocklang.marketplace.constant.PublishType;
import com.blocklang.marketplace.data.ComponentRepoInfo;
import com.blocklang.marketplace.data.NewComponentRepoParam;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.service.ComponentRepoPublishTaskService;
import com.blocklang.marketplace.service.ComponentRepoService;
import com.blocklang.marketplace.service.PublishService;
import com.blocklang.release.constant.ReleaseResult;

@RestController
public class ComponentRepoController {
	
	@Autowired
	private UserService userService;
	@Autowired
	private ComponentRepoService componentRepoService;
	@Autowired
	private ComponentRepoPublishTaskService componentRepoPublishTaskService;
	@Autowired
	private PublishService publishService;

	@GetMapping("/component-repos")
	public ResponseEntity<Page<ComponentRepo>> listComponentRepos(
			@RequestParam(value="q", required = false)String query, 
			@RequestParam(required = false) String page) {
		Integer iPage = null;
		if(StringUtils.isBlank(page)){
			iPage = 0;
		}else {
			try {
				iPage = Integer.valueOf(page);
			}catch (NumberFormatException e) {
				throw new ResourceNotFoundException();
			}
		}
		
		if(iPage < 0) {
			throw new ResourceNotFoundException();
		}
		
		// 默认一页显示 60 项组件仓库
		Pageable pageable = PageRequest.of(iPage, 60, Sort.by(Direction.DESC, "lastPublishTime"));
		Page<ComponentRepo> result = componentRepoService.findAllByNameOrLabel(query, pageable);
		
		if(iPage > result.getTotalPages()) {
			throw new ResourceNotFoundException();
		}
		
		return ResponseEntity.ok(result);
	}
	
	@GetMapping("/user/component-repos")
	public ResponseEntity<List<ComponentRepoInfo>> listMyComponentRepos(
			Principal principal) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		UserInfo userInfo = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		List<ComponentRepoInfo> result = componentRepoService.findUserComponentRepos(userInfo.getId());

		return ResponseEntity.ok(result);
	}
	
	/**
	 * 
	 * 
	 * <p>
	 * controller 中只做两件事
	 * <ol>
	 * <li>校验 git url 是否为空</li>
	 * <li>保存组件库发布任务</li>
	 * </ol>
	 * 
	 * 其余放在异步 service 中处理
	 * </p>
	 * 
	 * 
	 * @param principal
	 * @param param
	 * @param bindingResult
	 * @return
	 */
	@PostMapping("/component-repos")
	public ResponseEntity<ComponentRepoPublishTask> newComponentRepo(
			Principal principal,
			@Valid @RequestBody NewComponentRepoParam param, 
			BindingResult bindingResult) {
		
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		
		if(bindingResult.hasErrors()) {
			throw new InvalidRequestException(bindingResult);
		}
		
		String gitUrl = param.getGitUrl().trim();
		
		URIish uriish = null;
		try {
			uriish = new URIish(gitUrl);
		} catch (URISyntaxException e) {
			bindingResult.rejectValue("gitUrl", "NotValid.componentRepoGitUrl.invalidRemoteUrl");
			throw new InvalidRequestException(bindingResult);
		}
		if(!uriish.isRemote()) {
			bindingResult.rejectValue("gitUrl", "NotValid.componentRepoGitUrl.invalidRemoteUrl");
			throw new InvalidRequestException(bindingResult);
		}
		
		if(!"https".equalsIgnoreCase(uriish.getScheme())) {
			bindingResult.rejectValue("gitUrl", "NotValid.componentRepoGitUrl.shouldBeHttps");
			throw new InvalidRequestException(bindingResult);
		}
		
		GitUrlSegment gitUrlSegment = GitUrlParser.parse(gitUrl).orElseThrow(() -> {
			bindingResult.rejectValue("gitUrl", "NotValid.componentRepoGitUrl.shouldBeHttps");
			throw new InvalidRequestException(bindingResult);
		});
		
		UserInfo currentUser = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		Integer currentUserId = currentUser.getId();
		
		// 如果已经发布过，则不允许新增发布任务；而是在之前任务的基础上重新发布或升级
		if(componentRepoPublishTaskService.existsByCreateUserIdAndGitUrl(currentUser.getId(), gitUrl)){
			bindingResult.rejectValue("gitUrl", "Duplicated.componentRepoGitUrl", new Object[] {principal.getName()}, null);
			throw new InvalidRequestException(bindingResult);
		};
		
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		task.setGitUrl(gitUrl);
		task.setStartTime(LocalDateTime.now());
		task.setPublishType(PublishType.NEW);
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setCreateUserId(currentUserId);
		task.setCreateUserName(principal.getName());
		ComponentRepoPublishTask savedTask = componentRepoPublishTaskService.save(task);

		// 派生字段
		savedTask.setWebsite(gitUrlSegment.getWebsite());
		savedTask.setOwner(gitUrlSegment.getOwner());
		savedTask.setRepoName(gitUrlSegment.getRepoName());
		// 异步任务
		publishService.asyncPublish(savedTask);
		
		// 这里的 CREATED 只表示 task 创建成功，并不表示发布成功
		return new ResponseEntity<ComponentRepoPublishTask>(savedTask, HttpStatus.CREATED);
	}
	
}
