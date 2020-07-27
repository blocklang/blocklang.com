package com.blocklang.core.runner.common;

public interface ExecutionContext {

	// 将全局且必须在初始化上下文时要创建的变量名存在此处
	// Action 中获取值有两种方式，一是 steps 中 action 之间传值
	// 另一种是从全局上下文中获取，而 marketplaceStore 和 publishTask 就属于全局上下文
	public static final String MARKETPLACE_STORE = "marketplaceStore";
	public static final String PUBLISH_TASK = "publishTask";
	public static final String DATA_ROOT_PATH = "dataRootPath";
	// 如果 gitUrl 有值，则优先使用此值；否则使用 publishTask 中的 gitUrl
	public static final String GIT_URL = "gitUrl";

	void setLogger(CliLogger logger);
	
	/**
	 * 获取日志记录对象，在调用此方法前，需要先调用 {@link #newLogger(SimpMessagingTemplate, String)} 创建日志记录对象。
	 * 
	 * @return 日志记录对象
	 */
	CliLogger getLogger();

	void putValue(String key, Object value);
	
	String getStringValue(String key);

	<T> T getValue(String key, Class<T> clazz);
}
