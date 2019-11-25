package com.blocklang.marketplace.task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.commons.lang3.SystemUtils;
import org.springframework.util.FileSystemUtils;

import com.blocklang.core.git.GitUtils;

//准备好 componentJson 后，再先构建源代码，如果构建失败，则结束注册流程
//如果是 ide 版、非标准库的组件库，则要构建项目
//该操作只有在完成上述校验之后才有意义
//要先切换到对应的 tag，构建完后，再切换回 master 分支
//构建完后，要移动到指定的文件夹下，然后删除构建生成的所有文件
public class DojoBuildAppGroupTask extends AbstractRepoPublishTask{

	public DojoBuildAppGroupTask(MarketplacePublishContext context) {
		super(context);
	}

	@Override
	public Optional<?> run() {
		boolean success = true;
		
		Path sourceDirectoryPath = context.getLocalComponentRepoPath().getRepoSourceDirectory();
		// 将 source 中的源码切换（git checkout）到指定的 tag 下
		boolean checkoutSuccess = false;
		try {
			logger.info("开始切换到 tag {0}", context.getComponentRepoLatestTagName());
			GitUtils.checkout(sourceDirectoryPath, context.getComponentRepoLatestTagName());
			checkoutSuccess = true;
		}catch(RuntimeException e) {
			success = false;
		}
		if(success) {
			logger.info("切换完成");
		}else {
			logger.error("切换失败");
		}
		
		Path buildDirectoryPath = context.getLocalComponentRepoPath().getRepoBuildDirectory();
		if(success) {
			// 如果 build 文件夹不存在，则创建 build 文件夹
			logger.info("开始往 build 文件夹复制源代码");
			try {
				if(!buildDirectoryPath.toFile().exists()) {
					Files.createDirectory(buildDirectoryPath);
				}
				// 将该 tag 下的 src 文件夹、package.json 和 tsconfig.json 文件复制到 build 文件夹下
				//     如果 build 文件夹下已存在 package.json 和 src 文件夹，则先删除
				//     如果 build 文件夹下已存在 node_modules 文件夹，则保留
				//     如果 build 文件夹下已存在 output 文件夹，则保留
				Path buildSrcDirectoryPath = buildDirectoryPath.resolve("src");
				Path buildPackageJsonPath = buildDirectoryPath.resolve("package.json");
				Path buildTsconfigJsonPath = buildDirectoryPath.resolve("tsconfig.json");
				
				FileSystemUtils.deleteRecursively(buildSrcDirectoryPath);
				Files.deleteIfExists(buildPackageJsonPath);
				Files.deleteIfExists(buildTsconfigJsonPath);
				
				FileSystemUtils.copyRecursively(sourceDirectoryPath.resolve("src"), buildSrcDirectoryPath);
				Files.copy(sourceDirectoryPath.resolve("package.json"), buildPackageJsonPath);
				Files.copy(sourceDirectoryPath.resolve("tsconfig.json"), buildTsconfigJsonPath);
			} catch (IOException e) {
				logger.error(e);
				success = false;
			}
			if(success) {
				logger.info("源代码复制完成");
			} else {
				logger.error("源代码复制失败");
			}
		}
		
		// 不要判断 success，只要上面 git checkout 成功了，这里就要 checkout to master，即使其他构件环节出错。
		if(checkoutSuccess) {
			// 将 source 中的源码切换到 master 分支下
			try {
				logger.info("开始切换回 master 分支");
				GitUtils.checkout(sourceDirectoryPath, "master");
			}catch(RuntimeException e) {
				success = false;
			}
			if(success) {
				logger.info("切换完成");
			}else {
				logger.error("切换失败");
			}
		}
		
		// 在 build 文件夹下
		CliCommand cli = new CliCommand(logger);
		if(success) {
			// 执行 yarn 命令以安装依赖
			// 使用淘宝镜像要快一些
			// yarn config set registry https://registry.npm.taobao.org -g
			logger.info("开始安装依赖，执行 yarn 命令");
			success = cli.run(buildDirectoryPath, SystemUtils.IS_OS_WINDOWS ? "yarn.cmd" : "yarn");
			if(success) {
				logger.info("依赖安装完成");
			}else {
				logger.error("依赖安装失败");
			}
		}
		
		
		if(success) {
			// 执行 npm run build 以构建项目，文件存放在 build/output/dist 文件夹下
			logger.info("开始构建 dojo library，执行 npm run build 命令（必须在 package.json 的 scripts 中配置）");
			success = cli.run(buildDirectoryPath, SystemUtils.IS_OS_WINDOWS ? "npm.cmd" : "npm", "run", "build");
			if(success) {
				logger.info("构建完成");
			}else {
				logger.error("构建失败");
			}
		}
		
		if(success) {
			// 将 build/output/dist 文件夹下的内容复制到 package/{version}/ 文件夹下
			try {
				logger.info("将 build/output/dist/ 文件夹下的内容复制到 package/{0}/ 文件夹下", context.getComponentRepoLatestTagName());
				FileSystemUtils.copyRecursively(buildDirectoryPath.resolve("output").resolve("dist"), context.getLocalComponentRepoPath().getRepoPackageDirectory().resolve(context.getComponentRepoLatestVersion()));
			} catch (IOException e) {
				logger.error(e);
				success = false;
			}
			if(success) {
				logger.info("复制完成");
			}else {
				logger.error("复制失败");
			}
		}
		
		if(success) {
			return Optional.of(true);
		}
		return Optional.empty();
	}
}
