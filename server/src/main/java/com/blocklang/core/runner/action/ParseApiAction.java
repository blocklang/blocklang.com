package com.blocklang.core.runner.action;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.StreamUtils;

import com.blocklang.core.git.GitBlobInfo;
import com.blocklang.core.git.GitFileInfo;
import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.AbstractAction;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.data.changelog.AddWidgetEvent;
import com.blocklang.marketplace.data.changelog.AddWidgetProperty;
import com.blocklang.marketplace.data.changelog.Change;
import com.blocklang.marketplace.data.changelog.Widget;
import com.blocklang.marketplace.data.changelog.WidgetEvent;
import com.blocklang.marketplace.data.changelog.WidgetProperty;
import com.blocklang.marketplace.task.CodeGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;

/**
 * 逐个版本解析 Widget 的 API 仓库，并将解析后的结果存储在文件系统中。
 * 
 * <ul>
 * <li> 准备要解析的 tag 和分支：获取所有 tag 和 master 分支信息
 * <li> 切换到一个版本后，获取所有 change-set json 文件
 * <li> 获取 Widget 列表
 * <li> 逐个 Widget 的解析 change-set json 文件
 * <li> 校验 json schema
 * <li> 保存解析结果
 * </ul>
 * 
 * 注意：此类用于解析所有类型的 API，然后在 run 方法中执行不同的解析操作
 * 
 * <pre>
 * inputs
 *     tags      - string[]，git tag 列表
 *     master    - boolean，是否解析 master 分支，默认为 true
 * outputs
 * 
 * </pre>
 * 
 * @author Zhengwei Jin
 *
 */
public class ParseApiAction extends AbstractAction {
	
	private List<String> tags = Collections.emptyList();
	private boolean master = true;
	private String apiChangelogSchema;
	
	// FIXME: 临时实现，确保当前 action 的功能跑通
	public static final String INPUT_TAGS = "tags";
	public static final String INPUT_MASTER = "master";

	private MarketplaceStore store;
	private boolean success = true;
	
	public ParseApiAction(ExecutionContext context) {
		super(context);
		
		List<String> inputTags = context.getValue(INPUT_TAGS, List.class);
		if(inputTags != null) {
			tags = inputTags;
		}
		Boolean inputMaster = context.getValue(INPUT_MASTER, Boolean.class);
		if(inputMaster != null) {
			master = inputMaster;
		}
		this.store = context.getValue(ExecutionContext.MARKETPLACE_STORE, MarketplaceStore.class);
	}

	@Override
	public Optional<?> run() {
		if(tags.isEmpty()) {
			logger.info("git 仓库中没有标注 tag");
		} else {
			tags.forEach(tag -> GitUtils.getVersionFromRefName(tag).ifPresentOrElse(version -> parseTag(tag, version), () -> ignoreParse(tag)));
		}
		
		return success ? Optional.of(true) : Optional.empty();
	}

	private void ignoreParse(String tag) {
		logger.info("从 tag {0} 中未解析出语义版本号，忽略解析此 tag", tag);
	}

	private void parseTag(String tag, String version) {
		logger.info("开始解析 v{0}", version);
		
		// 确认 tag 是否已解析过，如果解析过，则不再解析
		// 如果文件夹存在，则视作已解析过
		Path packageVersionDirectory = store.getPackageVersionDirectory(version);
		if(packageVersionDirectory.toFile().exists()) {
			logger.info("已解析过 v{0}，不再解析", version);
			return;
		}
		
		// 不 checkout，直接从 git 仓库中读取 tag 下的所有 json 文件
		List<GitFileInfo> allJsonFile = GitUtils
				.getAllFilesFromTag(store.getRepoSourceDirectory(), tag, ".json")
				.stream()
				// 将没有存在 changelog 中的 json 文件排除掉
				// 约定所有变更都存在项目根目录下的 changelog 文件夹中
				.filter(gitFileInfo -> gitFileInfo.getPath().startsWith("changelog"))
				.collect(Collectors.toList());
		
		// TODO: 在此处校验分组和 widget 的命名规范
		// 格式为 {order}_{description}，或者 {order}__，或者 {order}
		// order 必须为时间戳
		// 并且一个文件夹下不会重复
		
		// 开始逐个 widget 解析，一个 widget 下包含多个日志文件，要先根据文件名进行排序
		// 然后按顺序，累加解析
		// 确保 widget 的排序不变，所以使用 LinkedHashMap
		LinkedHashMap<String, List<GitBlobInfo>> groupedBlobs = GitUtils.loadDataFromTag(store.getRepoSourceDirectory(), tag, allJsonFile)
			.stream()
			.collect(Collectors.groupingBy(jsonFile -> jsonFile.getPath().split("/")[1], LinkedHashMap::new, Collectors.toList()));
		
		try {
			// FIXME: 该异常只能出现在开发阶段，在运行时不允许出现，所以此处不建议处理此异常
			loadApiChangelogSchema();
		} catch (IOException e) {
			logger.error(e);
			return;
		}
		
		// 注意，如果遇到 json 文件格式有误或者 schema 校验未通过，则不能退出校验，而是继续校验下一个文件
		// 要校验出所有的错误，便于用于一次修改完成。
		List<String> invalidJsonFile = new ArrayList<String>(); // 也可改为 Boolean 值
		groupedBlobs.forEach((groupName, jsonFiles) -> {
			// 目录名由两部分组成，如 `202005151827_button`，前半部分是时间戳，是不允许改变的，后半部分是 widget 名，是可以重命名的
			// 确保 jsonFiles 是按时间戳先后排序的
			
			// FIXME: 校验同一个文件夹中的时间戳不能重复。
			
			jsonFiles.sort(Comparator.comparing(GitFileInfo::getName));
			jsonFiles.forEach(jsonFile -> {
				// 校验文件
				JsonNode jsonContent = null;
				try {
					jsonContent = JsonUtil.readTree(jsonFile.getContent());
				} catch (JsonProcessingException e) {
					invalidJsonFile.add(jsonFile.getPath());
					logger.error("{0} 文件中的 json 无效", jsonFile.getName());
					logger.error(e);
				}
				
				if(jsonContent != null) {
					Set<ValidationMessage> errors = JsonUtil.validate(jsonContent, this.apiChangelogSchema);
					if(!errors.isEmpty()) {
						invalidJsonFile.add(jsonFile.getPath());
						errors.forEach(error -> logger.error(error.getMessage()));
					}
				}
			});
		});
		// 校验通过后，应用增量变更
		if(!invalidJsonFile.isEmpty()) {
			return;
		}
		
		List<Widget> allWidgets = new ArrayList<Widget>();
		Map<String, List<ChangeLogInfo>> allChangeLogs = new HashMap<String, List<ChangeLogInfo>>();
		Path appliedChangelogDirectory = store.getRepoPackageDirectory().resolve("__changelog__");
		
		CodeGenerator widgetCodeGen = new CodeGenerator(null);
		groupedBlobs.forEach((groupName, jsonFiles) -> {
			List<ChangeLogInfo> changeLogs = new ArrayList<>();
			String widgetId = groupName.split("__")[0];
			// 在上一个版本的基础上增量安装
			// 首次安装
			
			// changes 中会包含多个变更操作，所以不仅仅是在版本之间，在一个变更文件的多个变更操作之间也需要增量操作
			// 如果是第一个分支，则不需要往前追溯
			// 要在每一个 tag 中都记录哪些 changelog 已执行过
			// 或者在一个文件中记录所有执行过的 changelog，并与 tag 关联起来
			// 如使用如下 json 数组记录，并存在 package/__changelog__/{widget}/changlog.json
			// [{version: "0.1.0", fileId: "202005161800", md5sum: "xxxdswewe"}]
			
			// 获取 changelog 文件
			Path widgetChangeLogPath = appliedChangelogDirectory.resolve(widgetId).resolve("index.json");
			
			try {
				String widgetChangeLog = Files.readString(widgetChangeLogPath);
				changeLogs.addAll(JsonUtil.fromJsonArray(widgetChangeLog, ChangeLogInfo.class));
			} catch (IOException e1) {
				// 如果文件不存在，则模式使用空 List
			}
			
			// 应先将所有文件层面的有效性校验完之后再处理
			
			// 校验已发布的文件是否被修改过
			boolean hasPublishedChangeLogUpdated = false;
			for(GitBlobInfo jsonFile : jsonFiles) {
				String jsonFileId = jsonFile.getName().split("__")[0];
				// 判断该文件是否已执行过
				Optional<ChangeLogInfo> changeLogInfoOption = changeLogs
						.stream()
						.filter(changeLog -> changeLog.getFileId().equals(jsonFileId))
						.findFirst();
				
				if(changeLogInfoOption.isPresent()) {
					// 如果已执行过，则跳过
					
					// 如果已执行过，但文件内容已修改，则给出错误提示
					String md5sumPublished = changeLogInfoOption.get().getMd5sum();
					String md5sumNow = DigestUtils.md5Hex(jsonFile.getContent());
					if(!md5sumPublished.equals(md5sumNow)) {
						logger.error("{0}/{1} 已被修改，已应用版本的 checksum 为 {2}，但当前版本的 checksum 为 {3}", 
								groupName, jsonFile.getName(), md5sumPublished, md5sumNow);
						hasPublishedChangeLogUpdated = true;
					}
				}
			}
			
			// 如果已发布的文件被修改过，则不再执行后续校验
			if(hasPublishedChangeLogUpdated) {
				success = false;
				return;
			}
			
			// 开始增量应用变更操作，每一步变更都要输出结果，先从零开始。
			// 需要定义实体类
			// 如果变更文件已执行过，则不再执行
			// 校验变更文件的内容是否已修改，如果修改，则给出提示
			Widget result = loadPreviousWidget(tag, widgetId);
			
			boolean anyOperatorsInValid = false;
			// 开始解析 changelog
			for(GitBlobInfo jsonFile : jsonFiles) {
				String widgetCode = widgetCodeGen.next();
				
				// 一次处理一个文件中的变更，而不是将所有所有文件中的变更
				List<Change> changes = new ArrayList<>();
				
				String jsonFileId = jsonFile.getName().split("__")[0];
				// 判断该文件是否已执行过
				Optional<ChangeLogInfo> changeLogInfoOption = changeLogs
						.stream()
						.filter(changeLog -> changeLog.getFileId().equals(jsonFileId))
						.findFirst();
				if(changeLogInfoOption.isPresent()) {
					break;
				}
				
				// 从 json 中获取 changes 列表中的内容，并转换为对应的操作，先实现 createWidget 操作
				try {
					JsonNode jsonNode = JsonUtil.readTree(jsonFile.getContent());
					JsonNode changeNodes = jsonNode.get("changes");
					for(JsonNode operator : changeNodes) {
						if(operator.get("createWidget") != null) {
							Widget widget = JsonUtil.treeToValue(operator.get("createWidget"), Widget.class);
							Change change = new Change("createWidget", widget);
							changes.add(change);
						} else if (operator.get("addProperty") != null) {
							AddWidgetProperty addWidgetProperty = JsonUtil.treeToValue(operator.get("addProperty"), AddWidgetProperty.class);
							// 约定 property 按照 code 排序，这样就可以取最后一个 property 的 code
							
							// 并不需要 widgetName 指定引用关系，因为放在 widget 目录下的操作，都是针对 widget 的
							// 新增的属性列表中，只要有一个属性名被占用，就不能应用变更
							Change change = new Change("addProperty", addWidgetProperty);
							changes.add(change);
						} else if (operator.get("addEvent") != null) {
							AddWidgetEvent addWidgetEvent = JsonUtil.treeToValue(operator.get("addEvent"), AddWidgetEvent.class);
							Change change = new Change("addEvent", addWidgetEvent);
							changes.add(change); //  TOOD: change.add(op, data);
						}
					}
				} catch (JsonProcessingException e) {
					// do nothing
				}
					
				// 注意：要将整个仓库校验一遍。
				// 因为是增量日志，所以只能边应用变更边校验
				
				// 校验文件中的所有操作，如果有一个操作未通过校验，不中断，仍然继续校验后续的操作
				// 但要利用此标识，不再应用这些变更。
				
				for(Change change : changes) {
					if(change.getOperator().equals("createWidget")) {
						// 如果该 widget 已经存在，则显示错误信息
						Widget data = change.getData(Widget.class);
						
						// 校验 widget 名是否被占用
						if(allWidgets.stream().anyMatch(widget -> widget.getName().equalsIgnoreCase(data.getName()))) {
							logger.error("Widget.name {0} 已经被占用，请更换");
							anyOperatorsInValid = true;
							break;
						}
						
						if(result == null) {
							data.setCode(widgetCode);
							// 为新建的属性设置 code
							CodeGenerator propertiesCodeGen = new CodeGenerator(null);
							data.getProperties().forEach(prop -> prop.setCode(propertiesCodeGen.next()));
							data.getEvents().forEach(event -> event.setCode(propertiesCodeGen.next()));
							
							result = data;
						} else {
							logger.error("{0} 已创建过，不能重复创建", data.getName());
							anyOperatorsInValid = true;
							break;
						}
					} else if(change.getOperator().equals("addProperty")){
						if(result == null) {
							// 如果尚未创建 widget，则给出错误信息
							logger.error("无法执行 addProperty 操作，因为尚未创建 Widget");
							anyOperatorsInValid = true;
							break;
						}
						// 需要先判断属性是否已经存在，如果已存在，则给出错误提示
						
						AddWidgetProperty addWidgetProperty = change.getData(AddWidgetProperty.class);
						// 约定 property 按照 code 排序，这样就可以取最后一个 property 的 code
						List<WidgetProperty> existProperties = result.getProperties();
						
						List<WidgetProperty> addWidgetProperties = addWidgetProperty.getProperties();
						for(WidgetProperty added : addWidgetProperties) {
							// 如果属性名已被占用
							if(existProperties.stream().anyMatch(prop -> prop.getName().equals(added.getName()))) {
								logger.error("属性名 {0} 已存在", added.getName());
								success = false;
								anyOperatorsInValid = true;
							}
						}
						
						// 新增的属性列表中，只要有一个属性名被占用，就不能应用变更
						if(!anyOperatorsInValid) {
							// 要从 properties 和 events 中找出最大值
							String seed = getMaxPropertyCode(result);
							CodeGenerator codeGen = new CodeGenerator(seed);
							addWidgetProperty.getProperties().forEach(prop -> prop.setCode(codeGen.next()));
							
							existProperties.addAll(addWidgetProperty.getProperties());
						}
					} else if(change.getOperator().equals("addEvent")) {
						if(result == null) {
							// 如果尚未创建 widget，则给出错误信息
							logger.error("无法执行 addEvent 操作，因为尚未创建 Widget");
							anyOperatorsInValid = true;
							break;
						}
						
						AddWidgetEvent addWidgetEvent = change.getData(AddWidgetEvent.class);
						List<WidgetEvent> existEvents = result.getEvents();
						
						List<WidgetEvent> addWidgetEvents = addWidgetEvent.getEvents();
						for(WidgetEvent added : addWidgetEvents) {
							// 如果事件名已被占用
							if(existEvents.stream().anyMatch(event -> event.getName().equals(added.getName()))) {
								logger.error("事件名 {0} 已存在", added.getName());
								success = false;
								anyOperatorsInValid = true;
							}
						}
						
						if(!anyOperatorsInValid) {
							String seed = getMaxPropertyCode(result);
							CodeGenerator codeGen = new CodeGenerator(seed);
							addWidgetEvent.getEvents().forEach(event -> event.setCode(codeGen.next()));
							result.getEvents().addAll(addWidgetEvent.getEvents());							
						}
					}
				}
				if(!anyOperatorsInValid) {
					// 执行完成后，在 changelog 中追加记录
					ChangeLogInfo changeLog = new ChangeLogInfo();
					changeLog.setFileId(jsonFileId);
					changeLog.setVersion(version);
					changeLog.setMd5sum(DigestUtils.md5Hex(jsonFile.getContent()));
					changeLogs.add(changeLog);
				}
			}
			
			if(anyOperatorsInValid) {
				success = false;
				return;
			}
			
			
			// 更新 changelog 文件
			
			
			// TODO: 完善逻辑，前面已经出错了，是否还需要执行后面的操作。
			// 该功能实现完之后，如何找到合理的关节来重构呢？
			
			// 往文件系统保存 result
			if(result != null) {
				result.setId(widgetId);
				allWidgets.add(result);
				
				allChangeLogs.put(widgetId, changeLogs);
				
			}
		});
		
		// 在文件系统中保存所有的 widget 结构
		if(!success) {
			return;
		}
		allWidgets.forEach(widget -> {
			Path widgetPath = packageVersionDirectory.resolve(widget.getId());
			try {
				Files.createDirectories(widgetPath);
				Files.writeString(widgetPath.resolve("index.json"), JsonUtil.stringify(widget));
			} catch (IOException e) {
				logger.error(e);
				success = false;
			}
		});
		allChangeLogs.forEach((widgetId, changelogs) -> {
			Path widgetChangelogDirectory = appliedChangelogDirectory.resolve(widgetId);
			try {
				Files.createDirectories(widgetChangelogDirectory);
				Files.writeString(widgetChangelogDirectory.resolve("index.json"), JsonUtil.stringify(changelogs));
			} catch (IOException e) {
				logger.error(e);
				success = false;
			}
		});

	}

	/**
	 * Widget 中的属性和事件使用相同的编码序列，将事件看作一种特殊的属性
	 * 
	 * @param widget Widget
	 * @return 下一个属性编码
	 */
	private String getMaxPropertyCode(Widget widget) {
		List<WidgetProperty> properties = widget.getProperties();
		List<WidgetEvent> events = widget.getEvents();
		
		String propertyMaxSeed = properties.isEmpty() ? "0001" : properties.get(properties.size() - 1).getCode();
		String eventMaxSeed = events.isEmpty() ? "0001" : events.get(events.size() - 1).getCode();
		return propertyMaxSeed.compareTo(eventMaxSeed) >= 0 ? propertyMaxSeed : eventMaxSeed;
	}

	private void loadApiChangelogSchema() throws IOException {
		if(this.apiChangelogSchema == null) {
			this.apiChangelogSchema = StreamUtils.copyToString(getClass().getResourceAsStream("api_change_set_schema.json"), Charset.defaultCharset());
		}
	}

	/**
	 * 获取上一个 tag 中发布的 Widget
	 * 
	 * @param tag git tag name
	 * @param widgetId widget 标识，取文件名 202005161723_button 中的时间戳
	 * @return 获取上一个 tag 中发布的 Widget，如果不存在上一个 tag，则返回 <code>null</code>
	 */
	private Widget loadPreviousWidget(String tag, String widgetId) {
		int index = tags.indexOf(tag);
		
		if(index <= 0) {
			return null;
		}
		
		String preVersion = GitUtils.getVersionFromRefName(tags.get(index - 1)).get();
		
		Path widgetPath = store.getPackageVersionDirectory(preVersion).resolve(widgetId).resolve("index.json");
		try {
			String content = Files.readString(widgetPath);
			return JsonUtil.fromJsonObject(content, Widget.class);
		} catch (IOException e) {
			// 如果组件是在该 tag 中新增的，则获取不到上一版本的 idnex.json 属于正常情况
		}
		return null;
	}

}
