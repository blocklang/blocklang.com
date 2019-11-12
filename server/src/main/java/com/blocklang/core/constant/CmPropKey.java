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
	
	// 标准库
	public static final String STD_WIDGET_API_NAME = "std.widget.api.name";
	public static final String STD_WIDGET_IDE_NAME = "std.widget.ide.name";
	public static final String STD_WIDGET_REGISTER_USERID = "std.widget.register.userid";
	public static final String STD_WIDGET_ROOT_NAME = "std.widget.root.name";

	// 平台预留的关键字
	public static final String PLATFORM_KEYWORDS = "platform.keywords";
	public static final String PLATFORM_RESERVED_USERNAME = "platform.reserved.username";
}
