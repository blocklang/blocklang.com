package com.blocklang.core.runner.common;

public interface ExecutionContext {

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
