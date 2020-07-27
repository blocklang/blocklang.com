package com.blocklang.core.runner.common;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * 贯穿整个工作流程的上下文，用于在 action 间共享信息。
 * 
 * @author Zhengwei Jin
 *
 */
public class DefaultExecutionContext implements ExecutionContext{

	private CliLogger logger;
	private Map<String, Object> data = new HashMap<String, Object>();

	@Override
	public CliLogger getLogger() {
		Assert.notNull(this.logger, "日志记录对象未创建，请先调用 setLogger 设置 logger");
		return this.logger;
	}
	
	@Override
	public void setLogger(CliLogger logger) {
		this.logger = logger;
	}

	@Override
	public String getStringValue(String key) {
		Object result = data.get(key);
		if(result == null) {
			return null;
		}
		return result.toString();
	}

	@Override
	public void putValue(String key, Object value) {
		data.put(key, value);
	}

	@Override
	public <T> T getValue(String key, Class<T> clazz) {
		Object result = data.get(key);
		if(result == null) {
			Assert.notNull(key, "要先设置 " + key + " 参数!");
		}
		return clazz.cast(result);
	}
}
