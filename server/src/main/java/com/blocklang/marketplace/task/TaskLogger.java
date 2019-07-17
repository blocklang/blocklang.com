package com.blocklang.marketplace.task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;

import com.blocklang.release.constant.ReleaseResult;

public class TaskLogger {
	
	private static final Logger logger = LoggerFactory.getLogger(TaskLogger.class);
	
	// 本地文件中记录日志
	private Path logFile;
	
	// 发送远程日志
	//
	// 网页控制台是一行一输出
	// 日志格式为 'lineNum:content'
	// lineNum 从 0 开始
	private boolean sendMessage;
	private SimpMessagingTemplate simpMessagingTemplate;
	private Integer taskId;
	
	private Integer lineNum = 0; // 跟踪日志文件的记录行号

	public TaskLogger(Path logFile) {
		this.logFile = logFile;
		
		if(Files.notExists(logFile)) {
			try {
				Files.createDirectories(logFile.getParent());
				Files.createFile(logFile);
			} catch (IOException e) {
				logger.error("can not create log file", e);
			}
		}
		
	}

	public void error(Throwable e) {
		this.writeLine(e.getMessage());
	}
	
	public void error(String pattern, Object... arguments) {
		this.writeLine("[ERROR] " + getContent(pattern, arguments));
	}

	public void info(String pattern, Object... arguments) {
		this.writeLine("[INFO] " + getContent(pattern, arguments));
	}
	
	public void println() {
		this.writeLine("");
	}
	
	/**
	 * 注意：pattern 中不能包含换行符，即使包含，也不会解析
	 * 
	 * 没有 [INFO] [ERROR] 等前缀。
	 * 
	 * @param line
	 */
	public void log(String pattern, Object... arguments) {
		this.writeLine(getContent(pattern, arguments));
	}

	private String getContent(String pattern, Object... arguments) {
		if(arguments.length == 0) {
			return pattern;
		}
		return MessageFormat.format(pattern, arguments);
	}
	
	private void writeLine(String content) {
		try {
			Files.writeString(this.logFile, content + System.lineSeparator(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			logger.error("not found log file", e);
		}
		
		if(sendMessage && taskId != null) {
			this.sendReleaseMessage(lineNum, content);
		}
		
		lineNum++;
	}

	public void setSendMessage(boolean sendMessage) {
		this.sendMessage = sendMessage;
	}

	public void setMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
		this.simpMessagingTemplate = messagingTemplate;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}
	
	private void sendReleaseMessage(long lineNum, String lineContent) {
		Message<String> message = MessageBuilder.withPayload(lineContent)
				.setHeader("lineNum", lineNum)
				.setHeader("event", "console")
				.build();
		sendWsMessage(message);
	}

	public void finished(ReleaseResult releaseResult) {
		if(sendMessage && taskId != null) {
			this.sendFinishMessage(releaseResult);
		}
	}
	
	private void sendFinishMessage(ReleaseResult releaseResult) {
		Message<String> message = MessageBuilder.withPayload("")
				.setHeader("lineNum", lineNum)
				.setHeader("event", "finish")
				.setHeader("releaseResult", releaseResult.getKey())
				.build();
		sendWsMessage(message);
	}

	private void sendWsMessage(Message<String> message) {
		simpMessagingTemplate.convertAndSend("/topic/publish/" + taskId, message);
	}
}
