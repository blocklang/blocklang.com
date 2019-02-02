package com.blocklang;

/**
 * 存储应用程序的参数。
 * 
 * TODO: 移除该类，将数据都存到数据库中。
 * 
 * @author Zhengwei Jin
 *
 */
public abstract class Config {

	public static final String BLOCK_LANG_ROOT_PATH = "E:/data/blocklang"; // TODO: 从系统参数中读取
	public static final String MAVEN_ROOT_PATH = "c:/Users/Administrator/.m2";
	// 或 https://gitee.com/blocklang/blocklang-template.git
	public static final String PROJECT_TEMPLATE_GIT_URL = "https://github.com/blocklang/blocklang-template.git";
}
