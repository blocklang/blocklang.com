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
 *     master   - boolean，是否构建 master 分支，默认为 true
 * outputs
 * </pre>
 * 
 * @author 金正伟
 *
 */
public class BuildDojoAppAction extends AbstractAction {
	
	private List<String> tags = new ArrayList<String>();
	private Boolean master = true;
	
	private MarketplaceStore store;

	// 只要有一个分支或 tag 构建失败，就退出
	private boolean success = true;

	public BuildDojoAppAction(ExecutionContext context) {
		super(context);
		
		this.store = context.getValue(ExecutionContext.MARKETPLACE_STORE, MarketplaceStore.class);
		List<?> inputTags = context.getValue("tags", List.class);
		this.tags = inputTags == null ? Collections.emptyList() : (List<String>) inputTags;
		Boolean inputMaster = context.getValue("master", Boolean.class);
		if(inputMaster != null) {
			master = inputMaster;
		}
	}

	/**
	 * <pre>
	 * 1. 先校验 tag 是否已构建过
	 * 2. 将 git 仓库切换到 tag 或分支下
	 * 3. 将源码复制到 build 文件夹
	 * 4. 复制完成后切换会 master 分支
	 * 5. 在 build 文件夹下
	 *     1. 安装依赖
	 *     2. build dojo app
	 * 6. 将 build 完的内容复制奥 package/{version/branch} 文件夹下
	 * </pre>
	 */
	@Override
	public Optional<?> run() {
		if(tags.isEmpty()) {
			logger.info("未发现要构建的 tag 和分支");
		} else {
			tags.forEach(tag -> GitUtils.getVersionFromRefName(tag).ifPresentOrElse(buildTag(tag), ignoreBuild(tag)));
		}
		
		// 因为此处是必须要构建 master 分支，且只构建 master 分支，所以不需要再从外部传入 branches
		// 因为 master 分支存的是最新代码，所以即使在 package/master 已存在构建版，也要重新构建
		// TODO: 优化，存储 master 分支对应的 commit 标识，如果 commit 标识没有变化，则不要重新发布
		// 构建 master 分支
		// 因为最后 build 的是 master 分支，自然要切换回 master 分支，所以不再单独处理，如果此逻辑有变化，则切记最后要切换回 master 分支
		if(master) {
			buildMaster();
		}
		
		ensureCheckoutMaster();
		
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
			try {
				GitUtils.checkout(store.getRepoSourceDirectory(), tag);
				logger.info("{0} 切换完成", TaskLogger.ANSWER);
			} catch (RuntimeException e) {
				logger.error(e);
				success = false;
				return;
			}
			
			try {
				copySourceToBuildDirectory();
			} catch (IOException e) {
				logger.error(e);
				success = false;
				return;
			}
			
			if(!buildDojoApp()) {
				success = false;
				return;
			}
			
			// 将 build/output/dist 文件夹下的内容复制到 package/{version}/ 文件夹下
			logger.info("将 build/output/dist/ 文件夹下的内容复制到 package/{0}/ 文件夹下", version);
			try {
				FileSystemUtils.copyRecursively(store.getRepoBuildDirectory().resolve("output").resolve("dist"), packageVersionDirectory);
				logger.info("{0} 复制完成", CliLogger.ANSWER);
			} catch (IOException e) {
				logger.error(e);
				success = false;
			}
		};
	}

	private void buildMaster() {
		logger.info("开始构建 master 分支");
		
		// 如果当前不在 master 分支下，则先切换到 master 分支
		try {
			Path sourceDirectory = store.getRepoSourceDirectory();
			GitUtils.checkout(sourceDirectory, "master");
		} catch (RuntimeException e) {
			logger.error(e);
			success = false;
			return;
		}
		
		try {
			copySourceToBuildDirectory();
		} catch (IOException e) {
			logger.error(e);
			success = false;
			return;
		}
		
		if(!buildDojoApp()) {
			success = false;
			return;
		}
		
		// master 分支必须重新 build
		// 将 build/output/dist 文件夹下的内容复制到 package/master/ 文件夹下
		// 如果 master 文件夹已存在，则先删除 master 文件夹
		String version = "master";
		logger.info("将 build/output/dist/ 文件夹下的内容复制到 package/{0}/ 文件夹下", version);
		try {
			Path packageMasterDirectory = store.getPackageVersionDirectory(version);
			FileSystemUtils.deleteRecursively(packageMasterDirectory);
			FileSystemUtils.copyRecursively(store.getRepoBuildDirectory().resolve("output").resolve("dist"), packageMasterDirectory);
			logger.info("{0} 复制完成", CliLogger.ANSWER);
		} catch (IOException e) {
			logger.error(e);
			success = false;
		}
	}

	private boolean buildDojoApp() {
		// 执行 yarn 命令以安装依赖
		// 使用淘宝镜像要快一些
		// yarn config set registry https://registry.npm.taobao.org -g
		
		Path buildDirectory = store.getRepoBuildDirectory();
		logger.info("开始 build dojo app");
		CliCommand cli = getCliCommand();
		logger.info("开始运行 yarn 命令安装依赖");
		if(!cli.run(buildDirectory, "yarn")) {
			// 此处不需要打印日志，因为 CliCommand 中已经打印错误日志
			return false;
		}
		
		logger.info("开始运行 npm run build 构建项目（必须在 package.json 中配置 build script）");
		if(!cli.run(buildDirectory, "npm", "run", "build")) {
			// 此处不需要打印日志，因为 CliCommand 中已经打印错误日志
			return false;
		}
		
		return true;
	}
	
	private void ensureCheckoutMaster() {
		try {
			Path sourceDirectory = store.getRepoSourceDirectory();
			GitUtils.checkout(sourceDirectory, "master");
		} catch (RuntimeException e) {
			logger.error(e);
		}
	}

	/**
	 *  将该 tag 下的 src 文件夹、package.json 和 tsconfig.json 文件复制到 build 文件夹下
	 *  
	 *  <ul>
	 *  <li>如果 build 文件夹下已存在 package.json 和 src 文件夹，则先删除
	 *  <li>如果 build 文件夹下已存在 node_modules 文件夹，则保留
	 *  <li>如果 build 文件夹下已存在 output 文件夹，则保留
	 * </ul>
	 * 
	 * @throws IOException
	 */
	private void copySourceToBuildDirectory() throws IOException {
		logger.info("开始将源码复制到 build 文件夹中");
		
		Path buildDirectory = store.getRepoBuildDirectory();
		if(!buildDirectory.toFile().exists()) {
			Files.createDirectory(buildDirectory);
		}
		
		Path buildSrcDirectoryPath = buildDirectory.resolve("src");
		Path buildPackageJsonPath = buildDirectory.resolve("package.json");
		Path buildTsconfigJsonPath = buildDirectory.resolve("tsconfig.json");
		
		FileSystemUtils.deleteRecursively(buildSrcDirectoryPath);
		Files.deleteIfExists(buildPackageJsonPath);
		Files.deleteIfExists(buildTsconfigJsonPath);
		
		Path sourceDirectory = store.getRepoSourceDirectory();
		FileSystemUtils.copyRecursively(sourceDirectory.resolve("src"), buildSrcDirectoryPath);
		Files.copy(sourceDirectory.resolve("package.json"), buildPackageJsonPath);
		Files.copy(sourceDirectory.resolve("tsconfig.json"), buildTsconfigJsonPath);
	}
	
	// 将此单独提出，是为了使得可以测试 run 方法，因为这样就可以 spy 该方法，
	// 而不用关注 process 的处理细节。
	protected CliCommand getCliCommand() {
		return new CliCommand(logger);
	}

}
