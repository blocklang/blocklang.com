package com.blocklang.release.task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.springframework.util.FileSystemUtils;

public class ClientDistCopyTask extends AbstractTask{

	public ClientDistCopyTask(AppBuildContext appBuildContext) {
		super(appBuildContext);
	}

	/**
	 * 将 build 后的 client 端代码复制到 spring boot 对应的文件夹中。
	 * 
	 * @return 复制成功，返回 <code>true</code>，否则返回 <code>false</code>。
	 */
	@Override
	public Optional<Boolean> run() {
		Path dojoDistDirectory = appBuildContext.getDojoDistDirectory();
		Path springBootStaticDirectory = appBuildContext.getSpringBootStaticDirectory();
		Path springBootTemplateDirectory = appBuildContext.getSpringBootTemplatesDirectory();
		
		try {
			// 将 client/output/dist 文件夹复制到 server/src/main/resources/static
			FileSystemUtils.deleteRecursively(springBootStaticDirectory);
			FileSystemUtils.copyRecursively(dojoDistDirectory, springBootStaticDirectory);
			
			// 将 server/src/main/resources/static/index.html 移动到 server/src/main/resources/templates/index.html 
			String indexFileName = appBuildContext.getIndexFileName();
			Files.move(springBootStaticDirectory.resolve(indexFileName), springBootTemplateDirectory.resolve(indexFileName), StandardCopyOption.REPLACE_EXISTING);
			return Optional.of(true);
		} catch (IOException e) {
			appBuildContext.error(e);
		}
		return Optional.empty();
	}
	
}
