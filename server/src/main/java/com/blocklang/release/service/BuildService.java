package com.blocklang.release.service;

import java.io.IOException;

import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.release.data.MiniProgramStore;
import com.blocklang.release.model.ProjectRelease;
import com.blocklang.release.model.ProjectReleaseTask;

/**
 * 实际构建服务
 * 
 * @author Zhengwei Jin
 *
 */
public interface BuildService {
	
	/**
	 * 构建 block lang 仓库，该方法属于同步方法。
	 * 主要是为了便于测试，由 asyncBuild 方法直接调用。
	 * 
	 * @param repository
	 * @param releaseTask
	 * @throws IOException
	 */
	void build(Repository repository, ProjectReleaseTask releaseTask);

	/**
	 * 构建 block lang 仓库，该方法属于异步方法。
	 * 
	 * @param repository
	 * @param releaseTask
	 */
	void asyncBuild(Repository repository, ProjectReleaseTask releaseTask);

	/**
	 * 构建仓库中的一个项目，如果是小程序或者鸿蒙OS，则只需要生成源代码。
	 * 
	 * @param repository
	 * @param project
	 * @param savedTask
	 * @param store
	 */
	void buildProject(Repository repository, RepositoryResource project, ProjectReleaseTask savedTask, MiniProgramStore store);

	/**
	 * 根据项目模型生成项目源码，为项目支持的每个 buildTarget 生成一遍源码
	 * @param repository 仓库基本信息
	 * @param project 项目基本信息
	 */
	void asyncBuildProject(Repository repository, RepositoryResource project, ProjectReleaseTask releaseTask, MiniProgramStore store);
}
