package com.blocklang.release.controller;

import java.io.IOException;
import java.nio.file.Path;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ServiceMayNotContinueException;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;

import com.blocklang.develop.constant.BuildTarget;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.RepositoryPermissionService;
import com.blocklang.develop.service.RepositoryResourceService;
import com.blocklang.develop.service.RepositoryService;
import com.blocklang.release.data.MiniProgramStore;
import com.blocklang.release.service.ProjectReleaseTaskService;

public class GitHttpResolver implements RepositoryResolver<HttpServletRequest> {

	private String basePath;
	private RepositoryService repositoryService;
	private RepositoryPermissionService repositoryPermissionService;
	private ProjectReleaseTaskService projectReleaseTaskService;
	private RepositoryResourceService repositoryResourceService;
	
	public GitHttpResolver(String basePath, 
			RepositoryService repositoryService, 
			RepositoryPermissionService repositoryPermissionService,
			RepositoryResourceService repositoryResourceService,
			ProjectReleaseTaskService projectReleaseTaskService) {
		this.basePath = basePath;
		this.repositoryService = repositoryService;
		this.repositoryPermissionService = repositoryPermissionService;
		this.repositoryResourceService = repositoryResourceService;
		this.projectReleaseTaskService = projectReleaseTaskService;
	}

	@Override
	public Repository open(HttpServletRequest req, String name) throws RepositoryNotFoundException,
			ServiceNotAuthorizedException, ServiceNotEnabledException, ServiceMayNotContinueException {
		// 如果项目正在构建，则返回提示信息
		// 如果不是最新的源码，则开始构建项目
		// 否则开始下载源码
		// TODO: 在下载之前，先确保生成最新版本的源码
		System.out.println("service: " + basePath);
		
		// http[s]://ip[:port]/{owner}/{repo}/{project}/{appType}.git
		String[] arrNames = name.split("/");
		if(arrNames.length != 4 || !name.endsWith(".git")) {
			throw new RepositoryNotFoundException(name);
		}
		String owner = arrNames[0];
		String repo = arrNames[1];
		String projectName = arrNames[2];
		String buildTarget = arrNames[3].substring(0, arrNames[3].length() - ".git".length());
		
		com.blocklang.develop.model.Repository repository = repositoryService.find(owner, repo).orElseThrow(() -> new RepositoryNotFoundException(name));
		if(!repository.getIsPublic()) {
			// 当前仅支持开放仓库
			throw new ServiceNotEnabledException("现在只支持公开仓库，不支持私有仓库。");
		}
		// TODO: 如果是公开项目，不需要权限认证；如果是私有项目，则需要用户登录
//		if(repositoryPermissionService.canRead(SecurityUtil.getLoginUser(), repository).isEmpty()) {
//			
//		}
		RepositoryResource project = repositoryResourceService.findProject(repository.getId(), projectName).orElseThrow(() -> new RepositoryNotFoundException(name));
		if(projectReleaseTaskService.isBuilding(project.getId(), "master")) {
			throw new ServiceNotEnabledException("正在构建 master 分支，稍后再试！");
		}
		
		MiniProgramStore store = new MiniProgramStore(basePath, owner, repo, projectName, BuildTarget.fromKey(buildTarget), "master");
		// 不是裸仓库，需要指向 .git 文件夹
		// 当前只支持客户端程序
		Path gitPath = store.getProjectSourceDirectory();
		try {
			return new FileRepository(gitPath.toFile());
		} catch (IOException e) {
			throw new RepositoryNotFoundException(name, e);
		}
	}

}
