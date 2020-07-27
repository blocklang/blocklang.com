package com.blocklang.core.runner.common;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.blocklang.release.constant.ReleaseResult;

/**
 * 记录任务中的日志。
 * 
 * <p>默认只往日志文件中写日志，如果需要开启往浏览器端发送日志，则调用 {@link TaskLogger#enableSendStompMessage(Integer, SimpMessagingTemplate)}}
 * 
 * @author Zhengwei Jin
 *
 */
public interface CliLogger {
	
	public static final String ANSWER = "↳";
	
	/**
	 * 注意：pattern 中不能包含换行符，即使包含，也不会解析
	 * 
	 * 没有 [INFO] [ERROR] 等前缀。
	 * 
	 */
	void log(String content);
	
	/**
	 * 注意：pattern 中不能包含换行符，即使包含，也不会解析
	 * 
	 * 没有 [INFO] [ERROR] 等前缀。
	 * 
	 */
	void log(String pattern, Object...arguments);
	
	void info(String pattern, Object... arguments);

	void error(Throwable e);

	void error(String pattern, Object... arguments);
	
	/**
	 * 换行
	 */
	void newLine();
	
	/**
	 * 开启发送 stomp 日志功能
	 * 
	 * @param taskId               任务标识
	 * @param messagingTemplate    消息模板
	 * @param destinationPrefix    发送目标的前缀，以/开头，并以/结束，如 "/topic/dosth/"
	 */
	void enableSendStompMessage(Integer taskId, SimpMessagingTemplate messagingTemplate, String destinationPrefix);
	
	void finished(ReleaseResult releaseResult);
}
