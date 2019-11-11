package com.blocklang.marketplace.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TaskLoggerTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Test
	public void info_no_arguments() throws IOException {
		File logFile = tempFolder.newFile();
		TaskLogger logger = new TaskLogger(logFile.toPath());
		
		logger.info("Hello Block Lang");
		
		String content = Files.readString(logFile.toPath());
		assertThat(content).isEqualTo("[INFO] Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void info_log_file_not_exist() throws IOException {
		File logFolder = tempFolder.newFolder();
		Path logFile = logFolder.toPath().resolve("folder1").resolve("folder2").resolve("a.log");
		TaskLogger logger = new TaskLogger(logFile);
		logger.info("Hello Block Lang");
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo("[INFO] Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void info_with_arguments() throws IOException {
		File logFile = tempFolder.newFile();
		TaskLogger logger = new TaskLogger(logFile.toPath());
		
		logger.info("Hello {0}", "Block Lang");
		
		String content = Files.readString(logFile.toPath());
		assertThat(content).isEqualTo("[INFO] Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void error_throwable() throws IOException {
		File logFile = tempFolder.newFile();
		TaskLogger logger = new TaskLogger(logFile.toPath());
		
		Throwable throwable = new Exception("throw a exception");
		logger.error(throwable);
		String content = Files.readString(logFile.toPath());
		assertThat(content).isEqualTo(throwable.toString() + System.lineSeparator());
	}
	
	@Test
	public void error_no_arguments() throws IOException {
		File logFile = tempFolder.newFile();
		TaskLogger logger = new TaskLogger(logFile.toPath());
		
		logger.error("Hello Block Lang");
		
		String content = Files.readString(logFile.toPath());
		assertThat(content).isEqualTo("[ERROR] Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void error_with_arguments() throws IOException {
		File logFile = tempFolder.newFile();
		TaskLogger logger = new TaskLogger(logFile.toPath());
		
		logger.error("Hello {0}", "Block Lang");
		
		String content = Files.readString(logFile.toPath());
		assertThat(content).isEqualTo("[ERROR] Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void log_no_arguments() throws IOException {
		File logFile = tempFolder.newFile();
		TaskLogger logger = new TaskLogger(logFile.toPath());
		
		logger.log("Hello Block Lang");
		
		String content = Files.readString(logFile.toPath());
		assertThat(content).isEqualTo("Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void log_with_arguments() throws IOException {
		File logFile = tempFolder.newFile();
		TaskLogger logger = new TaskLogger(logFile.toPath());
		
		logger.log("Hello {0}", "Block Lang");
		
		String content = Files.readString(logFile.toPath());
		assertThat(content).isEqualTo("Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void println() throws IOException {
		File logFile = tempFolder.newFile();
		TaskLogger logger = new TaskLogger(logFile.toPath());
		
		logger.println();
		
		String content = Files.readString(logFile.toPath());
		assertThat(content).isEqualTo( System.lineSeparator());
	}
}
