package com.blocklang.release.task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.designer.data.AttachedWidget;
import com.blocklang.develop.service.ProjectDependenceService;
import com.blocklang.develop.service.ProjectResourceService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 用于在文件系统中存储项目模型信息
 * 
 * @author jinzw
 *
 */
public class ProjectModelWriteTask extends AbstractTask {
	
	private ProjectDependenceService projectDependenceService;
	private ProjectResourceService projectResourceService;
	
	public ProjectModelWriteTask(AppBuildContext appBuildContext, ProjectDependenceService projectDependenceService, ProjectResourceService projectResourceService) {
		super(appBuildContext);
		this.projectDependenceService = projectDependenceService;
		this.projectResourceService = projectResourceService;
	}

	@Override
	public Optional<?> run() {
		// 进入项目的根目录，与 client 文件夹并列
		// 创建 .blocklang_models 文件夹
		Path projectModelPath = appBuildContext.getProjectModelRootDirectory();
		if(Files.notExists(projectModelPath)) {
			try {
				Files.createDirectory(projectModelPath);
			} catch (IOException e) {
				appBuildContext.error(e);
				return Optional.empty();
			}
		}
		final ObjectMapper objectMapper = new ObjectMapper();
		
		appBuildContext.info("开始生成 project.json 文件");
		// 生成 project.json 文件
		Map<String, String> project = new HashMap<String, String>();
		project.put("name", appBuildContext.getProjectName());
		project.put("label", appBuildContext.getDescription());
		project.put("version", appBuildContext.getVersion());
		try {
			String projectJson = objectMapper.writeValueAsString(project);
			Files.writeString(projectModelPath.resolve("project.json"), projectJson);
			appBuildContext.info("完成");
		} catch (IOException e) {
			appBuildContext.error("失败");
			appBuildContext.error(e);
			return Optional.empty();
		}
		
		// 生成 dependences.json 文件
		appBuildContext.info("开始生成 dependences.json 文件");
		List<Map<String, Object>> dependences = projectDependenceService.findProjectBuildDependences(appBuildContext.getProjectId()).stream().map(data -> {
			Map<String, Object> toMap = new HashMap<String, Object>();
			toMap.put("name", data.getComponentRepoVersion().getName());
			toMap.put("version", data.getComponentRepoVersion().getVersion());
			toMap.put("apiRepoId", data.getApiRepo().getId());
			return toMap;
		}).collect(Collectors.toList());
		try {
			String dependencesJson = objectMapper.writeValueAsString(dependences);
			Files.writeString(projectModelPath.resolve("dependences.json"), dependencesJson);
			appBuildContext.info("完成");
		} catch (IOException e) {
			appBuildContext.error("失败");
			appBuildContext.error(e);
			return Optional.empty();
		}
		
		// 为每个页面生成一个 json 文件
		// FIXME: appType 应该动态传入
		appBuildContext.info("开始生成页面模型");
		List<Map<String, Object>> pages = projectResourceService.findAllPages(appBuildContext.getProjectId(), AppType.WEB).stream().map(projectResource -> {
			Map<String, Object> result = new HashMap<String, Object>();
			// pageInfo
			// 1. id
			// 2. key
			// 3. groupPath
			Map<String, Object> pageInfo = new HashMap<String, Object>();
			pageInfo.put("id", projectResource.getId());
			pageInfo.put("key", projectResource.getKey());
			List<String> groupPathes = projectResourceService.findParentPathes(projectResource.getId());
			// 注意要去除当前资源的路径
			groupPathes.remove(groupPathes.size() - 1);
			// 临时存储
			result.put("groupPathes", groupPathes);
			// widgets
			// 1. id
			// 2. parentId
			// 3. apiRepoId
			// 4. widgetName
			// 5. canHasChildren
			// 6. properties
			List<AttachedWidget> widgets = projectResourceService.getPageModel(projectResource.getProjectId(), projectResource.getId()).getWidgets();
			
			result.put("pageInfo", pageInfo);
			result.put("widgets", widgets);
			return result;
		}).collect(Collectors.toList());
		
		appBuildContext.info("共需生成 {0} 个页面模型", pages.size());
		int num = 1;
		for(Map<String, Object> eachPage : pages) {
			@SuppressWarnings("unchecked")
			List<String> groupPathes = (List<String>) eachPage.remove("groupPathes");
			@SuppressWarnings("unchecked")
			Map<String, Object> pageInfo = (Map<String, Object>) eachPage.get("pageInfo");
			String pageKey = pageInfo.get("key").toString();
			appBuildContext.info("第 {0} 个，开始生成 {1}/{2}.json 文件", num, String.join("/", groupPathes), pageInfo.get("key"));
			
			Path groupPath = Paths.get("pages", groupPathes.toArray(new String[0]));
			Path pagePath = projectModelPath.resolve(groupPath);

			if(Files.notExists(pagePath)) {
				try {
					Files.createDirectories(pagePath);
				} catch (IOException e) {
					appBuildContext.error(e);
					return Optional.empty();
				}
			}
			
			try {
				String pageJson = objectMapper.writeValueAsString(eachPage);
				Files.writeString(pagePath.resolve(pageKey+".json"), pageJson);
				appBuildContext.info("完成");
			} catch (IOException e) {
				appBuildContext.error(e);
				return Optional.empty();
			}
			
			num++;
		}
		
		return Optional.of(true);
	}

}
