package com.blocklang.core.constant;

/**
 * 系统属性名称常量类。
 * 
 * 约定 key 的值一律用英文句号隔开，不要使用下划线等符号。
 * 
 * @author Zhengwei Jin
 *
 */
public abstract class CmPropKey {

	public final static String BLOCKLANG_ROOT_PATH = "blocklang.root.path";
	
	public final static String MAVEN_ROOT_PATH = "maven.root.path";
	
	public final static String TEMPLATE_PROJECT_GIT_URL = "template.project.git.url";

	public static final String INSTALL_API_ROOT_URL = "install.api.root.url";

	public static final String INSTALLER_LINUX_URL = "installer.linux.url";

	public static final String INSTALLER_WINDOWS_URL = "installer.windows.url";
}
