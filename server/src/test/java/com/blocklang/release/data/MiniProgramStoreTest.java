package com.blocklang.release.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.blocklang.develop.constant.BuildTarget;

public class MiniProgramStoreTest {

	private MiniProgramStore store;
	private String rootDataPath;
	
	@BeforeEach
	public void setUp(@TempDir Path rootDir) {
		String dataRootPath = rootDir.toString();
		String owner = "jack";
		String repositoryName = "repo1";
		String projectName = "project1";
		String version = "master";
		BuildTarget buildTarget = BuildTarget.WEAPP;
		this.store = new MiniProgramStore(dataRootPath, owner, repositoryName, projectName, buildTarget, version);
		this.rootDataPath = rootDir.toString();
	}
	
	@Test
	public void getLogFilePathNoGitShortCommitId() {
		Path logFilePath = store.getLogFilePath(null);
		
		assertThat(logFilePath.getParent().compareTo(Paths.get(this.rootDataPath, "sources/jack/repo1/project1/buildLogs/weapp/default"))).isEqualTo(0);
		
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
	
	@Test
	public void getProjectModelDirectory() {
		Path projectModelDirectory = store.getProjectModelDirectory();
		assertThat(projectModelDirectory.compareTo(Paths.get(this.rootDataPath, "models/jack/repo1/project1"))).isEqualTo(0);
	}
	
	@Test
	public void getProjectSourceDirectory() {
		Path projectSourceDirectory = store.getProjectSourceDirectory();
		assertThat(projectSourceDirectory.compareTo(Paths.get(this.rootDataPath, "sources/jack/repo1/project1/source/weapp/default"))).isEqualTo(0);
	}
}
