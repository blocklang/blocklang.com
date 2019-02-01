package com.blocklang.release.task;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.springframework.util.FileSystemUtils;

public class ProjectTemplateCopyTask extends AbstractTask{

	public ProjectTemplateCopyTask(AppBuildContext appBuildContext) {
		super(appBuildContext);
	}

	// 注意，不能删除 logs 文件夹
	@Override
	public Optional<Boolean> run() {
		try {
			// 复制 client 文件夹
			Path fromClient = appBuildContext.getProjectTemplateClientDirectory();
			Path toClient = appBuildContext.getClientProjectRootDirectory();
			
			FileSystemUtils.deleteRecursively(toClient);
			FileSystemUtils.copyRecursively(fromClient, toClient);
			
			// 复制 server 文件夹
			Path fromServer = appBuildContext.getProjectTemplateServerDirectory();
			Path toServer = appBuildContext.getServerProjectRootDirectory();
			
			FileSystemUtils.deleteRecursively(toServer);
			FileSystemUtils.copyRecursively(fromServer, toServer);
			
			return Optional.of(true);
		} catch (IOException e) {
			appBuildContext.error(e);
		}
		
		return Optional.empty();
	}

}
