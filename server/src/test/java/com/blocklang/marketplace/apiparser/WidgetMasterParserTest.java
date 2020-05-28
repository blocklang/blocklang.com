package com.blocklang.marketplace.apiparser;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jgit.lib.Constants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.StreamUtils;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.runner.action.PublishedFileInfo;
import com.fasterxml.jackson.databind.JsonNode;

public class WidgetMasterParserTest extends AbstractWidgetParserTest{

	@DisplayName("只读取放在 changelog 文件夹中的 json 文件")
	@Test
	public void run_read_json_files_in_changelog_directory() throws IOException {
		createApiRepo();
		
		// 以下都是无效的 changelog 文件
		// 202001010101.json
		// changelog
		//     202001010102.md
		//     202001010103
		//         202001010104.md

		Path resourceDirectory = store.getRepoSourceDirectory();
		Files.createDirectories(resourceDirectory);
		
		Files.writeString(resourceDirectory.resolve("202001010101.json"), "{}");
		Path changelogDirectory = resourceDirectory.resolve("changelog");
		Files.createDirectory(changelogDirectory);
		Files.writeString(changelogDirectory.resolve("202001010102.md"), "#");
		Path groupDirectory = changelogDirectory.resolve("202001010103");
		Files.createDirectory(groupDirectory);
		Files.writeString(groupDirectory.resolve("202001010104.md"), "#");
		
		GitUtils.add(resourceDirectory, ".");
		GitUtils.commit(resourceDirectory, "jinzw", "email@email.com", "first commit");
		
		MasterParser parser = new WidgetMasterParser(
				Collections.emptyList(),
				store,
				logger);
		assertThat(parser.run()).isEqualTo(ParseResult.ABORT);
	}
	
	@DisplayName("目录名和 changlog 文件名没有遵循 {order}__{description} 规范")
	@Test
	public void run_directory_and_file_name_invalid() throws IOException {
		createApiRepo();
		
		// changelog
		//     202001010101
		//         a.json
		//     b
		//         202001010102.json

		Path resourceDirectory = store.getRepoSourceDirectory();
		Files.createDirectories(resourceDirectory);
		
		Path changelogDirectory = resourceDirectory.resolve("changelog");
		Files.createDirectory(changelogDirectory);
		
		Path group1Directory = changelogDirectory.resolve("202001010101");
		Files.createDirectory(group1Directory);
		Files.writeString(group1Directory.resolve("a.json"), "{}");
		
		Path group2Directory = changelogDirectory.resolve("b");
		Files.createDirectory(group2Directory);
		Files.writeString(group2Directory.resolve("202001010102.json"), "{}");
		
		GitUtils.add(resourceDirectory, ".");
		GitUtils.commit(resourceDirectory, "jinzw", "email@email.com", "first commit");
		
		MasterParser parser = new WidgetMasterParser(
				Collections.emptyList(),
				store,
				logger);
		assertThat(parser.run()).isEqualTo(ParseResult.FAILED);
	}
	
	@DisplayName("目录名中的 {order} 重复")
	@Test
	public void run_directory_id_duplicated() throws IOException {
		createApiRepo();
		
		// changelog
		//     202001010101__a
		//         202001010102.json
		//     202001010101__b
		//         202001010103.json

		Path resourceDirectory = store.getRepoSourceDirectory();
		Files.createDirectories(resourceDirectory);
		
		Path changelogDirectory = resourceDirectory.resolve("changelog");
		Files.createDirectory(changelogDirectory);
		
		Path group1Directory = changelogDirectory.resolve("202001010101__a");
		Files.createDirectory(group1Directory);
		Files.writeString(group1Directory.resolve("202001010102.json"), "{}");
		
		Path group2Directory = changelogDirectory.resolve("202001010101__b");
		Files.createDirectory(group2Directory);
		Files.writeString(group2Directory.resolve("202001010103.json"), "{}");
		
		GitUtils.add(resourceDirectory, ".");
		GitUtils.commit(resourceDirectory, "jinzw", "email@email.com", "first commit");
		
		MasterParser parser = new WidgetMasterParser(
				Collections.emptyList(),
				store,
				logger);
		assertThat(parser.run()).isEqualTo(ParseResult.FAILED);
	}
	
	@DisplayName("同一目录下文件名中的 {order} 重复")
	@Test
	public void run_changelog_file_id_duplicated() throws IOException {
		createApiRepo();
		
		// changelog
		//     202001010101__a
		//         202001010102__b.json
		//         202001010102__c.json

		Path resourceDirectory = store.getRepoSourceDirectory();
		Files.createDirectories(resourceDirectory);
		
		Path changelogDirectory = resourceDirectory.resolve("changelog");
		Files.createDirectory(changelogDirectory);
		
		Path group1Directory = changelogDirectory.resolve("202001010101__a");
		Files.createDirectory(group1Directory);
		Files.writeString(group1Directory.resolve("202001010103__b.json"), "{}");
		Files.writeString(group1Directory.resolve("202001010103__c.json"), "{}");
		
		GitUtils.add(resourceDirectory, ".");
		GitUtils.commit(resourceDirectory, "jinzw", "email@email.com", "first commit");
		
		MasterParser parser = new WidgetMasterParser(
				Collections.emptyList(),
				store,
				logger);
		assertThat(parser.run()).isEqualTo(ParseResult.FAILED);
	}
	
	@DisplayName("一个 changelog 文件发布后，就不能再修改")
	@Test
	public void run_published_changelog_file_was_updated() throws IOException {
		createApiRepo();
		// changelog
		//     202001010101
		//         202001010102.json
		// 202001010102.json 在 0.1.0 版已发布，但在 0.2.0 版修改了内容
		
		String widgetId = "202001010101";
		String changelogFileId = "202001010102";
		
		Path publishedChangelogDir = store.getRepoPackageDirectory()
				.resolve("__changelog__")
				.resolve(widgetId);
		Files.createDirectories(publishedChangelogDir);
		
		Path resourceDirectory = store.getRepoSourceDirectory();
		Files.createDirectories(resourceDirectory);
		Path changelogDirectory = resourceDirectory.resolve("changelog");
		Files.createDirectory(changelogDirectory);
		
		var originChangelogFileContent = "{}";
		Path group1Directory = changelogDirectory.resolve(widgetId);
		Files.createDirectory(group1Directory);
		Files.writeString(group1Directory.resolve(changelogFileId + ".json"), originChangelogFileContent);
		
		var version1 = "v0.1.0";
		GitUtils.add(resourceDirectory, ".");
		GitUtils.commit(resourceDirectory, "jinzw", "email@email.com", "first commit");
		GitUtils.tag(resourceDirectory, version1, "first tag");
		
		var changedChangelogFileContent = "{a}";
		Files.writeString(group1Directory.resolve(changelogFileId + ".json"), changedChangelogFileContent);
		GitUtils.add(resourceDirectory, ".");
		GitUtils.commit(resourceDirectory, "jinzw", "email@email.com", "second commit");
		
		// 在已发布的记录中已记录，在 0.1.0 版本中发布的 changelog 文件
		List<PublishedFileInfo> changelogs = new ArrayList<PublishedFileInfo>();
		PublishedFileInfo published = new PublishedFileInfo();
		published.setFileId(changelogFileId);
		published.setVersion(version1);
		published.setMd5sum(DigestUtils.md5Hex(originChangelogFileContent));
		changelogs.add(published);
		Files.writeString(publishedChangelogDir.resolve("index.json"), JsonUtil.stringify(changelogs));
		
		MasterParser parser = new WidgetMasterParser(
				Collections.singletonList(Constants.R_TAGS + version1),
				store,
				logger);
		assertThat(parser.run()).isEqualTo(ParseResult.FAILED);
	}
	
	@DisplayName("在 master 中成功创建一个 Widget1 部件")
	@Test
	public void run_create_widget_success() throws IOException {
		createApiRepo();
		
		String widget1Id = "202005151645";
		String changeFileId = "202005151646";
		
		Path sourceDirectory = store.getRepoSourceDirectory();
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widget1Id + "__widget1");
		Files.createDirectories(widget1Directory);
		String widget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFileId + "__create_widget.json"), widget1Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		
		MasterParser parser = new WidgetMasterParser(
				Collections.emptyList(),
				store,
				logger);
		parser.setChangeSetSchemaValidator(validator);
		assertThat(parser.run()).isEqualTo(ParseResult.SUCCESS);
		// 将解析结果存在 package/0.1.0/202005151645/index.json 文件中
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
		String widget1Api = Files.readString(store.getPackageVersionDirectory("master").resolve(widget1Id).resolve("index.json"));
		assertWidget1(widget1Api);
		// 断言哪些文件已执行过
		String changelog = Files.readString(store.getPackageChangeLogDirectory().resolve(widget1Id).resolve("index.json"));
		List<PublishedFileInfo> changelogs = JsonUtil.fromJsonArray(changelog, PublishedFileInfo.class);
		PublishedFileInfo firstChangeLog = new PublishedFileInfo();
		firstChangeLog.setFileId(changeFileId); // 文件标识，不是文件夹标识
		firstChangeLog.setVersion("master");
		// 该 jsonContent 中包含 \r，需要替换掉
		firstChangeLog.setMd5sum(DigestUtils.md5Hex(widget1Json.replaceAll("\r", "")));
		assertThat(changelogs).hasSize(1).first().usingRecursiveComparison().isEqualTo(firstChangeLog);
	}
	
	@DisplayName("在 tag 0.1.0 中创建一个 Widget1，在 master 中未修改 Widget1")
	@Test
	public void run_tag_1_create_widget1_master_not_update_widget1() throws IOException {
		createApiRepo();
		
		String widget1Id = "202005151645";
		String changeFileId = "202005151646";
		
		Path sourceDirectory = store.getRepoSourceDirectory();
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widget1Id + "__widget1");
		Files.createDirectories(widget1Directory);
		String widget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFileId + "__create_widget.json"), widget1Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		String version1 = "v0.1.0";
		GitUtils.tag(sourceDirectory, version1, "first tag");
		
		// 任意添加一个文件
		Files.writeString(sourceDirectory.resolve("temp.md"), "#");
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "second commit");
		
		TagParser tagParser = new WidgetTagParser(
				Collections.singletonList(Constants.R_TAGS + version1),
				store,
				logger);
		tagParser.setChangeSetSchemaValidator(validator);
		assertThat(tagParser.run(Constants.R_TAGS + version1)).isEqualTo(ParseResult.SUCCESS);
		
		MasterParser masterParser = new WidgetMasterParser(
				Collections.singletonList(Constants.R_TAGS + version1),
				store,
				logger);
		masterParser.setChangeSetSchemaValidator(validator);
		assertThat(masterParser.run()).isEqualTo(ParseResult.SUCCESS);
		
		// tag v0.1.0
		String widget1Api1 = Files.readString(store.getPackageVersionDirectory("0.1.0").resolve(widget1Id).resolve("index.json"));
		assertWidget1(widget1Api1);
		
		// master
		String widget1Api2 = Files.readString(store.getPackageVersionDirectory("master").resolve(widget1Id).resolve("index.json"));
		assertWidget1(widget1Api2);
		
		// 只执行过一次
		String changelog = Files.readString(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widget1Id).resolve("index.json"));
		List<PublishedFileInfo> changelogs = JsonUtil.fromJsonArray(changelog, PublishedFileInfo.class);
		PublishedFileInfo firstChangeLog = new PublishedFileInfo();
		firstChangeLog.setFileId(changeFileId); // 文件标识，不是文件夹标识
		firstChangeLog.setVersion("0.1.0");
		// 该 jsonContent 中包含 \r，需要替换掉
		firstChangeLog.setMd5sum(DigestUtils.md5Hex(widget1Json.replaceAll("\r", "")));
		assertThat(changelogs).hasSize(1).first().usingRecursiveComparison().isEqualTo(firstChangeLog);
	}
	
	@DisplayName("在 tag 0.1.0 中创建一个 Widget1，在 master 中创建一个 Widget2")
	@Test
	public void run_tag_1_create_widget1_master_create_widget2() throws IOException {
		createApiRepo();
		
		String widget1Id = "202005151645";
		String changeFile1Id = "202005151646";
		
		Path sourceDirectory = store.getRepoSourceDirectory();
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widget1Id + "__widget1");
		Files.createDirectories(widget1Directory);
		String widget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile1Id + "__create_widget.json"), widget1Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		String version1 = "v0.1.0";
		GitUtils.tag(sourceDirectory, version1, "first tag");
		
		String widget2Id = "202005151647";
		String changeFile2Id = "202005151648";
		Path widget2Directory = sourceDirectory.resolve("changelog").resolve(widget2Id + "__widget2");
		Files.createDirectory(widget2Directory);
		String widget2Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget2.json"), Charset.defaultCharset());
		Files.writeString(widget2Directory.resolve(changeFile2Id + "__create_widget.json"), widget2Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "second commit");
		
		TagParser tagParser = new WidgetTagParser(
				Collections.singletonList(Constants.R_TAGS + version1),
				store,
				logger);
		tagParser.setChangeSetSchemaValidator(validator);
		assertThat(tagParser.run(Constants.R_TAGS + version1)).isEqualTo(ParseResult.SUCCESS);
		
		MasterParser masterParser = new WidgetMasterParser(
				Collections.singletonList(Constants.R_TAGS + version1),
				store,
				logger);
		masterParser.setChangeSetSchemaValidator(validator);
		assertThat(masterParser.run()).isEqualTo(ParseResult.SUCCESS);
		
		// tag v0.1.0
		String widget1Api1 = Files.readString(store.getPackageVersionDirectory("0.1.0").resolve(widget1Id).resolve("index.json"));
		assertWidget1(widget1Api1);
		// 断言在 0.1.0 版本中并未创建 widget2
		assertThat(store.getPackageVersionDirectory("0.1.0").resolve(widget2Id).resolve("index.json").toFile().exists());
		
		// master
		String widget1Api2 = Files.readString(store.getPackageVersionDirectory("master").resolve(widget1Id).resolve("index.json"));
		assertWidget1(widget1Api2);
		
		String widget2Api2 = Files.readString(store.getPackageVersionDirectory("master").resolve(widget2Id).resolve("index.json"));
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
		changeLogInfo2.setVersion("master");
		changeLogInfo2.setMd5sum(DigestUtils.md5Hex(widget2Json.replaceAll("\r", ""))); // 该 jsonContent 中包含 \r，需要替换掉
		assertThat(changelogs2).hasSize(1).first().usingRecursiveComparison().isEqualTo(changeLogInfo2);
	}

	@DisplayName("已发布后的 changelog 文件不允许修改")
	@Test
	public void run_master_report_errors_when_changelog_update_after_tag() throws IOException {
		createApiRepo();
		
		String widget1Id = "202005151645";
		String changeFile1Id = "202005151646";
		Path sourceDirectory = store.getRepoSourceDirectory();
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widget1Id + "__widget1");
		Files.createDirectories(widget1Directory);
		String widget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile1Id + "__create_widget.json"), widget1Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		String version1 = "v0.1.0";
		GitUtils.tag(sourceDirectory, version1, "first tag");

		// 使用 __create_widget.json
		String widget2Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget2.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile1Id + "__create_widget.json"), widget2Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "second commit");
		
		TagParser tagParser = new WidgetTagParser(
				Collections.singletonList(Constants.R_TAGS + version1),
				store,
				logger);
		tagParser.setChangeSetSchemaValidator(validator);
		assertThat(tagParser.run(Constants.R_TAGS + version1)).isEqualTo(ParseResult.SUCCESS);
		
		MasterParser masterParser = new WidgetMasterParser(
				Collections.singletonList(Constants.R_TAGS + version1),
				store,
				logger);
		masterParser.setChangeSetSchemaValidator(validator);
		assertThat(masterParser.run()).isEqualTo(ParseResult.FAILED);

		// tag v0.1.0，解析成功
		String widget1Api1 = Files.readString(store.getPackageVersionDirectory("0.1.0").resolve(widget1Id).resolve("index.json"));
		assertWidget1(widget1Api1);
		
		// master，解析失败
		assertThat(store.getPackageVersionDirectory("master").resolve(widget1Id).resolve("index.json").toFile().exists()).isFalse();
		
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
	
	@DisplayName("在一个 widget 目录中有两个 create widget 操作文件")
	@Test
	public void run_create_same_widget_twice() throws IOException {
		createApiRepo();
		
		String widget1Id = "202005151645";
		String changeFile1Id = "202005151646";
		String changeFile2Id = "202005151647";
		
		Path sourceDirectory = store.getRepoSourceDirectory();
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widget1Id + "__widget1");
		Files.createDirectories(widget1Directory);
		String widget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget_no_properties.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile1Id + "__create_widget.json"), widget1Json);
		Files.writeString(widget1Directory.resolve(changeFile2Id + "__create_widget.json"), widget1Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");

		
		MasterParser parser = new WidgetMasterParser(
				Collections.emptyList(),
				store,
				logger);
		parser.setChangeSetSchemaValidator(validator);
		assertThat(parser.run()).isEqualTo(ParseResult.FAILED);
		// 因为创建失败了，所以不会保存下来
		assertThat(store.getPackageVersionDirectory("master").resolve(widget1Id).resolve("index.json").toFile().exists()).isFalse();
		assertThat(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widget1Id).resolve("index.json").toFile().exists()).isFalse();
	}
	
	@DisplayName("在两个文件夹中分别创建了一个 widget，但是 name 相同(widget 名必须唯一)")
	@Test
	public void run_create_widget_name_should_unique() throws IOException {
		createApiRepo();
		
		String widget1Id = "202005151645";
		String widget2Id = "202005151646";
		String changeFile1Id = "202005151647";
		String changeFile2Id = "202005151648";

		Path sourceDirectory = store.getRepoSourceDirectory();
		String widget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget_no_properties.json"), Charset.defaultCharset());
		
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widget1Id + "__widget1");
		Files.createDirectories(widget1Directory);
		Files.writeString(widget1Directory.resolve(changeFile1Id + "__create_widget.json"), widget1Json);
		
		Path widget2Directory = sourceDirectory.resolve("changelog").resolve(widget2Id + "__widget1");
		Files.createDirectories(widget2Directory);
		Files.writeString(widget2Directory.resolve(changeFile2Id + "__create_widget.json"), widget1Json);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		
		MasterParser parser = new WidgetMasterParser(
				Collections.emptyList(),
				store,
				logger);
		parser.setChangeSetSchemaValidator(validator);
		assertThat(parser.run()).isEqualTo(ParseResult.FAILED);
		
		assertThat(store.getPackageVersionDirectory("master").resolve(widget1Id).resolve("index.json").toFile().exists()).isFalse();
		assertThat(store.getPackageVersionDirectory("master").resolve(widget2Id).resolve("index.json").toFile().exists()).isFalse();
		assertThat(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widget1Id).resolve("index.json").toFile().exists()).isFalse();
		assertThat(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widget2Id).resolve("index.json").toFile().exists()).isFalse();
	}
	
	@DisplayName("在一个 changelog 中先 createWidget，然后再 addProperty")
	@Test
	public void run_create_widget_then_add_property_in_one_changelog() throws IOException {
		createApiRepo();
	
		String widget1Id = "202005151645";
		String changeFileId = "202005151646";

		Path sourceDirectory = store.getRepoSourceDirectory();
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widget1Id + "__widget1");
		Files.createDirectories(widget1Directory);
		String widget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1_and_add_property1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFileId + "__create_widget.json"), widget1Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		
		MasterParser parser = new WidgetMasterParser(
				Collections.emptyList(),
				store,
				logger);
		parser.setChangeSetSchemaValidator(validator);
		assertThat(parser.run()).isEqualTo(ParseResult.SUCCESS);
		
		String widget1Api = Files.readString(store.getPackageVersionDirectory("master").resolve(widget1Id).resolve("index.json"));
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
		firstChangeLog.setVersion("master");
		// 该 jsonContent 中包含 \r，需要替换掉
		firstChangeLog.setMd5sum(DigestUtils.md5Hex(widget1Json.replaceAll("\r", "")));
		assertThat(changelogs).hasSize(1).first().usingRecursiveComparison().isEqualTo(firstChangeLog);
	}
	
	@DisplayName("在一个 changelog 中 createWidget，然后在另一个 changlog 中 addProperty")
	@Test
	public void run_create_widget_then_add_property_in_two_changelog() throws IOException {
		createApiRepo();
		
		String widgetId = "202005151645";
		String changeFile1Id = "202005151646";
		String changeFile2Id = "202005151647";

		Path sourceDirectory = store.getRepoSourceDirectory();
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widgetId + "__widget1");
		Files.createDirectories(widget1Directory);
		String createWidget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile1Id + "__create_widget.json"), createWidget1Json);
		
		String addProperty2Json = StreamUtils.copyToString(getClass().getResourceAsStream("add_property2.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile2Id + "__add_property.json"), addProperty2Json);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		
		MasterParser parser = new WidgetMasterParser(
				Collections.emptyList(),
				store,
				logger);
		parser.setChangeSetSchemaValidator(validator);
		assertThat(parser.run()).isEqualTo(ParseResult.SUCCESS);
		
		String widget1Api = Files.readString(store.getPackageVersionDirectory("master").resolve(widgetId).resolve("index.json"));
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
		firstChangeLog.setVersion("master");
		// 该 jsonContent 中包含 \r，需要替换掉
		firstChangeLog.setMd5sum(DigestUtils.md5Hex(createWidget1Json.replaceAll("\r", "")));
		
		PublishedFileInfo secondChangeLog = new PublishedFileInfo();
		secondChangeLog.setFileId(changeFile2Id); // 文件标识，不是文件夹标识
		secondChangeLog.setVersion("master");
		// 该 jsonContent 中包含 \r，需要替换掉
		secondChangeLog.setMd5sum(DigestUtils.md5Hex(addProperty2Json.replaceAll("\r", "")));
		
		assertThat(changelogs).hasSize(2);
		assertThat(changelogs.get(0)).usingRecursiveComparison().isEqualTo(firstChangeLog);
		assertThat(changelogs.get(1)).usingRecursiveComparison().isEqualTo(secondChangeLog);
	}
	
	@DisplayName("通过 addProperty 为 widget 添加一个属性，但此属性已存在")
	@Test
	public void run_add_property_that_already_exist() throws IOException {
		createApiRepo();
	
		String widgetId = "202005151645";
		String changeFile1Id = "202005151646";
		String changeFile2Id = "202005151647";

		Path sourceDirectory = store.getRepoSourceDirectory();
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widgetId + "__widget1");
		Files.createDirectories(widget1Directory);
		String createWidget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1_and_add_property2.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile1Id + "__create_widget.json"), createWidget1Json);
		
		String addProperty2Json = StreamUtils.copyToString(getClass().getResourceAsStream("add_property2.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile2Id + "__add_property.json"), addProperty2Json);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		
		MasterParser parser = new WidgetMasterParser(
				Collections.emptyList(),
				store,
				logger);
		parser.setChangeSetSchemaValidator(validator);
		assertThat(parser.run()).isEqualTo(ParseResult.FAILED);
		
		// createWidget 和 addProperty 两个操作分属两个变更文件，但在同一个 tag 中，
		// 当 addProperty 解析错误后，则也应该回滚（或称为不应用）createWidget 的操作，
		// 即，changelog 日志文件和 widget 的最终结构，这两个文件应该不记录任何内容
		assertThat(store.getPackageVersionDirectory("master").resolve(widgetId).resolve("index.json").toFile().exists()).isFalse();
		assertThat(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widgetId).resolve("index.json").toFile().exists()).isFalse();
	}
	
	@DisplayName("没有 create widget 直接 add properties")
	@Test
	public void run_add_property_but_not_create_widget() throws IOException {
		createApiRepo();
		
		String widgetId = "202005151645";
		String changeFile1Id = "202005151646";
		
		Path sourceDirectory = store.getRepoSourceDirectory();
		Path widgetDirectory = sourceDirectory.resolve("changelog").resolve(widgetId + "__widget1");
		Files.createDirectories(widgetDirectory);
		String addPropertyJson = StreamUtils.copyToString(getClass().getResourceAsStream("add_property2.json"), Charset.defaultCharset());
		Files.writeString(widgetDirectory.resolve(changeFile1Id + "__add_property.json"), addPropertyJson);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		
		MasterParser parser = new WidgetMasterParser(
				Collections.emptyList(),
				store,
				logger);
		parser.setChangeSetSchemaValidator(validator);
		assertThat(parser.run()).isEqualTo(ParseResult.FAILED);
		
		assertThat(store.getPackageVersionDirectory("master").resolve(widgetId).resolve("index.json").toFile().exists()).isFalse();
		assertThat(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widgetId).resolve("index.json").toFile().exists()).isFalse();
	}
	
	@DisplayName("在一个 changelog 文件中，先使用 createWidget 创建一个 Widget，然后使用 addEvent 添加一个事件")
	@Test
	public void run_create_widget_then_add_event_in_one_changelog() throws IOException {
		createApiRepo();
		
		String widget1Id = "202005151645";
		String changeFileId = "202005151646";

		Path sourceDirectory = store.getRepoSourceDirectory();
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widget1Id + "__widget1");
		Files.createDirectories(widget1Directory);
		String widget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1_and_add_event1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFileId + "__create_widget.json"), widget1Json);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		
		MasterParser parser = new WidgetMasterParser(
				Collections.emptyList(),
				store,
				logger);
		parser.setChangeSetSchemaValidator(validator);
		assertThat(parser.run()).isEqualTo(ParseResult.SUCCESS);
		
		String widget1Api = Files.readString(store.getPackageVersionDirectory("master").resolve(widget1Id).resolve("index.json"));
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
		firstChangeLog.setVersion("master");
		// 该 jsonContent 中包含 \r，需要替换掉
		firstChangeLog.setMd5sum(DigestUtils.md5Hex(widget1Json.replaceAll("\r", "")));
		assertThat(changelogs).hasSize(1).first().usingRecursiveComparison().isEqualTo(firstChangeLog);
	}
	
	@DisplayName("在一个 changelog 中 createWidget，然后在另一个 changlog 中 addEvent")
	@Test
	public void run_create_widget_then_add_event_in_two_changelog_success() throws IOException {
		createApiRepo();
		
		String widgetId = "202005151645";
		String changeFile1Id = "202005151646";
		String changeFile2Id = "202005151647";

		Path sourceDirectory = store.getRepoSourceDirectory();
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widgetId + "__widget1");
		Files.createDirectories(widget1Directory);
		String createWidget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile1Id + "__create_widget.json"), createWidget1Json);
		
		String addEvent2Json = StreamUtils.copyToString(getClass().getResourceAsStream("add_event2.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile2Id + "__add_event.json"), addEvent2Json);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		
		MasterParser parser = new WidgetMasterParser(
				Collections.emptyList(),
				store,
				logger);
		parser.setChangeSetSchemaValidator(validator);
		assertThat(parser.run()).isEqualTo(ParseResult.SUCCESS);
		
		String widget1Api = Files.readString(store.getPackageVersionDirectory("master").resolve(widgetId).resolve("index.json"));
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
		firstChangeLog.setVersion("master");
		// 该 jsonContent 中包含 \r，需要替换掉
		firstChangeLog.setMd5sum(DigestUtils.md5Hex(createWidget1Json.replaceAll("\r", "")));
		
		PublishedFileInfo secondChangeLog = new PublishedFileInfo();
		secondChangeLog.setFileId(changeFile2Id); // 文件标识，不是文件夹标识
		secondChangeLog.setVersion("master");
		// 该 jsonContent 中包含 \r，需要替换掉
		secondChangeLog.setMd5sum(DigestUtils.md5Hex(addEvent2Json.replaceAll("\r", "")));
		
		assertThat(changelogs).hasSize(2);
		assertThat(changelogs.get(0)).usingRecursiveComparison().isEqualTo(firstChangeLog);
		assertThat(changelogs.get(1)).usingRecursiveComparison().isEqualTo(secondChangeLog);
	}
	
	@DisplayName("通过 addEvent 为 widget 添加一个事件，而此事件已存在")
	@Test
	public void run_add_event_that_already_exist() throws IOException {
		createApiRepo();
		
		String widgetId = "202005151645";
		String changeFile1Id = "202005151646";
		String changeFile2Id = "202005151647";

		Path sourceDirectory = store.getRepoSourceDirectory();
		Path widget1Directory = sourceDirectory.resolve("changelog").resolve(widgetId + "__widget1");
		Files.createDirectories(widget1Directory);
		String createWidget1Json = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile1Id + "__create_widget.json"), createWidget1Json);
		
		String addEvent1Json = StreamUtils.copyToString(getClass().getResourceAsStream("add_event1.json"), Charset.defaultCharset());
		Files.writeString(widget1Directory.resolve(changeFile2Id + "__add_event.json"), addEvent1Json);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		
		MasterParser parser = new WidgetMasterParser(
				Collections.emptyList(),
				store,
				logger);
		parser.setChangeSetSchemaValidator(validator);
		assertThat(parser.run()).isEqualTo(ParseResult.FAILED);
		
		// createWidget 和 addProperty 两个操作分属两个变更文件，但在同一个 tag 中，
		// 当 addProperty 解析错误后，则也应该回滚（或称为不应用）createWidget 的操作，
		// 即，changelog 日志文件和 widget 的最终结构，这两个文件应该不记录任何内容
		assertThat(store.getPackageVersionDirectory("master").resolve(widgetId).resolve("index.json").toFile().exists()).isFalse();
		assertThat(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widgetId).resolve("index.json").toFile().exists()).isFalse();
	}
	
	@DisplayName("没有 create widget 直接 add event")
	@Test
	public void run_add_event_but_not_create_widget() throws IOException {
		createApiRepo();
		
		String widgetId = "202005151645";
		String changeFile1Id = "202005151646";
		
		Path sourceDirectory = store.getRepoSourceDirectory();
		Path widgetDirectory = sourceDirectory.resolve("changelog").resolve(widgetId + "__widget1");
		Files.createDirectories(widgetDirectory);
		String addEvent1Json = StreamUtils.copyToString(getClass().getResourceAsStream("add_event1.json"), Charset.defaultCharset());
		Files.writeString(widgetDirectory.resolve(changeFile1Id + "__add_event.json"), addEvent1Json);
		
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		
		MasterParser parser = new WidgetMasterParser(
				Collections.emptyList(),
				store,
				logger);
		parser.setChangeSetSchemaValidator(validator);
		assertThat(parser.run()).isEqualTo(ParseResult.FAILED);
		
		assertThat(store.getPackageVersionDirectory("master").resolve(widgetId).resolve("index.json").toFile().exists()).isFalse();
		assertThat(store.getRepoPackageDirectory().resolve("__changelog__").resolve(widgetId).resolve("index.json").toFile().exists()).isFalse();
	}
	
	@DisplayName("每次解析时都要重新解析 master 分支")
	@Test
	public void run_master_always_reparse() throws IOException {
		createApiRepo();
		
		String widgetId = "202005151645";
		String changeFile1Id = "202005151646";
		
		Path sourceDirectory = store.getRepoSourceDirectory();
		
		Path widgetDirectory = sourceDirectory.resolve("changelog").resolve(widgetId + "__widget1");
		Files.createDirectories(widgetDirectory);
		String createWidgetJson = StreamUtils.copyToString(getClass().getResourceAsStream("create_widget1.json"), Charset.defaultCharset());
		Files.writeString(widgetDirectory.resolve(changeFile1Id + "__create_widget.json"), createWidgetJson);
		GitUtils.add(sourceDirectory, ".");
		GitUtils.commit(sourceDirectory, "user", "user@email.com", "first commit");
		
		MasterParser parser = new WidgetMasterParser(
				Collections.emptyList(),
				store,
				logger);
		parser.setChangeSetSchemaValidator(validator);
		assertThat(parser.run()).isEqualTo(ParseResult.SUCCESS);
		
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
		
		assertThat(parser.run()).isEqualTo(ParseResult.SUCCESS);
		
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
}
