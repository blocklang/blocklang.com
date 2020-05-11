package com.blocklang.core.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.blocklang.core.runner.CliLogger;
import com.blocklang.core.runner.TaskLogger;
import com.blocklang.release.constant.ReleaseResult;

public class TaskLoggerTest{

	@Captor 
	ArgumentCaptor<Message<String>> messageCaptor;
	
	private Path logFolder;
	private Path logFile;
	
	@BeforeEach
	private void setUp(@TempDir Path tempFolder) throws IOException {
		logFolder = Files.createDirectories(tempFolder.resolve("folder"));
		logFile = Files.createFile(tempFolder.resolve("log.txt"));
	}
	
	@Test
	public void info_no_arguments() throws IOException {
		CliLogger logger = new TaskLogger(logFile);
		
		logger.info("Hello Block Lang");
		
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo("[INFO] Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void info_log_file_not_exist() throws IOException {
		Path logFile = logFolder.resolve("folder1").resolve("folder2").resolve("a.log");
		CliLogger logger = new TaskLogger(logFile);
		
		logger.info("Hello Block Lang");
		
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo("[INFO] Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void info_with_arguments() throws IOException {
		CliLogger logger = new TaskLogger(logFile);
		
		logger.info("Hello {0}", "Block Lang");
		
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo("[INFO] Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void error_throwable() throws IOException {
		CliLogger logger = new TaskLogger(logFile);
		
		Throwable throwable = new Exception("throw a exception");
		logger.error(throwable);
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo(throwable.toString() + System.lineSeparator());
	}
	
	@Test
	public void error_no_arguments() throws IOException {
		CliLogger logger = new TaskLogger(logFile);
		
		logger.error("Hello Block Lang");
		
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo("[ERROR] Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void error_with_arguments() throws IOException {
		CliLogger logger = new TaskLogger(logFile);
		
		logger.error("Hello {0}", "Block Lang");
		
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo("[ERROR] Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void log_no_arguments() throws IOException {
		CliLogger logger = new TaskLogger(logFile);
		
		logger.log("Hello Block Lang");
		
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo("Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void log_with_arguments() throws IOException {
		CliLogger logger = new TaskLogger(logFile);
		
		logger.log("Hello {0}", "Block Lang");
		
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo("Hello Block Lang" + System.lineSeparator());
	}
	
	@Test
	public void newLine() throws IOException {
		CliLogger logger = new TaskLogger(logFile);
		
		logger.newLine();
		
		String content = Files.readString(logFile);
		assertThat(content).isEqualTo(System.lineSeparator());
	}

	@Test
	public void enableSendStompMessage() {
		MockitoAnnotations.initMocks(this);
		
		CliLogger logger = new TaskLogger(logFile);
		SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
		
		logger.enableSendStompMessage(1, messagingTemplate, "/topic/publish/");
		String message = "abc";
		logger.log(message);
		verify(messagingTemplate).convertAndSend(eq("/topic/publish/1"), messageCaptor.capture());
	}
	
	@Test
	public void finished_enable_send_stop_message() throws IOException {
		MockitoAnnotations.initMocks(this);
		
		CliLogger logger = new TaskLogger(logFile);
		SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
		
		logger.enableSendStompMessage(1, messagingTemplate, "/topic/publish/");
		logger.finished(ReleaseResult.PASSED);
		
		// payload 参数只能使用 messageCaptor.capture() 代替，不能使用 any()，因为此参数是一个泛型对象
		verify(messagingTemplate).convertAndSend(eq("/topic/publish/1"), messageCaptor.capture());
		
		String content = Files.readString(logFile);
		assertThat(content).isEmpty();
	}
	
	@Test
	public void finished_disable_send_stop_message() throws IOException {
		MockitoAnnotations.initMocks(this);
		
		CliLogger logger = new TaskLogger(logFile);
		logger.finished(ReleaseResult.PASSED);
		
		String content = Files.readString(logFile);
		assertThat(content).isEmpty();
	}
}
