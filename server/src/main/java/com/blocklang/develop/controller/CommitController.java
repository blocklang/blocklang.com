package com.blocklang.develop.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.exception.InvalidRequestException;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.core.git.GitUtils;
import com.blocklang.core.git.exception.GitEmptyCommitException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.PropertyService;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.BuildTarget;
import com.blocklang.develop.data.CommitMessage;
import com.blocklang.develop.data.UncommittedFile;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.service.RepositoryResourceService;
import com.blocklang.release.constant.ReleaseResult;
import com.blocklang.release.data.MiniProgramStore;
import com.blocklang.release.model.ProjectReleaseTask;
import com.blocklang.release.service.BuildService;

/**
 * 
 * @author Zhengwei Jin
 *
 */
@RestController
public class CommitController extends AbstractRepositoryController{

	@Autowired
	private RepositoryResourceService repositoryResourceService;
	@Autowired
	private BuildService buildService;
	@Autowired
	private PropertyService propertyService;
	
	/**
	 * 如果是公开仓库，则任何人都能访问；如果是私有仓库，则有读的权限就能访问。
	 * 
	 * @param principal
	 * @param owner 仓库拥有者（用户登录名）
	 * @param repoName 仓库名称
	 * @return
	 */
	@GetMapping("/repos/{owner}/{repoName}/changes")
	public ResponseEntity<List<UncommittedFile>> listChanges(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("repoName") String repoName) {
		Repository repository = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canRead(principal, repository).orElseThrow(NoAuthorizationException::new);
		return ResponseEntity.ok(repositoryResourceService.findChanges(repository));
	}
	
	@PostMapping("/repos/{owner}/{repoName}/stage-changes")
	public ResponseEntity<Map<String, Object>> stageChanges(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("repoName") String repoName,
			@RequestBody String[] param) {
		// 必须要先登录
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Repository repository = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canWrite(principal, repository).orElseThrow(NoAuthorizationException::new);
		
		repositoryResourceService.stageChanges(repository, param);
		
		return ResponseEntity.ok(Collections.emptyMap());
	}
	
	@PostMapping("/repos/{owner}/{repoName}/unstage-changes")
	public ResponseEntity<Map<String, Object>> unstageChanges(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("repoName") String repoName,
			@RequestBody String[] param) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Repository repository = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canWrite(principal, repository).orElseThrow(NoAuthorizationException::new);
		
		repositoryResourceService.unstageChanges(repository, param);
		
		return ResponseEntity.ok(Collections.emptyMap());
	}
	
	@PostMapping("/repos/{owner}/{repoName}/commits")
	public ResponseEntity<Map<String, Object>> commit(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("repoName") String repoName,
			@Valid @RequestBody CommitMessage param,
			BindingResult bindingResult) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		
		if(bindingResult.hasErrors()) {
			throw new InvalidRequestException(bindingResult);
		}
		
		Repository repository = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canWrite(principal, repository).orElseThrow(NoAuthorizationException::new);
		
		UserInfo user = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		try {
			repositoryResourceService.commit(user, repository, param.getValue());
			
			String dataRootPath = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH, "");
			// TODO: commit 成功后，启动异步生产源码功能
			// 为每一个 buildTarget 生成源码
			// 获取仓库中所有的项目，从其中找出支持生成源码的项目，为项目的每个 buildTarget 生成源码
			repositoryResourceService.findAllProject(repository.getId()).stream().filter(project -> project.getAppType().equals(AppType.MINI_PROGRAM)).forEach(project -> {
				// 当前仅支持小程序
				// 1. 微信小程序
				MiniProgramStore store = new MiniProgramStore(
						dataRootPath, 
						repository.getCreateUserName(), 
						repository.getName(), 
						project.getKey(),
						BuildTarget.WEAPP,
						Constants.MASTER);
				
				RevCommit latestCommit = GitUtils.getLatestCommit(store.getModelRepositoryDirectory(), project.getKey());
				
				ProjectReleaseTask task = new ProjectReleaseTask();
				task.setRepositoryId(repository.getId());
				task.setProjectId(project.getId());
				task.setVersion(Constants.MASTER);
				task.setTitle("发布微信小程序");
				task.setStartTime(LocalDateTime.now());
				task.setReleaseResult(ReleaseResult.STARTED);
				task.setCreateTime(LocalDateTime.now());
				task.setCreateUserId(user.getId());
				task.setCommitId(latestCommit.getName());
				task.setBuildTarget(BuildTarget.WEAPP);
				
				buildService.asyncBuildProject(repository, project, task, store);
				
				// TODO: 为其他 BuildTarget 生成源码
			});
		} catch(GitEmptyCommitException e) {
			bindingResult.reject("NotEmpty.gitCommit");
		}
		
		if(bindingResult.hasErrors()) {
			throw new InvalidRequestException(bindingResult);
		}
		
		return ResponseEntity.ok(Collections.emptyMap());
	}
}
