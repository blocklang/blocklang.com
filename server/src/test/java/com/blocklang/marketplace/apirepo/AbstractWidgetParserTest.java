package com.blocklang.marketplace.apirepo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public abstract class AbstractWidgetParserTest {

	protected MarketplaceStore store;
	protected CliLogger logger;

	@BeforeEach
	public void setup(@TempDir Path tempDir) {
		var gitUrl = "https://github.com/you/your-repo.git";
		store = new MarketplaceStore(tempDir.toString(), gitUrl);
		logger = mock(CliLogger.class);
	}

	// api 仓库的目录结构
	// marketplace/
	//     github.com/
	//         you/
	//             you-repo/
	protected void createApiRepo() throws IOException {
		Path sourceDirectory = store.getRepoSourceDirectory();
		Files.createDirectories(sourceDirectory);
		GitUtils.init(sourceDirectory, "user", "user@email.com");
	}

	protected void assertWidget1(String widgetJson) throws JsonProcessingException {
		JsonNode widget = JsonUtil.readTree(widgetJson);
		assertThat(widget.get("code").asText()).isEqualTo("0001"); // 从 1 开始编号
		assertThat(widget.get("name").asText()).isEqualTo("Widget1");
		assertThat(widget.get("label").asText()).isEqualTo("Widget 1");
		assertThat(widget.get("description").asText()).isEmpty();
		
		JsonNode propertiesNodes = widget.get("properties");
		assertThat(propertiesNodes).hasSize(1);
		assertThat(propertiesNodes.get(0).get("code").asText()).isEqualTo("0001");
		assertThat(propertiesNodes.get(0).get("name").asText()).isEqualTo("prop1");
		assertThat(propertiesNodes.get(0).get("label").asText()).isEqualTo("prop 1");
		assertThat(propertiesNodes.get(0).get("defaultValue").asText()).isEqualTo("");
		assertThat(propertiesNodes.get(0).get("valueType").asText()).isEqualTo("string");
		
		JsonNode eventsNodes = widget.get("events");
		assertThat(eventsNodes).hasSize(1);
		assertThat(eventsNodes.get(0).get("code").asText()).isEqualTo("0002");
		assertThat(eventsNodes.get(0).get("name").asText()).isEqualTo("event1");
		assertThat(eventsNodes.get(0).get("label").asText()).isEqualTo("event 1");
		assertThat(eventsNodes.get(0).get("valueType").asText()).isEqualTo("function");
		assertThat(eventsNodes.get(0).get("arguments")).isEmpty();
	}

	protected void assertWidget2(String widgetJson) throws JsonProcessingException {
		JsonNode widget = JsonUtil.readTree(widgetJson);
		assertThat(widget.get("code").asText()).isEqualTo("0002");
		assertThat(widget.get("name").asText()).isEqualTo("Widget2");
		assertThat(widget.get("label").asText()).isEqualTo("Widget 2");
		assertThat(widget.get("description").asText()).isEmpty();
		
		JsonNode propertiesNodes = widget.get("properties");
		assertThat(propertiesNodes).hasSize(1);
		assertThat(propertiesNodes.get(0).get("code").asText()).isEqualTo("0001");
		assertThat(propertiesNodes.get(0).get("name").asText()).isEqualTo("prop2");
		assertThat(propertiesNodes.get(0).get("label").asText()).isEqualTo("prop 2");
		assertThat(propertiesNodes.get(0).get("defaultValue").asText()).isEqualTo("");
		assertThat(propertiesNodes.get(0).get("valueType").asText()).isEqualTo("string");
		
		JsonNode eventsNodes = widget.get("events");
		assertThat(eventsNodes).hasSize(1);
		assertThat(eventsNodes.get(0).get("code").asText()).isEqualTo("0002");
		assertThat(eventsNodes.get(0).get("name").asText()).isEqualTo("event2");
		assertThat(eventsNodes.get(0).get("label").asText()).isEqualTo("event 2");
		assertThat(eventsNodes.get(0).get("valueType").asText()).isEqualTo("function");
		assertThat(eventsNodes.get(0).get("arguments")).isEmpty();
	}
}
