package com.blocklang.release.service;

import com.blocklang.release.task.AppBuildContext;

/**
 * 简单封装 npm install，maven install 和 dojo build 等 build 相关操作
 * 
 * TODO: 将这些单个的服务最终合并成一个 build 服务？
 * 
 * @author Zhengwei Jin
 *
 */
public interface BuildToolService {

	/**
	 * 运行 npm install 命令
	 * 
	 * @param appBuildContext
	 * @return 执行成功返回 <code>true</code>，执行失败返回<code>false</code>
	 */
	boolean runNpmInstall(AppBuildContext appBuildContext);

	boolean runDojoBuild(AppBuildContext appBuildContext);

	boolean copyDojoDistToSpringBoot(AppBuildContext appBuildContext);

	boolean runMavenInstall(AppBuildContext appBuildContext);

}
