package com.blocklang.release.service;

import java.io.IOException;

import com.blocklang.develop.model.Project;
import com.blocklang.release.model.ProjectReleaseTask;

/**
 * 实际构建服务
 * 
 * @author Zhengwei Jin
 *
 */
public interface BuildService {
	
	/**
	 * 构建 block lang 项目，该方法属于同步方法。
	 * 主要是为了便于测试，由 asyncBuild 方法直接调用。
	 * 
	 * @param project
	 * @param releaseTask
	 * @throws IOException
	 */
	void build(Project project, ProjectReleaseTask releaseTask);

	/**
	 * 构建 block lang 项目，该方法属于异步方法。
	 * 
	 * @param project
	 * @param releaseTask
	 */
	void asyncBuild(Project project, ProjectReleaseTask releaseTask);
	
}
