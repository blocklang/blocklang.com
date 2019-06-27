package com.blocklang.marketplace.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.URIish;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.git.GitUtils;
import com.blocklang.core.git.exception.GitTagFailedException;
import com.blocklang.core.service.PropertyService;
import com.blocklang.marketplace.constant.Language;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.dao.ApiChangelogDao;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoPublishTaskDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.data.ApiJson;
import com.blocklang.marketplace.data.ComponentJson;
import com.blocklang.marketplace.data.changelog.ChangeLog;
import com.blocklang.marketplace.model.ApiChangelog;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.model.ComponentRepoVersion;
import com.blocklang.marketplace.service.PublishService;
import com.blocklang.marketplace.task.ApiJsonFetchTask;
import com.blocklang.marketplace.task.ApiRepoFindTagTask;
import com.blocklang.marketplace.task.ChangelogParseTask;
import com.blocklang.marketplace.task.ComponentJsonFetchTask;
import com.blocklang.marketplace.task.ComponentRepoLatestTagFetchTask;
import com.blocklang.marketplace.task.GitSyncApiRepoTask;
import com.blocklang.marketplace.task.GitSyncComponentRepoTask;
import com.blocklang.marketplace.task.MarketplacePublishContext;
import com.blocklang.marketplace.task.TaskLogger;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.skuzzle.semantic.Version;

@Service
public class PublishServiceImpl implements PublishService {

	@Autowired
	private PropertyService propertyService;
	@Autowired
	private ComponentRepoPublishTaskDao componentRepoPublishTaskDao;
	@Autowired
	private ComponentRepoDao componentRepoDao;
	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	@Autowired
	private ApiChangelogDao apiChangelogDao;
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	
	@Async
	@Override
	public void asyncPublish(ComponentRepoPublishTask publishTask) {
		this.publish(publishTask);
	}

	// TODO: 先实现功能，再重构。
	@Override
	public void publish(ComponentRepoPublishTask publishTask) {
		StopWatch stopWatch = StopWatch.createStarted();

		String dataRootPath = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).get();
		MarketplacePublishContext context = new MarketplacePublishContext(dataRootPath, publishTask.getGitUrl());
		// 确保全程使用同一个 logger
		Path logFile = context.getRepoPublishLogFile();
		publishTask.setLogFileName(logFile.getFileName().toString());
		componentRepoPublishTaskDao.save(publishTask);
		
		TaskLogger logger = new TaskLogger(logFile);
		// 以下三行是设置 websocket 消息的参数
		// 设置日志组件，支持向远程发送消息
		// TODO: 以下接口是否可以简化？
		logger.setSendMessage(true);
		logger.setMessagingTemplate(messagingTemplate);
		logger.setTaskId(publishTask.getId());
		
		logger.info(StringUtils.repeat("=", 60));
		logger.info("开始发布 @{0}/{1} 组件库", 
				context.getLocalComponentRepoPath().getOwner(), 
				context.getLocalComponentRepoPath().getRepoName());
		
		boolean success = true;

		// 从源代码托管网站下载组件的源代码
		logger.info(StringUtils.repeat("-", 45));
		logger.info("一、开始获取组件库源码");
		GitSyncComponentRepoTask componentRepoTask = new GitSyncComponentRepoTask(context);
		Optional<Boolean> gitSyncOption = componentRepoTask.run();
		success = gitSyncOption.isPresent();
		if(success) {
			logger.info("完成");
		} else {
			logger.error("失败");
		}
		
		String refName = null;
		String tagName = null;
		if(success) {
			logger.info(StringUtils.repeat("-", 45));
			logger.info("二、开始获取最新的 Git Tag");
			ComponentRepoLatestTagFetchTask gitTagFetchTask = new ComponentRepoLatestTagFetchTask(context);
			Optional<Ref> gitTagFetchTaskOption = gitTagFetchTask.run();
			success = gitTagFetchTaskOption.isPresent();
			
			if(success) {
				refName = gitTagFetchTaskOption.get().getName();
				tagName = refName.substring(Constants.R_TAGS.length());
				logger.info("完成，最新的 git tag 版本为 {0}", tagName);
			} else {
				logger.error("在该仓库中没有找到 git tag，请为仓库标注 tag 后再重试");
			}
		}
		
		// 从最新的 git tag 中查找 component.json 文件
		String componentJsonContent = null;
		if(success) {
			logger.info(StringUtils.repeat("-", 45));
			logger.info("三、校验仓库根目录下的 component.json 文件");
			
			logger.info("在 Git Tag {0} 的根目录下查找 component.json 文件", tagName);
			
			ComponentJsonFetchTask task = new ComponentJsonFetchTask(context, refName);
			Optional<String> contentOption = task.run();
			success = contentOption.isPresent();
			
			if(success) {
				componentJsonContent = contentOption.get();
				logger.info("存在 component.json 文件");
			} else {
				logger.error("没有找到 component.json 文件");
			}
		}
		
		// 将 json 字符串转换为 java 对象
		ComponentJson componentJson = null;
		if(success) {
			logger.info("将 component.json 内容转换为 java 对象");
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				componentJson = objectMapper.readValue(componentJsonContent, ComponentJson.class);
				logger.info("转换完成");
				success = true;
			} catch (IOException e) {
				logger.error("转换失败");
				logger.error(e);
				success = false;
			}
		}
		
		// 校验 component.json 中的字段
		// name
		// 1. 不能为空
		// 2. 长度不能超过64个字节
		// 3. 只能包含字母、数字、中划线和下划线
		// 4. 同一个发布者没有发不过此名称的组件库
		// version
		// 1. 不能为空
		// 2. 必须是有效的语义化版本
		// 3. 必须大于最新的版本号
		// category
		// 1. 不能为空
		// 2. 只能是 “Widget”（不区分大小写）
		// language
		// 1. 不能为空
		// 2. 只能是“Typescript”、“Java”（不区分大小写）
		// description
		// 1. 长度不能超过512个字节
		// icon
		// 1. 长度不能超过64个字节
		// api.git
		// 1. 不能为空
		// 2. 有效的 https 协议的 git 远程仓库地址
		// 3. 根据此地址能找到远程仓库
		// api.version
		// 1. 不能为空
		// 2. 有效的语义化版本号
		
		if(success) {
			// name
			boolean nameHasError = false;
			String name = componentJson.getName();
			if(StringUtils.isBlank(name)) {
				logger.error("name - 值不能为空");
				nameHasError = true;
			}
			String trimedName = Objects.toString(name, "").trim();
			if(!nameHasError) {
				// 之所以取 60 而并不是 64，因为 60 好记
				if(com.blocklang.core.util.StringUtils.byteLength(trimedName) > 60) {
					logger.error("name - 值的长度不能超过60个字节(一个汉字占两个字节)");
					nameHasError = true;
				}
			}
			if(!nameHasError) {
				//校验：只支持英文字母、数字、中划线(-)、下划线(_)、点(.)
				String regEx = "^[a-zA-Z0-9\\-\\w]+$";
				Pattern pattern = Pattern.compile(regEx);
				Matcher matcher = pattern.matcher(trimedName);
				if(!matcher.matches()) {
					logger.error("name - 值只支持英文字母、数字、中划线(-)、下划线(_)、点(.)，‘{0}’中包含非法字符", trimedName);
					nameHasError = true;
				}
			}
			if(!nameHasError) {
				if(componentRepoDao.findByNameAndCreateUserId(trimedName, publishTask.getCreateUserId()).isPresent()) {
					logger.error("name - {0} 下已注册名为 {1} 的组件库，请换一个名字", publishTask.getCreateUserName(), trimedName);
					nameHasError = true;
				}
			}
			
			// version
			boolean versionHasError = false;
			String version = componentJson.getVersion();
			if(StringUtils.isBlank(version)) {
				logger.error("version - 值不能为空");
				versionHasError = true;
			}
			String trimedVersion = Objects.toString(version, "").trim();
			if(!versionHasError) {
				if(!Version.isValidVersion(trimedVersion)) {
					logger.error("version - 值 {0} 不是有效的语义化版本", trimedVersion);
					versionHasError = true;
				}
			}
			if(!versionHasError) {
				// 获取最新发布的版本号
				Optional<ComponentRepo> compRepoOption = componentRepoDao.findByNameAndCreateUserId(trimedName, publishTask.getCreateUserId());
				if(compRepoOption.isPresent()) {
					Version previousVersion = Version.parseVersion(compRepoOption.get().getVersion(), true);
					Version currentVersion = Version.parseVersion(trimedVersion, true);
					if(!currentVersion.isGreaterThan(previousVersion)) {
						logger.error("version - 版本号应大于项目最新的版本号，但 {} 没有大于上一个版本号 {}", trimedVersion, compRepoOption.get().getVersion());
						versionHasError = true;
					}
				}
			}
			
			// category
			boolean categoryHasError = false;
			String category = componentJson.getCategory();
			if(StringUtils.isBlank(category)) {
				logger.error("category - 值不能为空");
				categoryHasError = true;
			}
			if(!categoryHasError) {
				if(!category.trim().equalsIgnoreCase("Widget")) {
					logger.error("category - 值只能是 Widget");
					categoryHasError = true;
				}
			}
			
			// language
			boolean languageHasError = false;
			String language = componentJson.getLanguage();
			if(StringUtils.isBlank(language)) {
				logger.error("language - 值不能为空");
				languageHasError = true;
			}
			
			if(!languageHasError) {
				String trimedLanguage = language.trim();
				if(!trimedLanguage.equalsIgnoreCase("Java") && !trimedLanguage.equalsIgnoreCase("TypeScript")) {
					logger.error("language - 值只能是 Java 或 TypeScript");
					languageHasError = true;
				}
			}
			
			// description
			boolean descriptionHasError = false;
			String description = componentJson.getDescription();
			if(com.blocklang.core.util.StringUtils.byteLength(description) > 500) {
				logger.error("description - 值的长度不能超过500个字节(一个汉字占两个字节)");
				descriptionHasError = true;
			}
			
			// icon
			boolean iconHasError = false;
			String icon = componentJson.getIcon();
			if(com.blocklang.core.util.StringUtils.byteLength(icon) > 60) {
				logger.error("icon - 值的长度不能超过60个字节(一个汉字占两个字节)");
				iconHasError = true;
			}
			
			// api.git
			boolean apiGitHasError = false;
			String apiGit = componentJson.getApi().getGit();
			if(StringUtils.isBlank(apiGit)) {
				logger.error("api.git - 值不能为空，一个组件库必须要实现一个 API");
				apiGitHasError = true;
			}
			if(!apiGitHasError) {
				String gitUrl = apiGit.trim();
				
				URIish uriish = null;
				try {
					uriish = new URIish(gitUrl);
				} catch (URISyntaxException e) {
					logger.error("api.git - Git 仓库地址的无效，不是有效的远程仓库地址");
					logger.error(e);
					apiGitHasError = true;
				}
				if(!apiGitHasError && !uriish.isRemote()) {
					logger.error("api.git - Git 仓库地址的无效，不是有效的远程仓库地址");
					apiGitHasError = true;
				}
				
				if(!apiGitHasError && !"https".equalsIgnoreCase(uriish.getScheme())) {
					logger.error("api.git - Git 仓库地址无效，请使用 HTTPS 协议的 git 仓库地址");
					apiGitHasError = true;
				}
				
				if(!apiGitHasError && !GitUtils.isValidRemoteRepository(gitUrl)) {
					logger.error("api.git - Git 仓库地址无效，该仓库不存在");
					apiGitHasError = true;
				}
			}

			// api.version
			boolean apiVersionHasError = false;
			String apiVersion = componentJson.getApi().getVersion();
			if(StringUtils.isBlank(apiVersion)) {
				logger.error("api.version - 值不能为空");
				apiVersionHasError = true;
			}
			if(!apiVersionHasError) {
				String trimedApiVersion = apiVersion.trim();
				if(!Version.isValidVersion(trimedApiVersion)) {
					logger.error("api.version - 值 {0} 不是有效的语义化版本", trimedApiVersion);
					apiVersionHasError = true;
				}
			}
			// 注意：api 是通过组件引用的，所以 api 的版本号不一定是最新版的
			// 后续要校验指定的 api 版本是否存在于 api 项目中

			success = !(nameHasError || versionHasError || categoryHasError || languageHasError || descriptionHasError
					|| iconHasError || apiGitHasError || apiVersionHasError);
		}
		
		// 对 component.json 文件校验通过后
		// 开始下载 api 项目，并校验 api.json
		if(success) {
			context.parseApiGitUrl(componentJson.getApi().getGit().trim());
			// 下载 api 项目
			logger.info(StringUtils.repeat("-", 45));
			logger.info("四、开始获取 API 库源码");
			GitSyncApiRepoTask apiRepoTask = new GitSyncApiRepoTask(context);
			Optional<Boolean> gitSyncApiOption = apiRepoTask.run();
			success = gitSyncApiOption.isPresent();
			if(success) {
				logger.info("完成");
			} else {
				logger.error("失败");
			}
		}
		
		String apiRepoRefName = null;
		String apiRepoTagName = null;
		if(success) {
			// 确认指定版本的 tag 是否存在
			String apiVersion = componentJson.getApi().getVersion().trim();
			logger.info(StringUtils.repeat("-", 45));
			logger.info("五、检查 API 库中是否存在名为 {0} 的 tag", apiVersion);
			
			ApiRepoFindTagTask apiRepoFindTagTask = new ApiRepoFindTagTask(context, apiVersion);
			Optional<Ref> apiRepoTag = apiRepoFindTagTask.run();
			success = apiRepoTag.isPresent();
			
			if(success) {
				apiRepoRefName = apiRepoTag.get().getName();
				apiRepoTagName = apiRepoRefName.substring(Constants.R_TAGS.length());
				logger.info("在 API 仓库中存在名为 {0} 的 tag", tagName);
			} else {
				logger.error("在 API 仓库中没有找到名为 {0} 的 tag", apiVersion);
			}
		}
		
		// 获取 api.json 内容
		// 从最新的 git tag 中查找 component.json 文件
		String apiJsonContent = null;
		if(success) {
			logger.info(StringUtils.repeat("-", 45));
			logger.info("六、校验 API 仓库根目录下的 api.json 文件");
			logger.info("在 Git Tag {0} 的根目录下查找 api.json 文件", apiRepoTagName);
			
			// 从组件库的 component.json 中指定的 api 版本中查找
			ApiJsonFetchTask task = new ApiJsonFetchTask(context, apiRepoRefName);
			Optional<String> contentOption = task.run();
			success = contentOption.isPresent();
			
			if(success) {
				apiJsonContent = contentOption.get();
				logger.info("存在 api.json 文件");
			} else {
				logger.error("没有找到 api.json 文件");
			}
		}
		// 将 json 字符串转换为 java 对象
		ApiJson apiJson = null;
		if(success) {
			logger.info("将 api.json 内容转换为 java 对象");
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				apiJson = objectMapper.readValue(apiJsonContent, ApiJson.class);
				logger.info("转换完成");
				success = true;
			} catch (IOException e) {
				logger.error("转换失败");
				logger.error(e);
				success = false;
			}
		}

		String[] apiComponents = null;
		// 确定组件库实现了 api 库中的所有组件
		if(success) {
			logger.info(StringUtils.repeat("-", 45));
			logger.info("七、校验 API 库中 api.json 的 components 中定义组件是否都在组件库中 component.json 的 components 中定义");
			logger.info("注意：只判断路径最后一段是否相同");
			String[] components = componentJson.getComponents();
			apiComponents = apiJson.getComponents();
			
			for(String apiComponent : apiComponents) {
				String[] apiSegments = apiComponent.split("/");
				String apiComponentName = apiSegments[apiSegments.length - 1];
				boolean matched = false;
				for(String component : components) {
					// 存的是完整路径，完整路径肯定不相等，但最后一个 / 后的值可以约定相等
					String[] segments = component.split("/");
					String componentName = segments[segments.length - 1];
					if(apiComponentName.trim().equalsIgnoreCase(componentName.trim())) {
						matched = true;
						break;
					}
				}
				
				if(!matched) {
					logger.error("components - 组件库中未配置 API 库定义的 {0} 组件", apiComponent);
					success = false;
				}
			}
			
			if(success) {
				logger.info("校验通过");
			}
		}
		
		List<ChangeLog> changelogs = new ArrayList<ChangeLog>();
		if(success) {
			// 开始逐个组件的校验 API 定义
			// 以下，只解析 API 项目，不解析组件项目
			logger.info(StringUtils.repeat("-", 45));
			logger.info("八、开始逐个校验 API 项目中的组件定义");
			
			// 逐个组件校验，是否存在 changelog 文件夹
			// 逐个组件校验，是否在 changelog 文件夹中至少存在一个 json 文件
			logger.info("api.json 文件中共存在 {0} 个组件", apiComponents.length);
			logger.info("校验是否存在 changelog 文件夹，并在 changelog 文件夹下定义了 API 变更文件");
			for(int i = 0; i < apiComponents.length; i++) {
				String path = apiComponents[i];
				logger.info("{0} {1} 部件", i+1, path);
				// TODO: 从指定的 tag 下获取，而不是获取仓库的当前路径
				Path componentRootPath = context.getLocalApiRepoPath().getRepoSourceDirectory().resolve(path);
				// 判断是否存在 changelog 文件夹
				if(Files.notExists(componentRootPath.resolve("changelog"))) {
					logger.error("在 {0} 文件夹下缺失 changelog 文件夹", path);
					success = false;
					continue;
				}
				// changelog 文件夹不能为空
				if(componentRootPath.resolve("changelog").toFile().list().length == 0) {
					logger.error("在 {0}/changelog 文件夹下缺失 API 定义文件，如 0_1_0.json", path);
					success = false;
					continue;
				}
				logger.info("校验通过");
			}
			
			List<String> notSetupChangeFiles = new ArrayList<String>();
			if(success) {
				// 校验所有的 changelog 文件
				// 先获取所有的 changelog 文件路径
				List<String> allChangeFiles = new ArrayList<String>();
				for(String eachPath : apiComponents) {
					Path apiRootPath = context.getLocalApiRepoPath().getRepoSourceDirectory().resolve(eachPath);
					String[] files = apiRootPath.resolve("changelog").toFile().list();
					for(String file : files) {
						allChangeFiles.add(eachPath + "/changelog/" + file);
					}
				}
				
				// 查出所有已安装的文件
				List<ApiChangelog> setupChangeFiles = apiRepoDao
						.findByNameAndCreateUserId(apiJson.getName(), publishTask.getCreateUserId())
						.map(apiRepo -> apiChangelogDao.findAllByApiRepoId(apiRepo.getId()))
						.orElse(Collections.emptyList());
				
				// 确认是否存在删了已安装的文件的情况
				// 已 setup 中存在，但 all 中不存在
				logger.info("检查是否存在，文件已安装，但在 API 项目中删掉的日志变更文件");
				List<ApiChangelog> deleted = setupChangeFiles
						.stream()
						.filter(changelog -> {
							boolean notContain = !allChangeFiles.contains(changelog.getChangelogFileName());
							if(notContain) {
								logger.error(changelog.getChangelogFileName());
							}
							return notContain;
						})
						.collect(Collectors.toList());
				if(!deleted.isEmpty()) {
					success = false;
				} else {
					// 很诡异，当值为“不存在”时，打印出的是 NX[(W
					logger.info("无");
				}
				
				if(success) {
					logger.info("检查是否存在，文件已安装过，但内容被修改");
					// 校验已安装的文件内容是否发生了变化
					List<ApiChangelog> modified = setupChangeFiles
							.stream()
							.filter(changelog -> {
								String md5Now = "";
								try (InputStream in = Files.newInputStream(context.getLocalApiRepoPath().getRepoSourceDirectory().resolve(changelog.getChangelogFileName()))){
									md5Now = DigestUtils.md5Hex(in);
								} catch (IOException e) {
									logger.error(e);
								}
								boolean notEqual = !changelog.getMd5Sum().equals(md5Now);
								if(notEqual) {
									logger.error(changelog.getChangelogFileName());
								}
								return notEqual;
							}).collect(Collectors.toList());
					
					if(!modified.isEmpty()) {
						success = false;
					} else {
						logger.info("无");
					}
				}
				
				// 找出未安装的变更文件
				if(success) {
					notSetupChangeFiles = allChangeFiles.stream()
							.filter(fileName -> !setupChangeFiles.stream()
									.anyMatch(setupChange -> setupChange.getChangelogFileName().equals(fileName)))
							.collect(Collectors.toList());
					
					// 如果没有找到未安装的文件，则给出提示信息
					if(notSetupChangeFiles.isEmpty()) {
						logger.info("已是最新版本，没有找到要安装的 API 文件");
					} else {
						// 开始逐个解析变更日志文件
						// 要对所有文件都校验一遍，不能出现了错误就停止校验
						logger.info("开始解析日志变更文件");
						boolean hasErrors = false;
						for(String fileName : notSetupChangeFiles) {
							logger.info("开始解析 {0}", fileName);
							ChangeLog changelogInfo = new ChangeLog();
							try {
								// TODO：从指定的 tag 下找，而不是在最新文件中查找
								String fileContent = Files.readString(context.getLocalApiRepoPath().getRepoSourceDirectory().resolve(fileName));
								ObjectMapper objectMapper = new ObjectMapper();
								Map<?, ?> changelogMap = objectMapper.readValue(fileContent, Map.class);
								ChangelogParseTask changelogParseTask = new ChangelogParseTask(context, changelogMap);
								Optional<ChangeLog> changelogOption = changelogParseTask.run();
								hasErrors = changelogOption.isPresent();
								if(hasErrors) {
									logger.error("解析时出现错误");
								} else {
									ChangeLog cl = changelogOption.get();
									cl.setFileName(fileName);
									changelogs.add(cl);
									logger.info("解析完成");
								}
							} catch (IOException e) {
								logger.error(e);
							}
							changelogs.add(changelogInfo);
						}
						success = !hasErrors;
					}
				}
			}
		}
		
		ComponentRepo savedCompRepo = null;
		ComponentRepoVersion savedCompRepoVersion = null;
		ApiRepo savedApiRepo = null;
		List<ApiRepoVersion> savedApiRepoVersions = new ArrayList<ApiRepoVersion>();
		if(success) {
			// 注册组件库
			logger.info(StringUtils.repeat("-", 45));
			logger.info("九、开始保存组件库信息");
			
			logger.info("保存组件库基本信息");
			ComponentRepo repo = new ComponentRepo();
			repo.setGitRepoUrl(publishTask.getGitUrl());
			repo.setGitRepoWebsite(context.getLocalComponentRepoPath().getWebsite());
			repo.setGitRepoOwner(context.getLocalComponentRepoPath().getOwner());
			repo.setGitRepoName(context.getLocalComponentRepoPath().getRepoName());
			repo.setName(componentJson.getName().trim()); // name 必填
			repo.setVersion(componentJson.getVersion().trim()); // version 必填
			repo.setLabel(componentJson.getDisplayName());
			repo.setDescription(componentJson.getDescription());
			// logo_path 未设置
			repo.setCategory(RepoCategory.fromValue(componentJson.getCategory().trim()));
			repo.setLanguage(Language.fromValue(componentJson.getLanguage().trim()));
			if(StringUtils.isNotBlank(componentJson.getIcon())) {
				repo.setLogoPath(componentJson.getIcon());
			}
			repo.setCreateUserId(publishTask.getCreateUserId());
			repo.setCreateTime(LocalDateTime.now());
			
			savedCompRepo = componentRepoDao.save(repo);
			logger.info("保存成功");
			
			logger.info("保存组件库版本信息");
			ComponentRepoVersion compRepoVersion = new ComponentRepoVersion();
			compRepoVersion.setComponentRepoId(savedCompRepo.getId());
			compRepoVersion.setVersion(componentJson.getVersion().trim());
			compRepoVersion.setCreateUserId(publishTask.getCreateUserId());
			compRepoVersion.setCreateTime(LocalDateTime.now());
			savedCompRepoVersion = componentRepoVersionDao.save(compRepoVersion);
			logger.info("保存成功");
			
			logger.info(StringUtils.repeat("-", 45));
			logger.info("十、开始保存 API 库信息");
			
			logger.info("保存 API 库基本信息");
			// 先判断 API 库是否已存在
			// 不存在则新增
			Optional<ApiRepo> apiRepoOption = apiRepoDao.findByNameAndCreateUserId(apiJson.getName(), publishTask.getCreateUserId());
			if(apiRepoOption.isPresent()) {
				savedApiRepo = apiRepoOption.get();
				logger.info("已存在");
			} else {
				ApiRepo apiRepo = new ApiRepo();
				apiRepo.setGitRepoUrl(context.getLocalApiRepoPath().getGitUrl());
				apiRepo.setGitRepoWebsite(context.getLocalApiRepoPath().getWebsite());
				apiRepo.setGitRepoOwner(context.getLocalApiRepoPath().getOwner());
				apiRepo.setGitRepoName(context.getLocalApiRepoPath().getRepoName());
				apiRepo.setName(apiJson.getName());
				apiRepo.setVersion(apiJson.getVersion()); // 用哪个版本号？确保版本号一致
				apiRepo.setLabel(apiJson.getDisplayName());
				apiRepo.setDescription(apiJson.getDescription());
				apiRepo.setCategory(RepoCategory.fromValue(apiJson.getCategory().trim()));
				apiRepo.setCreateUserId(publishTask.getCreateUserId());
				apiRepo.setCreateTime(LocalDateTime.now());
				savedApiRepo = apiRepoDao.save(apiRepo);
				
				logger.info("保存成功"); 
			}
			
			logger.info("保存 API 库版本信息");
			Optional<ApiRepoVersion> apiRepoVersionOption = apiRepoVersionDao.findByApiRepoIdAndVersion(savedApiRepo.getId(), apiJson.getVersion());
			if(apiRepoVersionOption.isPresent()) {
				logger.info("已存在");
			} else {
				// 关于 API repo 的发布，因为发布并不不能做到按照版本号顺序发布，所以需要注意：
				// 1. 如果要新增，则可能发布的是最新版，也可能发布的是之前没有发布过的旧版
				//   所以一定要精准的找到上一个版本
				// 2. 要确保已存储所有版本
				// 获取 api 仓库的 tag 列表
				
				List<String> apiVersions = new ArrayList<String>();
				try {
					apiVersions = GitUtils
							.getTags(context.getLocalApiRepoPath().getRepoSourceDirectory())
							.stream()
							.map(ref -> {
								String apiTagName = ref.getName().substring(Constants.R_TAGS.length());
								if(apiTagName.toLowerCase().startsWith("v")) {
									apiTagName = apiTagName.substring(1);
								}
								return apiTagName;
							}).collect(Collectors.toList());
				} catch (GitTagFailedException e) {
					logger.error(e);
				}
				
				Integer savedApiRepoId = savedApiRepo.getId();
				Version currentApiRepoVersion = Version.parseVersion(apiJson.getVersion(), true);
				savedApiRepoVersions = apiVersions.stream().filter(apiVersion -> {
					Version version = Version.parseVersion(apiVersion, true);
					return !currentApiRepoVersion.isGreaterThan(version);
				}).map(apiVersion -> {
					ApiRepoVersion apiRepoVersion = new ApiRepoVersion();
					apiRepoVersion.setApiRepoId(savedApiRepoId);
					apiRepoVersion.setVersion(apiVersion);
					apiRepoVersion.setCreateUserId(publishTask.getCreateUserId());
					apiRepoVersion.setCreateTime(LocalDateTime.now());
					return apiRepoVersionDao.save(apiRepoVersion);
				}).collect(Collectors.toList());
				
				// 先循环版本
				for(ApiRepoVersion apiRepoVersion : savedApiRepoVersions) {
					// 再嵌套循环组件
					for(String component : apiComponents) {
						// 如果当前版本存在 changelog 文件，则先查找是否存在上一个版本
						// 如果存在上一个版本，则先复制上一个版本
						// 然后在上一个版本的基础上，应用本版本的变更
						
						// 注意：变更文件的名称，是与版本号保持一致的，会存在某个组件在某个版本中没有变更的情况
						// 所以要一直向上追溯，而不是只追溯到上一个版本
						if(true) {
							
						}
					}
				}
				
				logger.info("保存成功");
			}
		}
		
		if(success) {
			logger.info(StringUtils.repeat("-", 45));
			logger.info("十一、开始校验是否存在无效的变更");
			// 如果是第一个变动，则与上一个版本做比较
			// 如果是第二个及后续变动，则与上一个变动后的版本做比较
			// 如果是新增部件，则判断该部件名是否已被占用，如果已占用，则给出错误信息
			// 所以在校验时，要先应用所有变动，如果校验失败，则删除所有变动
			// 先找到上一个版本，然后复制一份上一版本的内容
			
			// 如果当前应用的不是最新版的 API，则已发布过，所以不需要再发布?
			// 如果第二版已发布过，但是这里又要发布第一版，如何确保 code 的唯一？
			// 所以，在第一次发布时，一定要第一版能够发布？
			// 或者在发布时，将前面的每一版都检查并发布一遍？
			
			// 只有执行成功的，才往 changelog 历史表中存
			// 出错了，要 rollback
		}
		
		// 基于上一个版本做校验
		// 为新组件生成 code
		// 保存组件库基本信息
		// 保存 API 库基本信息
		// 应用 API 变更
		// 保存 API 变更执行结果
		
		
		
		// 修改组件库和API 库的最近发布时间
		
		
		
		
		
		// API 项目，如果已保存，则不再保存。
		
		
		
		// 以上是仓库一级的校验，校验通过后，登记组件库信息。
		// 接下来逐个做组件一级的校验
		// 先检查总共有多少个组件，然后逐个解析组件的基本信息，属性和事件等
		
		// TODO: 先看是否能编译成功？
		// TODO: 以下逻辑要重写
//		String[] components = null;
//		if(success) {
//			// 当是 UI 部件时
//			if("Widget".equalsIgnoreCase(repoInfo.getCategory().trim()) && 
//					"TypeScript".equalsIgnoreCase(repoInfo.getLanguage().trim())) {
//				components = repoInfo.getComponents();
//				if(components.length == 0) {
//					logger.info("blocklang.json 文件的 widgets 中没有配置 UI 部件");
//					success = false;
//				}
//			}
//		}
		
		// 遍历目录，按照默认的规则查找部件
		// 查询路径是 'src/widgets/{widgetName}/index.ts'
		// 校验规则需逐步完善和精准
		// 校验规则：
		// 1. 一个 UI 部件的目录下，应该包含以下文件：index.ts、designer.ts、changelog 文件夹中应该至少有一个 json 文件
		// 2. 解析 changelog 文件夹
		
		
		// 如果解析出错了呢？？？？？？？？？？？？？？
		// 前面的操作撤销还是保留呢？
		// 通过写文档梳理。
		

		
		// TODO: 先读取 tag 列表，还是先检查 blocklang.json
		
		// 或者检测都通过之后，确定是一个有效的组件仓库之后，才开始存储数据
		// 所以，这个阶段，应该是做仓库一级的检测
		// 仓库一级的检测通过后，才允许注册组件仓库
		// 第二轮逐个做部件检测，检测通过的组件才能保存，检测没有通过的给出详细提示
		
		
		// 查找 git 仓库中的 tag
		// 找到最新的 tag
		// 从最新的 tag 中查找 blocklang.json 文件
		// 校验 blocklang.json 中的必填项等
		// 扫面项目中所有的 ui 部件，确认文件是否齐全
		// 扫描 changelog 文件夹，检测配置的是否准确
		// 当检测通过之后，才开始往数据库中存储 ui 部件的元数据
		// 编译 ts 文件？
		
		// TODO:当组件库中的组件解析完成后，再填写发布时间
		
		stopWatch.stop();
		long seconds = stopWatch.getTime(TimeUnit.SECONDS);
		logger.info("共耗时：{0} 秒", seconds);
		logger.info(StringUtils.repeat("=", 60));
		
		// context.finished(releaseResult);
	}

}
