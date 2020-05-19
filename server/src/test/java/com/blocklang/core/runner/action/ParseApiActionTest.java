package com.blocklang.core.runner.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.StreamUtils;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.DefaultExecutionContext;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class ParseApiActionTest {
	
	private ExecutionContext context;

	@BeforeEach
	private void setup() {
		context = new DefaultExecutionContext();
		var logger = mock(CliLogger.class);
		context.setLogger(logger);
	}
	
	@DisplayName("当没有 tags 时，给出提示信息，但依然算操作成功")
	@Test
	public void run_tags_is_empty() {
		context.putValue(ParseApiAction.INPUT_MASTER, false);
		
		var action = new ParseApiAction(context);
		assertThat(action.run()).isPresent();
	}
	
	@DisplayName("在 tag 中创建一个 Widget1 部件")
	@Test
	public void run_tags_create_widget_success(@TempDir Path tempDir) throws IOException {
		context.putValue(ParseApiAction.INPUT_MASTER, false);
		// FIXME: context 贯穿 ide 仓库和 api 仓库的构建和解析，context 中需支持存储多个 store，当合并操作时再处理
		
		String widget1Id = "202005151645";
		String changeFileId = "202005151646";

		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/you-repo.git");
		Path sourceDirectory = createApiRepo(store);
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widget1Id + "__widget1");
		Files.createDirectories(widget1Directory);
		String widget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFileId + "__create_widget.json"), widget1Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "first tag");
		
		context.putValue(ParseApiAction.INPUT_TAGS, Collections.singletonList("refs/tags/v0.1.0"));
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		var action = new ParseApiAction(context);
		// 1. 解析通过
		assertThat(action.run()).isPresent();
		// 2. 将解析结果存在 package/0.1.0/components/button/index.json 文件中
		//    在输出结果中，为了提高辨识度，将 properties 和 events 分开存储
		//    断言 index.json 文件中的内容
		//		{	
		//			"name": "Button",
		//			"label": "按钮",
		//			"description": "",
		//			"properties": [{
		//				"name": "label",
		//				"label": "文本",
		//				"defaultValue": "",
		//				"valueType": "string"
		//			}],
		//			"events": [{
		//				"name": "onClick",
		//				"label": "单击按钮",
		//				"valueType": "function",
		//				"arguments": []
		//			}]
		//		}
		String widget1Api = Files.readString(store.getPackageVersionDirectory("0.1.0").resolve(widget1Id).resolve("index.json"));
		// FIXME: 之前的设计是此表存储 Widget、Service 和 API 等所有组件，但是这三个信息差别较大，ApiComponent 可能会专门存 Widget
		assertWidget1(widget1Api);
		// 断言哪些文件已执行过
		String changelog = Files.readString(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widget1Id).resolve("index.json"));
		List<PublishedFileInfo> changelogs = JsonUtil.fromJsonArray(changelog, PublishedFileInfo.class);
		PublishedFileInfo firstChangeLog = new PublishedFileInfo();
		firstChangeLog.setFileId(changeFileId); // 文件标识，不是文件夹标识
		firstChangeLog.setVersion("0.1.0");
		// 该 jsonContent 中包含 \r，需要替换掉
		firstChangeLog.setMd5sum(DigestUtils.md5Hex(widget1Json.replaceAll("\r", "")));
		assertThat(changelogs).hasSize(1).first().usingRecursiveComparison().isEqualTo(firstChangeLog);
	}
	
	@DisplayName("有两个 tag，解析第一个 tag 出错后，不再解析第二个 tag")
	@Test
	public void run_tags_tag1_failed_then_not_parse_tag2(@TempDir Path tempDir) throws IOException {
		context.putValue(ParseApiAction.INPUT_MASTER, false);

		String widget1Id = "1";
		String changeFileId = "2";
		
		// FIXME: context 贯穿 ide 仓库和 api 仓库的构建和解析，context 中需支持存储多个 store，当合并操作时再处理
		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/you-repo.git");
		
		// v0.1.0 中的 changelog 定义无效
		Path sourceDirectory = createApiRepo(store);
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widget1Id + "__widget1");
		Files.createDirectories(widget1Directory);
		Files.writeString(widget1Directory.resolve(changeFileId + "__create_widget.json"), "{}");
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "first tag");
		
		// v0.1.0 中的 changelog 定义有效
		sourceDirectory = createApiRepo(store);
		widget1Directory = sourceDirectory.resolve("changelog").resolve(widget1Id + "__widget1");
		Files.createDirectories(widget1Directory);
		String widget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFileId + "__create_widget.json"), widget1Json, StandardOpenOption.TRUNCATE_EXISTING);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.2.0", "second tag");
		
		context.putValue(ParseApiAction.INPUT_TAGS, Arrays.asList("refs/tags/v0.1.0", "refs/tags/v0.2.0"));
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		var action = new ParseApiAction(context);
		
		assertThat(action.run()).isEmpty();

		// tag v0.1.0
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve(widget1Id).resolve("index.json").toFile().exists()).isFalse();
		
		// tag v0.2.0
		assertThat(store.getPackageVersionDirectory("0.2.0").resolve(widget1Id).resolve("index.json").toFile().exists()).isFalse();
		
		// 断言哪些文件已执行过
		assertThat(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widget1Id).resolve("index.json").toFile().exists()).isFalse();
	}
	
	@DisplayName("在第一个 tag 中创建一个 widget1，在第二 tag 中未对 widget1 做任何更改")
	@Test
	public void run_tags_based_on_previous_tag_no_update(@TempDir Path tempDir) throws IOException {
		context.putValue(ParseApiAction.INPUT_MASTER, false);

		String widget1Id = "202005151645";
		String changeFileId = "202005151646";
		
		// FIXME: context 贯穿 ide 仓库和 api 仓库的构建和解析，context 中需支持存储多个 store，当合并操作时再处理
		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/you-repo.git");
		Path sourceDirectory = createApiRepo(store);
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widget1Id + "__widget1");
		Files.createDirectories(widget1Directory);
		String widget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFileId + "__create_widget.json"), widget1Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "first tag");
		GitUtils.tag(sourceDirectory, "v0.2.0", "second tag");
		
		context.putValue(ParseApiAction.INPUT_TAGS, Arrays.asList("refs/tags/v0.1.0", "refs/tags/v0.2.0"));
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		var action = new ParseApiAction(context);
		
		// 解析通过
		assertThat(action.run()).isPresent();

		// tag v0.1.0
		String widget1Api1 = Files.readString(store.getPackageVersionDirectory("0.1.0").resolve(widget1Id).resolve("index.json"));
		// FIXME: 之前的设计是此表存储 Widget、Service 和 API 等所有组件，但是这三个信息差别较大，ApiComponent 可能会专门存 Widget
		assertWidget1(widget1Api1);
		
		// tag v0.2.0
		String widget1Api2 = Files.readString(store.getPackageVersionDirectory("0.2.0").resolve(widget1Id).resolve("index.json"));
		assertWidget1(widget1Api2);
		
		// 断言哪些文件已执行过
		String changelog = Files.readString(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widget1Id).resolve("index.json"));
		List<PublishedFileInfo> changelogs = JsonUtil.fromJsonArray(changelog, PublishedFileInfo.class);
		PublishedFileInfo firstChangeLog = new PublishedFileInfo();
		firstChangeLog.setFileId(changeFileId); // 文件标识，不是文件夹标识
		firstChangeLog.setVersion("0.1.0");
		// 该 jsonContent 中包含 \r，需要替换掉
		firstChangeLog.setMd5sum(DigestUtils.md5Hex(widget1Json.replaceAll("\r", "")));
		assertThat(changelogs).hasSize(1).first().usingRecursiveComparison().isEqualTo(firstChangeLog);
	}
	
	@DisplayName("在第一个 tag 中创建一个 widget1，在第二 tag 中创建 widget2")
	@Test
	public void run_tags_create_widget1_tag1_widget2_tag2(@TempDir Path tempDir) throws IOException {
		context.putValue(ParseApiAction.INPUT_MASTER, false);
		
		// FIXME: context 贯穿 ide 仓库和 api 仓库的构建和解析，context 中需支持存储多个 store，当合并操作时再处理
		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/you-repo.git");
		Path sourceDirectory = createApiRepo(store);
		// 创建 widget1 并标注 git tag
		String widget1Id = "1";
		String changeFile1Id = "2";
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widget1Id + "__widget1");
		Files.createDirectories(widget1Directory);
		String widget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile1Id + "__create_widget.json"), widget1Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "first tag");
		
		// 创建 widget2 并标注 git tag
		String widget2Id = "3";
		String changeFile2Id = "4";
		Path widget2Directory = sourceDirectory.resolve("changelog").resolve(widget2Id + "__widget2");
		Files.createDirectory(widget2Directory);
		String widget2Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget2.json"), Charset.defaultCharset());
		Files.writeString(widget2Directory.resolve(changeFile2Id + "__create_widget.json"), widget2Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "second commit");
		GitUtils.tag(sourceDirectory, "v0.2.0", "second tag");
		
		context.putValue(ParseApiAction.INPUT_TAGS, Arrays.asList("refs/tags/v0.1.0", "refs/tags/v0.2.0"));
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		var action = new ParseApiAction(context);
		
		assertThat(action.run()).isPresent();

		// tag v0.1.0
		String widget1Api1 = Files.readString(store.getPackageVersionDirectory("0.1.0").resolve(widget1Id).resolve("index.json"));
		assertWidget1(widget1Api1);
		// 断言在 0.1.0 版本中并未创建 widget2
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve(widget2Id).resolve("index.json").toFile().exists());
		
		// tag v0.2.0
		String widget1Api2 = Files.readString(store.getPackageVersionDirectory("0.2.0").resolve(widget1Id).resolve("index.json"));
		assertWidget1(widget1Api2);
		
		String widget2Api2 = Files.readString(store.getPackageVersionDirectory("0.2.0").resolve(widget2Id).resolve("index.json"));
		assertWidget2(widget2Api2);
		
		// 断言生成 widget1 API 时执行了哪些 changelog
		String changeLog1 = Files.readString(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widget1Id).resolve("index.json"));
		List<PublishedFileInfo> changelogs1 = JsonUtil.fromJsonArray(changeLog1, PublishedFileInfo.class);
		PublishedFileInfo changeLogInfo1 = new PublishedFileInfo();
		changeLogInfo1.setFileId(changeFile1Id);
		changeLogInfo1.setVersion("0.1.0");
		changeLogInfo1.setMd5sum(DigestUtils.md5Hex(widget1Json.replaceAll("\r", ""))); // 该 jsonContent 中包含 \r，需要替换掉
		assertThat(changelogs1).hasSize(1).first().usingRecursiveComparison().isEqualTo(changeLogInfo1);

		// 断言生成 widget2 API 时执行了哪些 changelog
		String changeLog2 = Files.readString(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widget2Id).resolve("index.json"));
		List<PublishedFileInfo> changelogs2 = JsonUtil.fromJsonArray(changeLog2, PublishedFileInfo.class);
		PublishedFileInfo changeLogInfo2 = new PublishedFileInfo();
		changeLogInfo2.setFileId(changeFile2Id);
		changeLogInfo2.setVersion("0.2.0");
		changeLogInfo2.setMd5sum(DigestUtils.md5Hex(widget2Json.replaceAll("\r", ""))); // 该 jsonContent 中包含 \r，需要替换掉
		assertThat(changelogs2).hasSize(1).first().usingRecursiveComparison().isEqualTo(changeLogInfo2);
	}

	// 当已发布的变更文件被修改后，给出错误提示
	@DisplayName("已发布后的 changelog 文件不允许修改")
	@Test
	public void run_tags_report_errors_when_changelog_update_after_tag(@TempDir Path tempDir) throws IOException {
		context.putValue(ParseApiAction.INPUT_MASTER, false);

		String widget1Id = "202005151645";
		String changeFile1Id = "202005151646";
		
		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/you-repo.git");
		Path sourceDirectory = createApiRepo(store);
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widget1Id + "__widget1");
		Files.createDirectories(widget1Directory);
		String widget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile1Id + "__create_widget.json"), widget1Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "first tag");

		// 使用 __create_widget.json
		String widget2Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget2.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile1Id + "__create_widget.json"), widget2Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "second commit");
		GitUtils.tag(sourceDirectory, "v0.2.0", "second tag");
		
		context.putValue(ParseApiAction.INPUT_TAGS, Arrays.asList("refs/tags/v0.1.0", "refs/tags/v0.2.0"));
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		var action = new ParseApiAction(context);
		
		// 解析未通过
		assertThat(action.run()).isEmpty();

		// tag v0.1.0，解析成功
		String widget1Api1 = Files.readString(store.getPackageVersionDirectory("0.1.0").resolve(widget1Id).resolve("index.json"));
		assertWidget1(widget1Api1);
		
		// tag v0.2.0，解析失败
		assertThat(store.getPackageVersionDirectory("0.2.0").resolve(widget1Id).resolve("index.json").toFile().exists()).isFalse();
		
		// 断言哪些文件已执行过
		String changelog = Files.readString(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widget1Id).resolve("index.json"));
		List<PublishedFileInfo> changelogs = JsonUtil.fromJsonArray(changelog, PublishedFileInfo.class);
		PublishedFileInfo firstChangeLog = new PublishedFileInfo();
		firstChangeLog.setFileId(changeFile1Id); // 文件标识，不是文件夹标识
		firstChangeLog.setVersion("0.1.0");
		// 该 jsonContent 中包含 \r，需要替换掉
		firstChangeLog.setMd5sum(DigestUtils.md5Hex(widget1Json.replaceAll("\r", "")));
		assertThat(changelogs).hasSize(1).first().usingRecursiveComparison().isEqualTo(firstChangeLog);
	}
	
	@DisplayName("对同一个 widget 执行两次 create widget")
	@Test
	public void run_tags_create_same_widget_twice(@TempDir Path tempDir) throws IOException {
		context.putValue(ParseApiAction.INPUT_MASTER, false);
		
		String widget1Id = "1";
		String changeFile1Id = "2";
		String changeFile2Id = "3";

		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/you-repo.git");
		Path sourceDirectory = createApiRepo(store);
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widget1Id + "__widget1");
		Files.createDirectories(widget1Directory);
		String widget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget_no_properties.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile1Id + "__create_widget.json"), widget1Json);
		Files.writeString(widget1Directory.resolve(changeFile2Id + "__create_widget.json"), widget1Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "first tag");
		
		context.putValue(ParseApiAction.INPUT_TAGS, Collections.singletonList("refs/tags/v0.1.0"));
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		var action = new ParseApiAction(context);
		
		assertThat(action.run()).isEmpty();
		
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve(widget1Id).resolve("index.json").toFile().exists()).isFalse();
		assertThat(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widget1Id).resolve("index.json").toFile().exists()).isFalse();
	}
	
	@DisplayName("在两个文件夹中分别创建了一个 widget，但是 name 相同，widget 名必须唯一")
	@Test
	public void run_tags_create_widget_name_should_unique(@TempDir Path tempDir) throws IOException {
		context.putValue(ParseApiAction.INPUT_MASTER, false);
		
		String widget1Id = "1";
		String widget2Id = "2";
		String changeFile1Id = "3";
		String changeFile2Id = "4";

		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/you-repo.git");
		Path sourceDirectory = createApiRepo(store);
		
		String widget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget_no_properties.json"), Charset.defaultCharset());
		
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widget1Id + "__widget1");
		Files.createDirectories(widget1Directory);
		Files.writeString(widget1Directory.resolve(changeFile1Id + "__create_widget.json"), widget1Json);
		
		Path widget2Directory = sourceDirectory.resolve("changelog").resolve(widget2Id + "__widget1");
		Files.createDirectories(widget2Directory);
		Files.writeString(widget2Directory.resolve(changeFile2Id + "__create_widget.json"), widget1Json);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "first tag");
		
		context.putValue(ParseApiAction.INPUT_TAGS, Collections.singletonList("refs/tags/v0.1.0"));
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		var action = new ParseApiAction(context);
		
		assertThat(action.run()).isEmpty();
		
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve(widget1Id).resolve("index.json").toFile().exists()).isFalse();
		assertThat(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widget1Id).resolve("index.json").toFile().exists()).isFalse();
	}
	
	// create widget and add property in one change log file
	@DisplayName("在一个 changelog 中先 createWidget，然后再 addProperty")
	@Test
	public void run_tags_create_widget_then_add_property_in_one_changelog_success(@TempDir Path tempDir) throws IOException {
		context.putValue(ParseApiAction.INPUT_MASTER, false);
	
		String widget1Id = "1";
		String changeFileId = "2";

		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/you-repo.git");
		Path sourceDirectory = createApiRepo(store);
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widget1Id + "__widget1");
		Files.createDirectories(widget1Directory);
		String widget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1_and_add_property1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFileId + "__create_widget.json"), widget1Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "first tag");
		
		context.putValue(ParseApiAction.INPUT_TAGS, Collections.singletonList("refs/tags/v0.1.0"));
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		var action = new ParseApiAction(context);
		// 解析通过
		assertThat(action.run()).isPresent();
		
		String widget1Api = Files.readString(store.getPackageVersionDirectory("0.1.0").resolve(widget1Id).resolve("index.json"));
		JsonNode widget = JsonUtil.readTree(widget1Api);
		assertThat(widget.get("code").asText()).isEqualTo("0001"); // 从 1 开始编号
		assertThat(widget.get("name").asText()).isEqualTo("Widget1");
		assertThat(widget.get("label").asText()).isEqualTo("Widget 1");
		assertThat(widget.get("description").asText()).isEmpty();
		
		JsonNode propertiesNodes = widget.get("properties");
		assertThat(propertiesNodes).hasSize(1);
		assertThat(propertiesNodes.get(0).get("code").asText()).isEqualTo("0002");
		assertThat(propertiesNodes.get(0).get("name").asText()).isEqualTo("prop1");
		assertThat(propertiesNodes.get(0).get("label").asText()).isEqualTo("prop 1");
		assertThat(propertiesNodes.get(0).get("defaultValue").asText()).isEqualTo("");
		assertThat(propertiesNodes.get(0).get("valueType").asText()).isEqualTo("string");
		
		JsonNode eventsNodes = widget.get("events");
		assertThat(eventsNodes).hasSize(1);
		assertThat(eventsNodes.get(0).get("code").asText()).isEqualTo("0001");
		assertThat(eventsNodes.get(0).get("name").asText()).isEqualTo("event1");
		assertThat(eventsNodes.get(0).get("label").asText()).isEqualTo("event 1");
		assertThat(eventsNodes.get(0).get("valueType").asText()).isEqualTo("function");
		assertThat(eventsNodes.get(0).get("arguments")).isEmpty();
		
		// 断言哪些文件已执行过
		String changelog = Files.readString(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widget1Id).resolve("index.json"));
		List<PublishedFileInfo> changelogs = JsonUtil.fromJsonArray(changelog, PublishedFileInfo.class);
		PublishedFileInfo firstChangeLog = new PublishedFileInfo();
		firstChangeLog.setFileId(changeFileId); // 文件标识，不是文件夹标识
		firstChangeLog.setVersion("0.1.0");
		// 该 jsonContent 中包含 \r，需要替换掉
		firstChangeLog.setMd5sum(DigestUtils.md5Hex(widget1Json.replaceAll("\r", "")));
		assertThat(changelogs).hasSize(1).first().usingRecursiveComparison().isEqualTo(firstChangeLog);
	}
	
	// add property_success in two changelog
	@DisplayName("在一个 changelog 中 createWidget，然后在另一个 changlog 中 addProperty")
	@Test
	public void run_tags_create_widget_then_add_property_in_two_changelog_success(@TempDir Path tempDir) throws IOException {
		context.putValue(ParseApiAction.INPUT_MASTER, false);
	
		String widgetId = "1";
		String changeFile1Id = "2";
		String changeFile2Id = "3";

		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/you-repo.git");
		Path sourceDirectory = createApiRepo(store);
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widgetId + "__widget1");
		Files.createDirectories(widget1Directory);
		String createWidget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile1Id + "__create_widget.json"), createWidget1Json);
		
		String addProperty2Json = StreamUtils.copyToString(getClass().getResourceAsStream("add_property2.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile2Id + "__add_property.json"), addProperty2Json);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "first tag");
		
		context.putValue(ParseApiAction.INPUT_TAGS, Collections.singletonList("refs/tags/v0.1.0"));
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		var action = new ParseApiAction(context);
		// 解析通过
		assertThat(action.run()).isPresent();
		
		String widget1Api = Files.readString(store.getPackageVersionDirectory("0.1.0").resolve(widgetId).resolve("index.json"));
		JsonNode widget = JsonUtil.readTree(widget1Api);
		assertThat(widget.get("code").asText()).isEqualTo("0001"); // 从 1 开始编号
		assertThat(widget.get("name").asText()).isEqualTo("Widget1");
		assertThat(widget.get("label").asText()).isEqualTo("Widget 1");
		assertThat(widget.get("description").asText()).isEmpty();
		
		JsonNode propertiesNodes = widget.get("properties");
		assertThat(propertiesNodes).hasSize(2);
		assertThat(propertiesNodes.get(0).get("code").asText()).isEqualTo("0001");
		assertThat(propertiesNodes.get(0).get("name").asText()).isEqualTo("prop1");
		assertThat(propertiesNodes.get(0).get("label").asText()).isEqualTo("prop 1");
		assertThat(propertiesNodes.get(0).get("defaultValue").asText()).isEqualTo("");
		assertThat(propertiesNodes.get(0).get("valueType").asText()).isEqualTo("string");
		
		assertThat(propertiesNodes.get(1).get("code").asText()).isEqualTo("0003");
		assertThat(propertiesNodes.get(1).get("name").asText()).isEqualTo("prop2");
		assertThat(propertiesNodes.get(1).get("label").asText()).isEqualTo("prop 2");
		assertThat(propertiesNodes.get(1).get("defaultValue").asText()).isEqualTo("");
		assertThat(propertiesNodes.get(1).get("valueType").asText()).isEqualTo("string");
		
		JsonNode eventsNodes = widget.get("events");
		assertThat(eventsNodes).hasSize(1);
		assertThat(eventsNodes.get(0).get("code").asText()).isEqualTo("0002");
		assertThat(eventsNodes.get(0).get("name").asText()).isEqualTo("event1");
		assertThat(eventsNodes.get(0).get("label").asText()).isEqualTo("event 1");
		assertThat(eventsNodes.get(0).get("valueType").asText()).isEqualTo("function");
		assertThat(eventsNodes.get(0).get("arguments")).isEmpty();
		
		// 断言哪些文件已执行过
		String changelog = Files.readString(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widgetId).resolve("index.json"));
		List<PublishedFileInfo> changelogs = JsonUtil.fromJsonArray(changelog, PublishedFileInfo.class);
		
		PublishedFileInfo firstChangeLog = new PublishedFileInfo();
		firstChangeLog.setFileId(changeFile1Id); // 文件标识，不是文件夹标识
		firstChangeLog.setVersion("0.1.0");
		// 该 jsonContent 中包含 \r，需要替换掉
		firstChangeLog.setMd5sum(DigestUtils.md5Hex(createWidget1Json.replaceAll("\r", "")));
		
		PublishedFileInfo secondChangeLog = new PublishedFileInfo();
		secondChangeLog.setFileId(changeFile2Id); // 文件标识，不是文件夹标识
		secondChangeLog.setVersion("0.1.0");
		// 该 jsonContent 中包含 \r，需要替换掉
		secondChangeLog.setMd5sum(DigestUtils.md5Hex(addProperty2Json.replaceAll("\r", "")));
		
		assertThat(changelogs).hasSize(2);
		assertThat(changelogs.get(0)).usingRecursiveComparison().isEqualTo(firstChangeLog);
		assertThat(changelogs.get(1)).usingRecursiveComparison().isEqualTo(secondChangeLog);
	}
	
	// add property_already_exists
	@DisplayName("通过 addProperty 为 widget 添加一个属性，而此属性已存在")
	@Test
	public void run_tags_add_property_that_already_exist(@TempDir Path tempDir) throws IOException {
		context.putValue(ParseApiAction.INPUT_MASTER, false);
	
		String widgetId = "1";
		String changeFile1Id = "2";
		String changeFile2Id = "3";

		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/you-repo.git");
		Path sourceDirectory = createApiRepo(store);
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widgetId + "__widget1");
		Files.createDirectories(widget1Directory);
		String createWidget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1_and_add_property2.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile1Id + "__create_widget.json"), createWidget1Json);
		
		String addProperty2Json = StreamUtils.copyToString(getClass().getResourceAsStream("add_property2.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile2Id + "__add_property.json"), addProperty2Json);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "first tag");
		
		context.putValue(ParseApiAction.INPUT_TAGS, Collections.singletonList("refs/tags/v0.1.0"));
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		var action = new ParseApiAction(context);

		assertThat(action.run()).isEmpty();
		
		// createWidget 和 addProperty 两个操作分属两个变更文件，但在同一个 tag 中，
		// 当 addProperty 解析错误后，则也应该回滚（或称为不应用）createWidget 的操作，
		// 即，changelog 日志文件和 widget 的最终结构，这两个文件应该不记录任何内容
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve(widgetId).resolve("index.json").toFile().exists()).isFalse();
		assertThat(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widgetId).resolve("index.json").toFile().exists()).isFalse();
	}
	
	@DisplayName("没有 create widget 直接 add properties")
	@Test
	public void run_tags_add_property_but_not_create_widget(@TempDir Path tempDir) throws IOException {
		context.putValue(ParseApiAction.INPUT_MASTER, false);
		
		String widgetId = "1";
		String changeFile1Id = "2";
		
		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/your-repo.git");
		Path sourceDirectory = createApiRepo(store);
		Path widgetDirectory = sourceDirectory.resolve("changelog").resolve(widgetId + "__widget1");
		Files.createDirectories(widgetDirectory);
		String addPropertyJson = StreamUtils.copyToString(getClass().getResourceAsStream("add_property2.json"), Charset.defaultCharset());
		Files.writeString(widgetDirectory.resolve(changeFile1Id + "__add_property.json"), addPropertyJson);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "first tag");
		
		context.putValue(ParseApiAction.INPUT_TAGS, Collections.singletonList("refs/tags/v0.1.0"));
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		var action = new ParseApiAction(context);
		
		assertThat(action.run()).isEmpty();
		
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve(widgetId).resolve("index.json").toFile().exists()).isFalse();
		assertThat(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widgetId).resolve("index.json").toFile().exists()).isFalse();
	}
	
	@DisplayName("在一个 changelog 文件中，先使用 createWidget 创建一个 Widget，然后使用 addEvent 添加一个事件")
	@Test
	public void run_tags_create_widget_then_add_event_in_one_changelog_success(@TempDir Path tempDir) throws IOException {
		context.putValue(ParseApiAction.INPUT_MASTER, false);
		
		String widget1Id = "1";
		String changeFileId = "2";

		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/you-repo.git");
		Path sourceDirectory = createApiRepo(store);
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widget1Id + "__widget1");
		Files.createDirectories(widget1Directory);
		String widget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1_and_add_event1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFileId + "__create_widget.json"), widget1Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "first tag");
		
		context.putValue(ParseApiAction.INPUT_TAGS, Collections.singletonList("refs/tags/v0.1.0"));
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		var action = new ParseApiAction(context);
		// 解析通过
		assertThat(action.run()).isPresent();
		
		String widget1Api = Files.readString(store.getPackageVersionDirectory("0.1.0").resolve(widget1Id).resolve("index.json"));
		JsonNode widget = JsonUtil.readTree(widget1Api);
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
		
		// 断言哪些文件已执行过
		String changelog = Files.readString(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widget1Id).resolve("index.json"));
		List<PublishedFileInfo> changelogs = JsonUtil.fromJsonArray(changelog, PublishedFileInfo.class);
		PublishedFileInfo firstChangeLog = new PublishedFileInfo();
		firstChangeLog.setFileId(changeFileId); // 文件标识，不是文件夹标识
		firstChangeLog.setVersion("0.1.0");
		// 该 jsonContent 中包含 \r，需要替换掉
		firstChangeLog.setMd5sum(DigestUtils.md5Hex(widget1Json.replaceAll("\r", "")));
		assertThat(changelogs).hasSize(1).first().usingRecursiveComparison().isEqualTo(firstChangeLog);
	}
	
	@DisplayName("在一个 changelog 中 createWidget，然后在另一个 changlog 中 addEvent")
	@Test
	public void run_tags_create_widget_then_add_event_in_two_changelog_success(@TempDir Path tempDir) throws IOException {
		context.putValue(ParseApiAction.INPUT_MASTER, false);
		
		String widgetId = "1";
		String changeFile1Id = "2";
		String changeFile2Id = "3";

		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/you-repo.git");
		Path sourceDirectory = createApiRepo(store);
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widgetId + "__widget1");
		Files.createDirectories(widget1Directory);
		String createWidget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile1Id + "__create_widget.json"), createWidget1Json);
		
		String addEvent2Json = StreamUtils.copyToString(getClass().getResourceAsStream("add_event2.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile2Id + "__add_event.json"), addEvent2Json);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "first tag");
		
		context.putValue(ParseApiAction.INPUT_TAGS, Collections.singletonList("refs/tags/v0.1.0"));
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		var action = new ParseApiAction(context);
		// 解析通过
		assertThat(action.run()).isPresent();
		
		String widget1Api = Files.readString(store.getPackageVersionDirectory("0.1.0").resolve(widgetId).resolve("index.json"));
		JsonNode widget = JsonUtil.readTree(widget1Api);
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
		assertThat(eventsNodes).hasSize(2);
		assertThat(eventsNodes.get(0).get("code").asText()).isEqualTo("0002");
		assertThat(eventsNodes.get(0).get("name").asText()).isEqualTo("event1");
		assertThat(eventsNodes.get(0).get("label").asText()).isEqualTo("event 1");
		assertThat(eventsNodes.get(0).get("valueType").asText()).isEqualTo("function");
		assertThat(eventsNodes.get(0).get("arguments")).isEmpty();
		
		assertThat(eventsNodes.get(1).get("code").asText()).isEqualTo("0003");
		assertThat(eventsNodes.get(1).get("name").asText()).isEqualTo("event2");
		assertThat(eventsNodes.get(1).get("label").asText()).isEqualTo("event 2");
		assertThat(eventsNodes.get(1).get("valueType").asText()).isEqualTo("function");
		assertThat(eventsNodes.get(1).get("arguments")).isEmpty();
		
		// 断言哪些文件已执行过
		String changelog = Files.readString(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widgetId).resolve("index.json"));
		List<PublishedFileInfo> changelogs = JsonUtil.fromJsonArray(changelog, PublishedFileInfo.class);
		
		PublishedFileInfo firstChangeLog = new PublishedFileInfo();
		firstChangeLog.setFileId(changeFile1Id); // 文件标识，不是文件夹标识
		firstChangeLog.setVersion("0.1.0");
		// 该 jsonContent 中包含 \r，需要替换掉
		firstChangeLog.setMd5sum(DigestUtils.md5Hex(createWidget1Json.replaceAll("\r", "")));
		
		PublishedFileInfo secondChangeLog = new PublishedFileInfo();
		secondChangeLog.setFileId(changeFile2Id); // 文件标识，不是文件夹标识
		secondChangeLog.setVersion("0.1.0");
		// 该 jsonContent 中包含 \r，需要替换掉
		secondChangeLog.setMd5sum(DigestUtils.md5Hex(addEvent2Json.replaceAll("\r", "")));
		
		assertThat(changelogs).hasSize(2);
		assertThat(changelogs.get(0)).usingRecursiveComparison().isEqualTo(firstChangeLog);
		assertThat(changelogs.get(1)).usingRecursiveComparison().isEqualTo(secondChangeLog);
	}
	
	@DisplayName("通过 addEvent 为 widget 添加一个事件，而此事件已存在")
	@Test
	public void run_tags_add_event_that_already_exist(@TempDir Path tempDir) throws IOException {
		context.putValue(ParseApiAction.INPUT_MASTER, false);
		
		String widgetId = "1";
		String changeFile1Id = "2";
		String changeFile2Id = "3";

		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/you-repo.git");
		Path sourceDirectory = createApiRepo(store);
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widgetId + "__widget1");
		Files.createDirectories(widget1Directory);
		String createWidget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile1Id + "__create_widget.json"), createWidget1Json);
		
		String addEvent1Json = StreamUtils.copyToString(getClass().getResourceAsStream("add_event1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile2Id + "__add_event.json"), addEvent1Json);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "first tag");
		
		context.putValue(ParseApiAction.INPUT_TAGS, Collections.singletonList("refs/tags/v0.1.0"));
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		var action = new ParseApiAction(context);
	
		assertThat(action.run()).isEmpty();
		
		// createWidget 和 addProperty 两个操作分属两个变更文件，但在同一个 tag 中，
		// 当 addProperty 解析错误后，则也应该回滚（或称为不应用）createWidget 的操作，
		// 即，changelog 日志文件和 widget 的最终结构，这两个文件应该不记录任何内容
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve(widgetId).resolve("index.json").toFile().exists()).isFalse();
		assertThat(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widgetId).resolve("index.json").toFile().exists()).isFalse();
	}
	
	@DisplayName("没有 create widget 直接 add event")
	@Test
	public void run_tags_add_event_but_not_create_widget(@TempDir Path tempDir) throws IOException {
		context.putValue(ParseApiAction.INPUT_MASTER, false);
		
		String widgetId = "1";
		String changeFile1Id = "2";
		
		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/your-repo.git");
		Path sourceDirectory = createApiRepo(store);
		Path widgetDirectory = sourceDirectory.resolve("changelog").resolve(widgetId + "__widget1");
		Files.createDirectories(widgetDirectory);
		String addEvent1Json = StreamUtils.copyToString(getClass().getResourceAsStream("add_event1.json"), Charset.defaultCharset());
		Files.writeString(widgetDirectory.resolve(changeFile1Id + "__add_event.json"), addEvent1Json);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "first tag");
		
		context.putValue(ParseApiAction.INPUT_TAGS, Collections.singletonList("refs/tags/v0.1.0"));
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		var action = new ParseApiAction(context);
		
		assertThat(action.run()).isEmpty();
		
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve(widgetId).resolve("index.json").toFile().exists()).isFalse();
		assertThat(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widgetId).resolve("index.json").toFile().exists()).isFalse();
	}
	
	// 如果之前构建过 master，则依然解析，并覆盖之前的结构
	@DisplayName("每次解析时都要重新解析 master 分支")
	@Test
	public void run_master_always_reparse(@TempDir Path tempDir) throws IOException {
		String widgetId = "1";
		String changeFile1Id = "2";
		
		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/your-repo.git");
		Path sourceDirectory = createApiRepo(store);
		
		Path widgetDirectory = sourceDirectory.resolve("changelog").resolve(widgetId + "__widget1");
		Files.createDirectories(widgetDirectory);
		String createWidgetJson = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1.json"), Charset.defaultCharset());
		Files.writeString(widgetDirectory.resolve(changeFile1Id + "__create_widget.json"), createWidgetJson);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		
		// 此处并未设置 tags，也没有禁止解析 master，所以只会解析 master
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		var action = new ParseApiAction(context);
		
		assertThat(action.run()).isPresent();
		
		String widget1Api = Files.readString(store.getPackageVersionDirectory("master").resolve(widgetId).resolve("index.json"));
		assertWidget1(widget1Api);
		
		String changelog = Files.readString(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widgetId).resolve("index.json"));
		List<PublishedFileInfo> changelogs = JsonUtil.fromJsonArray(changelog, PublishedFileInfo.class);
		PublishedFileInfo firstPublishedFile = new PublishedFileInfo();
		firstPublishedFile.setFileId(changeFile1Id); // 文件标识，不是文件夹标识
		firstPublishedFile.setVersion("master");
		// 该 jsonContent 中包含 \r，需要替换掉
		firstPublishedFile.setMd5sum(DigestUtils.md5Hex(createWidgetJson.replaceAll("\r", "")));
		assertThat(changelogs).hasSize(1).first().usingRecursiveComparison().isEqualTo(firstPublishedFile);
		
		// 修改内容后，依然解析
		String createWidget2Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget2.json"), Charset.defaultCharset());
		Files.writeString(widgetDirectory.resolve(changeFile1Id + "__create_widget.json"), createWidget2Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		
		assertThat(action.run()).isPresent();
		
		String widget21Api = Files.readString(store.getPackageVersionDirectory("master").resolve(widgetId).resolve("index.json"));
		JsonNode widget = JsonUtil.readTree(widget21Api);
		assertThat(widget.get("code").asText()).isEqualTo("0001");
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
		
		String changelog2 = Files.readString(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widgetId).resolve("index.json"));
		List<PublishedFileInfo> changelogs2 = JsonUtil.fromJsonArray(changelog2, PublishedFileInfo.class);
		PublishedFileInfo secondPublishedFile = new PublishedFileInfo();
		secondPublishedFile.setFileId(changeFile1Id); // 文件标识，不是文件夹标识
		secondPublishedFile.setVersion("master");
		// 该 jsonContent 中包含 \r，需要替换掉
		secondPublishedFile.setMd5sum(DigestUtils.md5Hex(createWidget2Json.replaceAll("\r", "")));
		assertThat(changelogs2).hasSize(1).first().usingRecursiveComparison().isEqualTo(secondPublishedFile);
		
		assertThat(firstPublishedFile.getMd5sum()).isNotEqualTo(secondPublishedFile.getMd5sum());
	}
	
	@DisplayName("先解析 tag，然后解析 master，但是 master 分支中没有新增内容")
	@Test
	public void run_tags_and_master_no_new_commit(@TempDir Path tempDir) throws IOException {
		String widgetId = "1";
		String changeFile1Id = "2";
		
		var store = new MarketplaceStore(tempDir.resolve("rootPath").toString(), "https://github.com/you/your-repo.git");
		Path sourceDirectory = createApiRepo(store);
		
		Path widgetDirectory = sourceDirectory.resolve("changelog").resolve(widgetId + "__widget1");
		Files.createDirectories(widgetDirectory);
		String createWidgetJson = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1.json"), Charset.defaultCharset());
		Files.writeString(widgetDirectory.resolve(changeFile1Id + "__create_widget.json"), createWidgetJson);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		GitUtils.tag(sourceDirectory, "v0.1.0", "first tag");
		
		// 此处并未设置 tags，也没有禁止解析 master，所以只会解析 master
		context.putValue(ExecutionContext.MARKETPLACE_STORE, store);
		context.putValue(ParseApiAction.INPUT_TAGS, Collections.singletonList("refs/tags/v0.1.0"));
		var action = new ParseApiAction(context);
		
		assertThat(action.run()).isPresent();
		
		String widget1Api = Files.readString(store.getPackageVersionDirectory("master").resolve(widgetId).resolve("index.json"));
		assertWidget1(widget1Api);
		
		String changelog = Files.readString(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widgetId).resolve("index.json"));
		List<PublishedFileInfo> changelogs = JsonUtil.fromJsonArray(changelog, PublishedFileInfo.class);
		PublishedFileInfo firstPublishedFile = new PublishedFileInfo();
		firstPublishedFile.setFileId(changeFile1Id); // 文件标识，不是文件夹标识
		firstPublishedFile.setVersion("0.1.0");
		// 该 jsonContent 中包含 \r，需要替换掉
		firstPublishedFile.setMd5sum(DigestUtils.md5Hex(createWidgetJson.replaceAll("\r", "")));
		assertThat(changelogs).hasSize(1).first().usingRecursiveComparison().isEqualTo(firstPublishedFile);
	}
	
	
	private void assertWidget1(String widgetJson) throws JsonProcessingException {
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
	
	private void assertWidget2(String widgetJson) throws JsonProcessingException {
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
	
	// api 仓库的目录结构
	// marketplace/
	//     github.com/
	//         you/
	//             you-repo/
	private Path createApiRepo(MarketplaceStore store) throws IOException {
		Path sourceDirectory = store.getRepoSourceDirectory();
		Files.createDirectories(sourceDirectory);
		GitUtils.init(sourceDirectory, "user", "user@email.com");
		return sourceDirectory;
	}
	
}
