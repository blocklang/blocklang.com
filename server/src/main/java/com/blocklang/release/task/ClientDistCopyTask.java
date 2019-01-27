package com.blocklang.release.task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientDistCopyTask extends AbstractTask{

	private static final Logger logger = LoggerFactory.getLogger(ClientDistCopyTask.class);
	
	public ClientDistCopyTask(AppBuildContext appBuildContext) {
		super(appBuildContext);
	}

	/**
	 * 
	 * 
	 * @return 复制成功，返回 <code>true</code>，否则返回 <code>false</code>。
	 */
	@Override
	public boolean run() {
		logger.info("开始将 dojo dist 文件夹复制到 spring boot 的 static 文件夹中");
		Path dojoDistDirectory = appBuildContext.getDojoDistDirectory();
		Path springBootStaticDirectory = appBuildContext.getSpringBootStaticDirectory();
		Path springBootTemplateDirectory = appBuildContext.getSpringBootTemplatesDirectory();
		
		try {
			// 将 client/output/dist 文件夹复制到 server/src/main/resources/static
			Files.copy(dojoDistDirectory, springBootStaticDirectory, StandardCopyOption.REPLACE_EXISTING);
			// 将 server/src/main/resources/static/index.html 移动到 server/src/main/resources/templates/index.html 
			String indexFileName = appBuildContext.getIndexFileName();
			Files.move(springBootStaticDirectory.resolve(indexFileName), springBootTemplateDirectory.resolve(indexFileName), StandardCopyOption.REPLACE_EXISTING);
			return true;
		} catch (IOException e) {
			logger.error("复制出错", e);
		}
		return false;
	}
	
}
