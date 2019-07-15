package com.blocklang.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LogFileReaderTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Test
	public void read_all_lines_then_no_log_file() throws IOException {
		assertThat(LogFileReader.readAllLines(null)).isEmpty();
		assertThat(LogFileReader.readAllLines(Paths.get("not-exist.file"))).isEmpty();
	}
	
	@Test
	public void read_all_lines_then_full_content() throws IOException {
		File logFile = tempFolder.newFile();
		Files.write(logFile.toPath(), Arrays.asList("a", "b", "c"), StandardOpenOption.APPEND);
		
		List<String> content = LogFileReader.readAllLines(logFile.toPath());
		assertThat(content).hasSize(3);
		assertThat(content.get(0)).isEqualTo("a");
		assertThat(content.get(1)).isEqualTo("b");
		assertThat(content.get(2)).isEqualTo("c");
	}
}
