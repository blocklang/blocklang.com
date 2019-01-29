package com.blocklang.release.service;

import java.util.Optional;

import com.blocklang.release.task.AppBuildContext;

/**
 * 简单封装 git 操作
 * 
 * @author Zhengwei Jin
 *
 */
public interface GitToolService {

	/**
	 * 为项目的 git 仓库添加标签
	 * 
	 * @param context 构建上下文
	 * @return 如果添加标签失败返回空对象，如果添加标签成功返回 git 标签标识
	 */
	Optional<String> tag(AppBuildContext context);

}
