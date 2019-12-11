package com.blocklang.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LogFileReaderTest {
	
	@Test
	public void read_all_lines_then_no_log_file() throws IOException {
		assertThat(LogFileReader.readAllLines(null)).isEmpty();
		assertThat(LogFileReader.readAllLines(Paths.get("not-exist.file"))).isEmpty();
	}
	
	@Test
	public void read_all_lines_then_full_content(@TempDir Path tempFolder) throws IOException {
		Path logFile = Files.createFile(tempFolder.resolve("a.log"));

		Files.write(logFile, Arrays.asList("a", "b", "c"), StandardOpenOption.APPEND);
		
		List<String> content = LogFileReader.readAllLines(logFile);
		assertThat(content).hasSize(3);
		assertThat(content.get(0)).isEqualTo("a");
		assertThat(content.get(1)).isEqualTo("b");
		assertThat(content.get(2)).isEqualTo("c");
	}
}
