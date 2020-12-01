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
	public static final String STD_WIDGET_ROOT_NAME = "std.widget.root.name";
	
	// 各种标准库通用的注册用户标识
	public static final String STD_REPO_REGISTER_USER_ID = "std.repo.register.user.id";
	// 小程序的标准库常量
	public static final String STD_MINI_PROGRAM_COMPONENT_API_GIT_URL = "std.mini.program.component.api.git.url";
	public static final String STD_MINI_PROGRAM_COMPONENT_IDE_GIT_URL = "std.mini.program.component.ide.git.url";
	public static final String STD_MINI_PROGRAM_COMPONENT_PROD_GIT_URL = "std.mini.program.component.prod.git.url";
	public static final String STD_MINI_PROGRAM_COMPONENT_APP_NAME = "std.mini.program.app.name";
	public static final String STD_MINI_PROGRAM_COMPONENT_PAGE_NAME = "std.mini.program.page.name";
	
	// 鸿蒙应用的标准库
	// 1. Lite Wearable
	public static final String STD_HARMONYOS_LITE_WEARABLE_UI_API_GIT_URL = "std.harmonyos.lite.wearable.ui.api.git.url";
	public static final String STD_HARMONYOS_LITE_WEARABLE_UI_IDE_GIT_URL = "std.harmonyos.lite.wearable.ui.ide.git.url";
	public static final String STD_HARMONYOS_LITE_WEARABLE_UI_PROD_GIT_URL = "std.harmonyos.lite.wearable.ui.prod.git.url";
	public static final String STD_HARMONYOS_LITE_WEARABLE_UI_APP_NAME = "std.harmonyos.lite.wearable.app.name";
	public static final String STD_HARMONYOS_LITE_WEARABLE_UI_PAGE_NAME = "std.harmonyos.lite.wearable.page.name";
	
	
	// dojo 版的标准库
	public static final String STD_WIDGET_BUILD_DOJO_GIT_URL = "std.widget.build.dojo.git.url";

	// 平台预留的关键字
	public static final String PLATFORM_KEYWORDS = "platform.keywords";
	public static final String PLATFORM_RESERVED_USERNAME = "platform.reserved.username";
}
