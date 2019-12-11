package com.blocklang.marketplace.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TaskLoggerTest {
	
	private Path logFolder;
	private Path logFile;
	
	@BeforeEach
	private void setUp(@TempDir Path tempFolder) throws IOException {
		logFolder = Files.createDirectories(tempFolder.resolve("folder"));
		logFile = Files.createFile(tempFolder.resolve("log.txt"));
	}
	
	@Test
	public void info_no_arguments() throws IOException {
		TaskLogger logger = new TaskLogger(logFile);
		
		logger.info("Hello Block Lang");
		
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo("[INFO] Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void info_log_file_not_exist() throws IOException {
		Path logFile = logFolder.resolve("folder1").resolve("folder2").resolve("a.log");
		TaskLogger logger = new TaskLogger(logFile);
		logger.info("Hello Block Lang");
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo("[INFO] Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void info_with_arguments() throws IOException {
		TaskLogger logger = new TaskLogger(logFile);
		
		logger.info("Hello {0}", "Block Lang");
		
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo("[INFO] Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void error_throwable() throws IOException {
		TaskLogger logger = new TaskLogger(logFile);
		
		Throwable throwable = new Exception("throw a exception");
		logger.error(throwable);
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo(throwable.toString() + System.lineSeparator());
	}
	
	@Test
	public void error_no_arguments() throws IOException {
		TaskLogger logger = new TaskLogger(logFile);
		
		logger.error("Hello Block Lang");
		
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo("[ERROR] Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void error_with_arguments() throws IOException {
		TaskLogger logger = new TaskLogger(logFile);
		
		logger.error("Hello {0}", "Block Lang");
		
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo("[ERROR] Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void log_no_arguments() throws IOException {
		TaskLogger logger = new TaskLogger(logFile);
		
		logger.log("Hello Block Lang");
		
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo("Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void log_with_arguments() throws IOException {
		TaskLogger logger = new TaskLogger(logFile);
		
		logger.log("Hello {0}", "Block Lang");
		
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo("Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void println() throws IOException {
		TaskLogger logger = new TaskLogger(logFile);
		
		logger.println();
		
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo( System.lineSeparator());
	}
}
