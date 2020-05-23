package com.blocklang.marketplace.runner.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;
import org.springframework.util.StreamUtils;

import com.blocklang.core.util.JsonUtil;

public class BlocklangJsonValidatorTest {

	@Test
	public void run_json_node_is_null() {
		assertThrows(IllegalArgumentException.class, () -> BlocklangJsonValidator.run(null));
	}
	
	@Test
	public void run_ide_repo_config_success() throws IOException {
		String content = StreamUtils.copyToString(getClass().getResourceAsStream("widget_ide_repo_config.json"), Charset.defaultCharset());
		assertThat(BlocklangJsonValidator.run(JsonUtil.readTree(content))).isEmpty();
	}
	
	@Test
	public void run_ide_repo_config_failed() throws IOException {
		String content = StreamUtils.copyToString(getClass().getResourceAsStream("widget_ide_repo_config_invalid.json"), Charset.defaultCharset());
		assertThat(BlocklangJsonValidator.run(JsonUtil.readTree(content))).hasSize(2);
	}
}
