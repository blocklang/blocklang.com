package com.blocklang.develop.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.exception.InvalidRequestException;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.core.git.exception.GitEmptyCommitException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.develop.data.CommitMessage;
import com.blocklang.develop.data.UncommittedFile;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.service.RepositoryResourceService;

/**
 * 
 * @author Zhengwei Jin
 *
 */
@RestController
public class CommitController extends AbstractRepositoryController{

	@Autowired
	private RepositoryResourceService repositoryResourceService;
	
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
		} catch(GitEmptyCommitException e) {
			bindingResult.reject("NotEmpty.gitCommit");
		}
		
		if(bindingResult.hasErrors()) {
			throw new InvalidRequestException(bindingResult);
		}
		
		return ResponseEntity.ok(Collections.emptyMap());
	}
}
