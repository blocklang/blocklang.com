package com.blocklang.config;

public class Resources {

	public static String[] PUBLIC_URL = {
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
		"/**/*.map",
		"/**/*.map",
		"/**/*.map",
		"/errors**", // Spring boot 错误页面
		// 自动安装 APP 的服务接口
		"/projects/**",
		"/installers/**",
		"/apps/**"
	};
	
}
