package com.blocklang.marketplace.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;

import com.blocklang.core.git.GitBlobInfo;
import com.blocklang.core.git.GitFileInfo;
import com.blocklang.core.git.GitUtils;
import com.blocklang.marketplace.constant.MarketplaceConstant;
import com.blocklang.marketplace.dao.ApiChangeLogDao;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.data.ApiJson;
import com.blocklang.marketplace.data.changelog.ChangeLog;
import com.blocklang.marketplace.data.changelog.ComponentChangeLogs;
import com.blocklang.marketplace.model.ApiChangeLog;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiChangeLogParseGroupTask extends AbstractRepoPublishTask {

	private ApiJson apiJson;
	private ApiRepoDao apiRepoDao;
	private ApiChangeLogDao apiChangelogDao;
	
	public ApiChangeLogParseGroupTask(MarketplacePublishContext context) {
		super(context);
		this.apiJson = context.getApiJson();
	}

	@Override
	public Optional<Boolean> run() {
		boolean success = true;
		
		List<String> apiComponents = Arrays.asList(apiJson.getComponents());
		logger.info("{0} 文件中共存在 {1} 个组件", MarketplaceConstant.FILE_NAME_API, apiComponents.size());
		// 从 API 仓库的指定 tag 中找到所有的 changelog 文件
		List<GitFileInfo> allJsonFiles = GitUtils
				.getAllFilesFromTag(
					context.getLocalApiRepoPath().getRepoSourceDirectory(), 
					context.getApiRepoTagName(),
					".json")
				.stream()
				// 过滤掉不属于 changelog 的文件
				// 放在组件的 changelog 文件夹下的 json 文件都可看做 api 变更文件
				.filter(gitFileInfo -> {
					String parentPath = gitFileInfo.getParentPath();
					String componentPath = parentPath.substring(0, parentPath.length() - "/changelog".length());
					return apiComponents.contains(componentPath);
				}).collect(Collectors.toList());
		
		// 按照组件分组
		List<ComponentChangeLogs> allComponentChangeLogs = apiComponents.stream().map(componentName -> {
			ComponentChangeLogs componentChangeLogs = new ComponentChangeLogs();
			componentChangeLogs.setComponentName(componentName);
			
			List<ChangeLog> changeLogs = allJsonFiles.stream().map(gitFileInfo -> {
				ChangeLog changeLog = new ChangeLog();
				changeLog.setFileName(gitFileInfo.getName());
				return changeLog;
			}).collect(Collectors.toList());
			componentChangeLogs.setChangeLogs(changeLogs);
			return componentChangeLogs;
		}).collect(Collectors.toList());
		
		// 后续都是逐个组件查找
		
		// 在 allComponentChangeLogs 的基础上校验
		// 逐个判断每个组件是否存在 changelog 文件
		logger.info("逐个组件校验，组件是否定义了 API 变更文件");
		int i = 0;
		for(ComponentChangeLogs componentChangeLogs : allComponentChangeLogs) {
			i++;
			String componentName = componentChangeLogs.getComponentName();
			logger.info("{0}. {1} 组件", i, componentName);
			if(componentChangeLogs.getChangeLogs().isEmpty()) {
				logger.error("在 {0}/changelog 文件夹下缺失 API 变更文件，如 0_1_0.json", componentName);
				success = false;
			} else {
				logger.info("共存在 {0} 个 API 变更文件", componentChangeLogs.getChangeLogs().size());
			}
		}
		
		// 获取已安装的 API 变更文件
		// 注意：只有安装成功后，才能在 api_change_log 表中登记
		List<ApiChangeLog> setupChangeFiles = apiRepoDao
				.findByNameAndCreateUserId(apiJson.getName(), context.getPublishTask().getCreateUserId())
				.map(apiRepo -> apiChangelogDao.findAllByApiRepoId(apiRepo.getId()))
				.orElse(Collections.emptyList());
		// 按照组件分组，约定日志变更文件是直接存在{componentName}/changelog/ 文件夹下的，所以按照路径截取
		Map<String, List<ApiChangeLog>> groupedSetupChangeFiles = setupChangeFiles.stream().collect(Collectors.groupingBy(apiChangeLog -> {
			String fileName = apiChangeLog.getChangelogFileName();
			return fileName.substring(0, fileName.lastIndexOf("/"));
		}));
		
		if(success) {
			logger.info("校验是否存在，API 变更文件已经安装过，但在 API 项目中却删了此文件");
			i = 0;
			for(Map.Entry<String, List<ApiChangeLog>> entry : groupedSetupChangeFiles.entrySet()) {
				i++;
				String componentName = entry.getKey();
				logger.info("{0}. {1} 组件", i, componentName);
				
				Optional<ComponentChangeLogs> changeLogsOption = allComponentChangeLogs
						.stream()
						.filter(componentChangeLogs -> componentChangeLogs.getComponentName().equals(componentName))
						.findFirst();
				List<String> changeLogsInApiRepo = new ArrayList<String>();
				if(changeLogsOption.isPresent()) {
					ComponentChangeLogs componentChangeLogs = changeLogsOption.get();
					changeLogsInApiRepo = componentChangeLogs.getChangeLogs()
							.stream()
							.map(changeLog -> componentChangeLogs.getComponentName() + "/changelog/" + changeLog.getFileName())
							.collect(Collectors.toList());
				}
				for(ApiChangeLog setuped : entry.getValue()) {
					if(!changeLogsInApiRepo.contains(setuped.getChangelogFileName())) {
						logger.error("{0} 文件已被删除", setuped.getChangelogFileName());
						success = false;
					}
				}
			}
		}
		if(success) {
			logger.info("校验完成");
		}
		
		List<GitBlobInfo> blobs = new ArrayList<GitBlobInfo>();
		if(success) {
			// 获取未安装的 API 变更文件的内容
			blobs = GitUtils.loadDataFromTag(context.getLocalApiRepoPath().getRepoSourceDirectory(), context.getApiRepoTagName(), allJsonFiles);
			logger.info("校验是否存在，API 变更文件已经安装过，但在 API 项目中却修改了此文件");
			i = 0;
			for(Map.Entry<String, List<ApiChangeLog>> entry : groupedSetupChangeFiles.entrySet()) {
				i++;
				String componentName = entry.getKey();
				logger.info("{0}. {1} 组件", i, componentName);
				
				for(ApiChangeLog apiChangeLog : entry.getValue()) {
					for(GitBlobInfo gitBlobInfo : blobs) {
						if(gitBlobInfo.getPath().equals(apiChangeLog.getChangelogFileName())) {
							String md5Now = DigestUtils.md5Hex(gitBlobInfo.getContent());
							if(!apiChangeLog.getMd5Sum().equals(md5Now)) {
								logger.error("{0} 文件已被修改", apiChangeLog.getChangelogFileName());
								success = false;
								break;
							}
						}
					}
				}
			}
		}
		if(success) {
			logger.info("校验完成");
		}
		
		if(success) {
			// 找到未安装的 API 变更文件
			logger.info("获取未安装的 API 变更文件");
			i = 0;
			int unsetupCount = 0;
			for(ComponentChangeLogs componentChangeLogs : allComponentChangeLogs) {
				i++;
				String componentName = componentChangeLogs.getComponentName();
				logger.info("{0}. {1} 组件", i, componentName);
				
				// 按照约定，一个版本最多对应一个 API 变更文件
				// 并且 API 变更文件是严格按照版本顺序引入的
				// 则直接按照已安装的文件数，剔除掉前面几个即可
				List<ChangeLog> allChangeLogs = componentChangeLogs.getChangeLogs();
				List<ApiChangeLog> setupedChangeLogs = groupedSetupChangeFiles.get(componentChangeLogs.getComponentName());
				
				// 文件名必须使用版本号，如版本号为 0.1.0，则文件名为 0_1_0.json
				String latestPublishVersion = null;
				if(setupedChangeLogs.size() > 0) {
					String fileName = allChangeLogs.get(setupedChangeLogs.size() - 1).getFileName();
					latestPublishVersion = parseVersion(fileName);
				}
				componentChangeLogs.setLatestPublishVersion(latestPublishVersion);
				// 去掉已安装的文件
				for(int index = 0; index < setupedChangeLogs.size(); index++) {
					allChangeLogs.remove(0);
				}
				
				unsetupCount += allChangeLogs.size();
				logger.info("共找到 {0} 个未安装的 API 变更文件", allChangeLogs.size());
			}
			
			if(unsetupCount == 0) {
				logger.info("已是最新版本");
				success = false;
			}
		}
		
		if(success) {
			// 然后解析 API 安装文件的内容
			logger.info("开始解析 API 变更文件，将文件内容转换为 Java 对象");
			ObjectMapper objectMapper = new ObjectMapper();
			i = 0;
			for(ComponentChangeLogs componentChangeLogs : allComponentChangeLogs) {
				String componentName = componentChangeLogs.getComponentName();
				List<ChangeLog> changeLogs = componentChangeLogs.getChangeLogs();
				if(changeLogs.size() > 0) {
					i++;
					logger.info("{0}. {1} 组件", i, componentName);
					
					for(ChangeLog changeLog : changeLogs) {
						for(GitBlobInfo gitBlobInfo : blobs) {
							String changeLogFilePath = componentName + "/changelog/" + changeLog.getFileName();
							if(gitBlobInfo.getPath().equals(changeLogFilePath)) {
								try {
									Map<?, ?> changelogMap = objectMapper.readValue(gitBlobInfo.getContent(), Map.class);
									ApiChangeLogValidateTask changeLogValidateTask = new ApiChangeLogValidateTask(context, changelogMap);
									Optional<ChangeLog> changelogOption = changeLogValidateTask.run();
									success = changelogOption.isPresent();
									if(success) {
										ChangeLog cl = changelogOption.get();
										
										changeLog.setId(cl.getId());
										changeLog.setAuthor(cl.getAuthor());
										changeLog.setChanges(cl.getChanges());
										changeLog.setVersion(parseVersion(changeLog.getFileName()));
										logger.info("{0} 解析完成", changeLogFilePath);
									} else {
										logger.error("{0} 解析时出现错误", changeLogFilePath);
									}
								} catch (IOException e) {
									logger.error(e);
								}
							}
						}
					}
				}
			}
		}

		if(success) {
			context.setChangeLogs(allComponentChangeLogs);
			return Optional.of(true);
		}
		return Optional.empty();
	}

	private String parseVersion(String fileName) {
		return fileName.substring(0, fileName.length() - ".json".length()).replace('_', '.');
	}

}
