package com.blocklang.release.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ProjectStoreTest {

	private ProjectStore store;
	private String rootDataPath;
	
	@BeforeEach
	public void setUp(@TempDir Path rootDir) {
		String dataRootPath = rootDir.toString();
		String owner = "jack";
		String repositoryName = "repo1";
		String projectName = "project1";
		String version = "master";
		
		
		this.store = new ProjectStore(dataRootPath, owner, repositoryName, projectName, version);
		this.rootDataPath = rootDir.toString();
	}
	
	@Test
	public void getLogFilePathNoGitShortCommitId() {
		Path logFilePath = store.getLogFilePath(null);
		
		assertThat(logFilePath.getParent().compareTo(Paths.get(this.rootDataPath, "projects/jack/repo1/project1/buildLogs"))).isEqualTo(0);
		
		String logFileName = logFilePath.getFileName().toString();
		assertThat(logFileName).startsWith("master-");
		// 如果是第一次构建，则 git short commit id 的值为 0000000
		assertThat(logFileName).endsWith("-" + StringUtils.repeat('0', 7) + ".log");
	}
	
	@Test
	public void getLogFilePathHasGitShortCommitId(@TempDir Path rootDir) {
		Path logFilePath = store.getLogFilePath("abcdefg");
		String logFileName = logFilePath.getFileName().toString();
		assertThat(logFileName).startsWith("master-");
		assertThat(logFileName).endsWith("-abcdefg.log");
	}
	
	@Test
	public void getLogFilePathTwice() throws InterruptedException {
		Path first = store.getLogFilePath(null);
		TimeUnit.SECONDS.sleep(1);
		Path second = store.getLogFilePath(null);
		assertThat(first).isEqualTo(second);
	}
	
	@Test
	public void getLogFilePathNotExists() {
		Path logFilePath = store.getLogFilePath(null);
		assertThat(logFilePath).doesNotExist();
	}
}
