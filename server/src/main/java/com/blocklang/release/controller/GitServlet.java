package com.blocklang.release.controller;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.service.PropertyService;
import com.blocklang.develop.service.RepositoryPermissionService;
import com.blocklang.develop.service.RepositoryResourceService;
import com.blocklang.develop.service.RepositoryService;
import com.blocklang.release.service.ProjectReleaseTaskService;

// git 访问地址为: http[s]://ip[:port]/{owner}/{repo}/{project}/{appType}.git
@WebServlet(name = "gitServer", loadOnStartup = 1, urlPatterns = { "/git/*" }, initParams = {
		@WebInitParam(name = "base-path", value = ""), @WebInitParam(name = "export-all", value = "true") })
public class GitServlet extends org.eclipse.jgit.http.server.GitServlet {

	private static final long serialVersionUID = 7251262335792613657L;

	@Autowired
	private PropertyService propertyService;
	@Autowired
	private RepositoryService respositoryService;
	@Autowired
	private RepositoryResourceService repositoryResourceService;
	@Autowired
	private RepositoryPermissionService repositoryPermissionService;
	@Autowired
	private ProjectReleaseTaskService projectReleaseTaskService;
	private String basePath;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		basePath = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).get();
		var resolver = new GitHttpResolver(basePath, 
				respositoryService, 
				repositoryPermissionService,
				repositoryResourceService, 
				projectReleaseTaskService);
		super.setRepositoryResolver(resolver);
		super.init(config);
	}

}
