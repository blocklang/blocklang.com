package com.blocklang.release.task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.Assert;

import com.blocklang.develop.model.ProjectContext;

import de.skuzzle.semantic.Version;

public class AppBuildContext extends ProjectContext{
	
	private static final Logger logger = LoggerFactory.getLogger(AppBuildContext.class);

	private String version;
	
	private String templateProjectGitUrl;
	private String jdkVersion;
	
	private LocalDateTime startLogTime;
	private Path logFilePath;
	
	private SimpMessagingTemplate messagingTemplate;
	private boolean sendMessage = false;
	private Integer taskId;
	
	private Integer lineNum = 0;
	
	protected AppBuildContext() {
		super();
	}

	public AppBuildContext(String dataRootPath, 
			String mavenRootPath, 
			String projectName, 
			String version) {
		super(projectName, dataRootPath);
		
		Assert.hasLength(version, "项目版本号不能为空");
		Assert.hasLength(mavenRootPath, "maven 仓库的根路径不能为空");
		
		this.mavenRootPath = mavenRootPath;
		this.version = version;
	}
	
	public AppBuildContext(String dataRootPath, 
			String mavenRootPath, 
			String templateProjectGitUrl,
			String owner,
			String projectName, 
			String version,
			String description,
			String jdkVersion) {
		this(dataRootPath, mavenRootPath, projectName, version);

		Assert.hasLength(owner, "项目拥有者的登录名不能为空");

		this.owner = owner;
		this.templateProjectGitUrl = templateProjectGitUrl;
		super.description = description;
		this.jdkVersion = jdkVersion;
	}
	
	protected void setVersion(String version) {
		this.version = version;
	}

	public void setMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	public void setSendMessage(boolean sendMessage) {
		this.sendMessage = sendMessage;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	private Path getProjectRootDirectory() {
		return Paths.get(this.dataRootPath, "projects", this.owner, this.projectName);
	}
	
	public Path getClientProjectRootDirectory() {
		return this.getProjectRootDirectory().resolve("client");
	}

	public Path getServerProjectRootDirectory() {
		return this.getProjectRootDirectory().resolve("server");
	}
	
	public Path getMavenPomFile() {
		return this.getServerProjectRootDirectory().resolve("pom.xml");
	}

	public Path getMavenInstallJar() {
		return Paths.get(this.mavenRootPath, 
				"repository", 
				this.getMavenInstallJarRelativePath().toString());
	}
	
	public Path getMavenInstallJarRelativePath() {
		return Paths.get("com", 
				"blocklang", 
				this.owner, 
				this.projectName,
				this.version,
				this.getMavenInstallJarFileName());
	}

	public String getMavenInstallJarFileName() {
		return this.projectName + "-" + this.version + ".jar";
	}

	private Path getLogDirectory() {
		return this.getProjectRootDirectory().resolve("logs");
	}
	
	private String getLogFileName() {
		if(startLogTime == null) {
			startLogTime = LocalDateTime.now();
		}
		return this.projectName + "-" + this.version + "-" + startLogTime.format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")) + ".log";
	}
	
	public Path getLogFilePath() throws IOException {
		if(logFilePath == null) {
			Path logFileDir = this.getLogDirectory();
			Files.createDirectories(logFileDir);

			String logFileName = this.getLogFileName();
			logFilePath = logFileDir.resolve(logFileName);
			if(Files.notExists(logFilePath)) {
				Files.createFile(logFilePath);
			}
		}
		
		return logFilePath;
	}

	public Path getDojoDistDirectory() {
		return this.getClientProjectRootDirectory().resolve("output").resolve("dist");
	}

	private Path getSpringBootMainResourcesDirectory() {
		return this.getServerProjectRootDirectory().resolve("src").resolve("main").resolve("resources");
	}
	
	public Path getSpringBootTemplatesDirectory() {
		return getSpringBootMainResourcesDirectory().resolve("templates");
	}
	
	public Path getSpringBootStaticDirectory() {
		return getSpringBootMainResourcesDirectory().resolve("static");
	}
	
	public String getIndexFileName() {
		return "index.html";
	}

	public String getTemplateProjectGitUrl() {
		return templateProjectGitUrl;
	}

	public String getTagName() {
		if(this.version.startsWith("v")) {
			return this.version;
		}
		return "v" + this.version;
	}
	
	public void raw(String line) {
		try {
			Files.write(getLogFilePath(), Collections.singletonList(line), StandardOpenOption.APPEND);
		} catch (IOException e) {
			logger.error("not found log file", e);
		}
		// 网页控制台是一行一输出
		// 日志格式为 'lineNum:content'
		// lineNum 从 0 开始
		if(sendMessage && taskId != null) {
			messagingTemplate.convertAndSend("/topic/releases/" + taskId, lineNum + ":" + line);
		}
		
		lineNum++;
	}
	
	public void info(String pattern, Object... arguments) {
		String message = null;
		if(arguments.length == 0) {
			message = pattern;
		} else {
			message = MessageFormat.format(pattern, arguments);
		}
		try {
			Files.writeString(getLogFilePath(), "[INFO] " + message + System.lineSeparator(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			logger.error("not found log file", e);
		}
		
		if(sendMessage && taskId != null) {
			messagingTemplate.convertAndSend("/topic/releases/" + taskId, lineNum + ":" + "[INFO] " + message);
		}
		
		lineNum++;
	}
	
	public void log(String pattern, Object... arguments) {
		String message = null;
		if(arguments.length == 0) {
			message = pattern;
		} else {
			message = MessageFormat.format(pattern, arguments);
		}
		try {
			Files.writeString(getLogFilePath(), message + System.lineSeparator(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			logger.error("not found log file", e);
		}
		
		if(sendMessage && taskId != null) {
			messagingTemplate.convertAndSend("/topic/releases/" + taskId, lineNum + ":" + message);
		}
		
		lineNum++;
	}

	public void error(Throwable e) {
		try {
			Files.writeString(getLogFilePath(), e.getMessage() + System.lineSeparator(), StandardOpenOption.APPEND);
		} catch (IOException e1) {
			logger.error("not found log file", e);
		}
		
		if(sendMessage && taskId != null) {
			messagingTemplate.convertAndSend("/topic/releases/" + taskId, lineNum + ":" + e.getMessage());
		}
		
		lineNum++;
	}
	
	public void error(String pattern, Object... arguments) {
		String message = null;
		if(arguments.length == 0) {
			message = pattern;
		} else {
			message = MessageFormat.format(pattern, arguments);
		}
		try {
			Files.writeString(getLogFilePath(), "[ERROR] " + message + System.lineSeparator(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			logger.error("not found log file", e);
		}
		
		if(sendMessage && taskId != null) {
			messagingTemplate.convertAndSend("/topic/releases/" + taskId, lineNum + ":" + "[ERROR] " + message);
		}
		
		lineNum++;
	}

	public void println() {
		try {
			Files.writeString(getLogFilePath(), System.lineSeparator(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			logger.error("not found log file", e);
		}
		
		if(sendMessage && taskId != null) {
			messagingTemplate.convertAndSend("/topic/releases/" + taskId, lineNum + ":" + "");
		}
		
		lineNum++;
	}

	public Path getProjectTemplateDirectory() {
		return Paths.get(this.dataRootPath, "template");
	}

	public Path getProjectTemplateClientDirectory() {
		return this.getProjectTemplateDirectory().resolve("client");
	}

	public Path getProjectTemplateServerDirectory() {
		return this.getProjectTemplateDirectory().resolve("server");
	}

	public String getVersion() {
		return version;
	}
	
	public int getJdkMajorVersion() {
		return Version.parseVersion(this.jdkVersion).getMajor();
	}

	public static class LogPathBuilder {

		private String dataRootPath;
		private String owner;
		private String projectName;
		private String version;
		
		public LogPathBuilder setDataRootPath(String dataRootPath) {
			this.dataRootPath = dataRootPath;
			return this;
		}
		public LogPathBuilder setOwner(String owner) {
			this.owner = owner;
			return this;
		}
		public LogPathBuilder setProjectName(String projectName) {
			this.projectName = projectName;
			return this;
		}
		public LogPathBuilder setVersion(String version) {
			this.version = version;
			return this;
		}
		
		public AppBuildContext build() {
			Assert.hasText(dataRootPath, "存放项目数据的根路径不能为空");
			Assert.hasText(owner, "项目拥有者的登录名不能为空");
			Assert.hasText(projectName, "项目名不能为空");
			Assert.hasText(version, "项目版本号不能为空");
			
			AppBuildContext context = new AppBuildContext();
			context.setDataRootPath(dataRootPath);
			context.setOwner(owner);
			context.setProjectName(projectName);
			context.setVersion(version);
			
			return context;
		}
		
	}
}
