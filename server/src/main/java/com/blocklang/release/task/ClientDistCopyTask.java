package com.blocklang.release.task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class ClientDistCopyTask extends AbstractTask{

	public ClientDistCopyTask(AppBuildContext appBuildContext) {
		super(appBuildContext);
	}

	/**
	 * 
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
			Files.copy(dojoDistDirectory, springBootStaticDirectory, StandardCopyOption.REPLACE_EXISTING);
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
