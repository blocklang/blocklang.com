package com.blocklang.core.runner.action;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.util.FileSystemUtils;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.AbstractAction;
import com.blocklang.core.runner.common.CliCommand;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.core.runner.common.TaskLogger;
import com.blocklang.marketplace.data.MarketplaceStore;

/**
 * 构建 dojo app 仓库，支持根据指定的 tag 列表或分支，构建多个版本
 * 
 * <pre>
 * inputs
 *     tags     - string[]，要构建的 tag 列表
 *     branches - string[]，要构建的分支列表
 * outputs
 * </pre>
 * 
 * @author 金正伟
 *
 */
public class BuildDojoAppAction extends AbstractAction {
	
	private List<String> tags = new ArrayList<String>();
	private List<String> branches = new ArrayList<String>();
	
	private MarketplaceStore store;
	
	// 只要有一个分支或 tag 构建失败，就退出
	private boolean success = true;

	public BuildDojoAppAction(ExecutionContext context) {
		super(context);
		
		this.store = context.getValue(ExecutionContext.MARKETPLACE_STORE, MarketplaceStore.class);
		List<?> inputTags = context.getValue("tags", List.class);
		this.tags = inputTags == null ? Collections.emptyList() : (List<String>) inputTags;
	}

	@Override
	public Optional<?> run() {
		if(tags.isEmpty() && branches.isEmpty()) {
			logger.info("未发现要构建的 tag 和分支");
			return Optional.of(true);
		}

		// 0. 先校验 tag 是否已构建过
		// 1. 将 git 仓库切换到 tag 或分支下
		// 2. 将源码复制到 build 文件夹
		// 3. 复制完成后切换会 master 分支
		// 4. 在 build 文件夹下
		//    1. 安装依赖
		//    2. build dojo app
		// 5. 将 build 完的内容复制奥 package/{version/branch} 文件夹下
		tags.forEach(tag -> GitUtils.getVersionFromRefName(tag).ifPresentOrElse(buildTag(tag), ignoreBuild(tag)));
		
		// 切换回 master 分支
		
		if(success) {
			return Optional.of(true);
		}
		return Optional.empty();
	}

	private Runnable ignoreBuild(String tag) {
		return () -> {
			logger.error("从 tag {0} 中未解析出语义版本号，忽略 build 此 tag", tag);
		};
	}

	private Consumer<? super String> buildTag(String tag) {
		return version -> {
			logger.info("开始构建 v{0}", version);
			
			// 确认 tag 是否已构建
			Path packageVersionDirectory = store.getPackageVersionDirectory(version);
			if(packageVersionDirectory.resolve("main.bundle.js").toFile().exists()) {
				logger.info("忽略构建 v{0}，因为之前构建完成", version);
				return;
			}
			
			logger.info("开始切换到 tag：{0}", tag);
			Path sourceDirectory = store.getRepoSourceDirectory();
			try {
				GitUtils.checkout(sourceDirectory, tag);
				logger.info("{0} 切换完成", TaskLogger.ANSWER);
			} catch (RuntimeException e) {
				logger.error(e);
				success = false;
				return;
			}
			
			logger.info("开始将源码复制到 build 文件夹中");
			Path buildDirectory = store.getRepoBuildDirectory();
			try {
				if(!buildDirectory.toFile().exists()) {
					Files.createDirectory(buildDirectory);
				}
				// 将该 tag 下的 src 文件夹、package.json 和 tsconfig.json 文件复制到 build 文件夹下
				//     如果 build 文件夹下已存在 package.json 和 src 文件夹，则先删除
				//     如果 build 文件夹下已存在 node_modules 文件夹，则保留
				//     如果 build 文件夹下已存在 output 文件夹，则保留
				Path buildSrcDirectoryPath = buildDirectory.resolve("src");
				Path buildPackageJsonPath = buildDirectory.resolve("package.json");
				Path buildTsconfigJsonPath = buildDirectory.resolve("tsconfig.json");
				
				FileSystemUtils.deleteRecursively(buildSrcDirectoryPath);
				Files.deleteIfExists(buildPackageJsonPath);
				Files.deleteIfExists(buildTsconfigJsonPath);
				
				FileSystemUtils.copyRecursively(sourceDirectory.resolve("src"), buildSrcDirectoryPath);
				Files.copy(sourceDirectory.resolve("package.json"), buildPackageJsonPath);
				Files.copy(sourceDirectory.resolve("tsconfig.json"), buildTsconfigJsonPath);
			} catch (IOException e) {
				logger.error(e);
				success = false;
				return;
			}
			
			logger.info("开始 build dojo app");
			CliCommand cli = getCliCommand();
			// 执行 yarn 命令以安装依赖
			// 使用淘宝镜像要快一些
			// yarn config set registry https://registry.npm.taobao.org -g
			logger.info("开始安装依赖，执行 yarn 命令");
			if(!cli.run(buildDirectory,  "yarn")) {
				// 此处不需要打印日志，因为 CliCommand 中已经打印错误日志
				success = false;
				return;
			}
			
			// 执行 npm run build 以构建项目，文件存放在 build/output/dist 文件夹下
			logger.info("开始 build dojo app，执行 npm run build 命令（必须在 package.json 的 scripts 中配置）");
			if(!cli.run(buildDirectory, "npm", "run", "build")) {
				success = false;
				return;
			}
			
			// 将 build/output/dist 文件夹下的内容复制到 package/{version}/ 文件夹下
			logger.info("将 build/output/dist/ 文件夹下的内容复制到 package/{0}/ 文件夹下", version);
			try {
				FileSystemUtils.copyRecursively(buildDirectory.resolve("output").resolve("dist"), packageVersionDirectory);
				logger.info("{0} 复制完成", CliLogger.ANSWER);
			} catch (IOException e) {
				logger.error(e);
				success = false;
			}
		};
	}

	// 将此单独提出，是为了使得可以测试 run 方法，因为这样就可以 spy 该方法，
	// 而不用关注 process 的处理细节。
	protected CliCommand getCliCommand() {
		return new CliCommand(logger);
	}

}
