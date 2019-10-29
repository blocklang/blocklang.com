package com.blocklang.core.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LogFileReader {

	private static final Logger logger = LoggerFactory.getLogger(LogFileReader.class);
	
	public static List<String> readAllLines(Path logFilePath) {
		if(logFilePath == null ) {
			logger.warn("传入的文件路径是 null");
			return Collections.emptyList();
		}
		
		if(!logFilePath.toFile().exists()) {
			logger.warn("日志文件 {0} 不存在", logFilePath.toString());
			return Collections.emptyList();
		}
		
		try {
			return Files.readAllLines(logFilePath);
		} catch (IOException e) {
			logger.warn("获取文件内容失败", e);
		}
		
		return Collections.emptyList();
	}
}
