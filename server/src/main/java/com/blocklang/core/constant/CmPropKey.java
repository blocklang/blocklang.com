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

	/**
	 * 存放 blocklang 运行时生成文件的根目录。
	 * 
	 * <p> <strong>在系统启动时，已校验该参数是否已配置，所以通过 PropertyService 获取时无需再校验。</strong></p>
	 */
	public final static String BLOCKLANG_ROOT_PATH = "blocklang.root.path";
	
	/**
	 * maven 仓库的根目录。
	 * 
	 * <p> <strong>在系统启动时，已校验该参数是否已配置，所以通过 PropertyService 获取时无需再校验。<strong></p>
	 */
	public final static String MAVEN_ROOT_PATH = "maven.root.path";
	
	public final static String TEMPLATE_PROJECT_GIT_URL = "template.project.git.url";

	public static final String INSTALL_API_ROOT_URL = "install.api.root.url";
	
	// git 标准库常量
	public static final String STD_WIDGET_API_GIT_URL = "std.widget.api.git.url";
	public static final String STD_WIDGET_IDE_GIT_URL = "std.widget.ide.git.url";
	public static final String STD_WIDGET_REGISTER_USERID = "std.widget.register.userid";
	public static final String STD_WIDGET_ROOT_NAME = "std.widget.root.name";
	
	// dojo 版的标准库
	public static final String STD_WIDGET_BUILD_DOJO_GIT_URL = "std.widget.build.dojo.git.url";

	// 平台预留的关键字
	public static final String PLATFORM_KEYWORDS = "platform.keywords";
	public static final String PLATFORM_RESERVED_USERNAME = "platform.reserved.username";
}
