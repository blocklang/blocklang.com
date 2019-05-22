package com.blocklang.core.filter;

public abstract class Resources {

	public static final String[] PUBLIC_FILE_EXTENSIONS = new String[] {
		"html",
		"js", 
		"css", 
		"map", 
		"woff2", 
		"ttf", 
		"eot", 
		"svg",
		"woff", 
		"json",
		"ico"
	};
	
	/**
	 * 在 dojo route 中登记的 route，这些 url 会显示在浏览器地址栏，然后按下浏览器的 F5 键刷新。
	 * 注意 /users/{login_name}, /projects/{owner}/{project_name} 这两类路由不需要在这里配置，
	 * 因为已经有专门的代码处理了。
	 */
	public static final String[] ROUTES = new String[] {
		"/projects/new",
		"/docs/{fileName}",
		"/settings/profile",
		"/user/completeUserInfo"
	};

	public static final String[] PUBLIC_URL = {
		"/", // 首页
		"/login**", // 用户登录
		"/**/*.js", // static 文件夹中的静态资源
		"/**/*.css",
		"/**/*.ttf",
		"/**/*.svg",
		"/**/*.woff",
		"/**/*.eot",
		"/**/*.woff2",
		"/**/*.json",
		"/**/*.map",
		"/favicon.ico",
		"/index.html",
		"/errors**", // Spring boot 错误页面
		// 自动安装 APP 的服务接口
		"/projects/**",
		"/installers/**",
		"/apps/**",
		// 信息分类编码
		"/properties/**"
	};
	
	/**
	 * 所有的 servlet
	 */
	public static String[] SERVLET_NAMES = {
		// 以下是为 Installer 提供服务的 API
		"installers",                      // APP 安装服务
		"apps",                            // APP 下载服务
		// 以下是本项目内专用的 Controller
		"projects",                        // 项目管理
		"users",                           // 用户管理
		"user",                            // 登录用户信息
		"docs",                            // 帮助文档
		"properties"                       // 信息分类编码
		
//		"/session", // 用户登录
//		"/mocksession", // 开发环境下，模拟用户登录
//		
//		"/users",  // 用户管理
//		"/logout",// 注销用户
//		"/deploy", // 部署
//		"/softwares",// 软件管理
//		"/docs",// 帮助文档
//		"/trace",// 发布 socket
//		"/modules",// 模块
//		"/actuator",// spring 监控
//		"/uiWidgets",// 通用部件
//		"/dismissUnsupportedBrowser",// 删除不支持浏览器的提示
//		"/marketplace", // 淘软件
//		"/upload", //上传
//		"/images", //图片
//		"/app", // bianruanjian.com 自身的项目管理功能
//		"/error", // 错误
//		"/projectResources", // 项目资源
//		"/posts", // 帖子
//		"/comments", // 评论
//		"/services", // 服务
//		"/clientComponents", // 客户端组件
//		"/home", // 主页
//		"/admin", // 管理员
//		"/servicesGo" // 服务调用
	}; 
	
	public static final String WS_RELEASE_CONSOLE = "/release-console";
	public static String[] WS_ENDPOINTS = {
		WS_RELEASE_CONSOLE
	};

}
