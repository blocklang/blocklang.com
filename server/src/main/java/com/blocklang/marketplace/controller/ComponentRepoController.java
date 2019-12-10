package com.blocklang.marketplace.controller;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.transport.URIish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.exception.InvalidRequestException;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.service.UserService;
import com.blocklang.core.util.GitUrlParser;
import com.blocklang.core.util.GitUrlSegment;
import com.blocklang.marketplace.constant.PublishType;
import com.blocklang.marketplace.data.ComponentRepoInfo;
import com.blocklang.marketplace.data.NewComponentRepoParam;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.model.ComponentRepoVersion;
import com.blocklang.marketplace.service.ComponentRepoPublishTaskService;
import com.blocklang.marketplace.service.ComponentRepoService;
import com.blocklang.marketplace.service.ComponentRepoVersionService;
import com.blocklang.marketplace.service.PublishService;
import com.blocklang.release.constant.ReleaseResult;

@EnableAsync
@RestController
public class ComponentRepoController {
	private static final Logger logger = LoggerFactory.getLogger(ComponentRepoController.class);
	
	@Autowired
	private PropertyService propertyService;
	@Autowired
	private UserService userService;
	@Autowired
	private ComponentRepoService componentRepoService;
	@Autowired
	private ComponentRepoVersionService componentRepoVersionService;
	@Autowired
	private ComponentRepoPublishTaskService componentRepoPublishTaskService;
	@Autowired
	private PublishService publishService;

	@GetMapping("/component-repos")
	public ResponseEntity<Page<ComponentRepoInfo>> listComponentRepos(
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
		Pageable pageable = PageRequest.of(iPage, 60, Sort.by(Direction.ASC, "name"));
		Page<ComponentRepoInfo> result = componentRepoService.findAllByNameOrLabel(query, pageable);
		
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
	 * 往组件市场中发布组件
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
		
		// TODO:将这段校验，移到 app 启动之后，如果校验不通过，则无法使用该平台。
		// 在此处对系统参数进行有效性校验。
		// 准备阶段的校验，校验配置的路径在文件系统中是否存在，不要抛出以下异常
		// java.nio.file.FileSystemException: Unable to determine if root directory exists
		// 临时将错误信息放在 NewComponentRepoParam 中
		String dataRootPath = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH, "");
		if(dataRootPath.isBlank()) {
			logger.error("未配置参数 {}", CmPropKey.BLOCKLANG_ROOT_PATH);
			bindingResult.rejectValue("propertyConfig", "NotBlank.propertyValue", new Object[] {CmPropKey.BLOCKLANG_ROOT_PATH}, null);
			throw new InvalidRequestException(bindingResult);
		}
		if(Files.notExists(Path.of(dataRootPath))) {
			logger.error("参数 {} 配置的路径 {} 在文件系统中无效", CmPropKey.BLOCKLANG_ROOT_PATH, dataRootPath);
			bindingResult.rejectValue("propertyConfig", "NotValid.propertyValue.filePath", new Object[] {CmPropKey.BLOCKLANG_ROOT_PATH}, null);
			throw new InvalidRequestException(bindingResult);
		}
		
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
		// 不要从任务表中判断是否存在发布任务，因为任务可能会失败
		// 直接从组件库表中查找更准确
		if(componentRepoService.existsByCreateUserIdAndGitRepoUrl(currentUser.getId(), gitUrl)){
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
		publishService.asyncPublish(savedTask, dataRootPath);
		
		// 这里的 CREATED 只表示 task 创建成功，并不表示发布成功
		return new ResponseEntity<ComponentRepoPublishTask>(savedTask, HttpStatus.CREATED);
	}
	
	@GetMapping("/component-repos/{componentRepoId}/versions")
	public ResponseEntity<List<ComponentRepoVersion>> listComponentRepoVersions(
			@PathVariable Integer componentRepoId) {
		componentRepoService.findById(componentRepoId).orElseThrow(ResourceNotFoundException::new);
		List<ComponentRepoVersion> versions = componentRepoVersionService.findByComponentRepoId(componentRepoId);
		return ResponseEntity.ok(versions);
	}
}
