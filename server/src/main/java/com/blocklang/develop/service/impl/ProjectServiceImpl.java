package com.blocklang.develop.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.constant.Constant;
import com.blocklang.core.git.GitUtils;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.service.UserService;
import com.blocklang.core.util.IdGenerator;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.FileType;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.dao.ProjectDao;
import com.blocklang.develop.dao.ProjectFileDao;
import com.blocklang.develop.data.ProgramModel;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectContext;
import com.blocklang.develop.model.ProjectFile;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectResourceService;
import com.blocklang.develop.service.ProjectService;
import com.blocklang.release.dao.AppDao;
import com.blocklang.release.model.App;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProjectServiceImpl implements ProjectService {
	
	private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

	@Autowired
	private ProjectDao projectDao;
	@Autowired
	private AppDao appDao;
	@Autowired
	private ProjectFileDao projectFileDao;
	@Autowired
	private PropertyService propertyService;
	@Autowired
	private ProjectResourceService projectResourceService;
	
	// 如果需要缓存时，使用 service，不要使用 dao
	// 因为只会为 service 添加缓存
	@Autowired
	private UserService userService;
	
	@Override
	public Optional<Project> find(String userName, String projectName) {
		return userService.findByLoginName(userName).flatMap(user -> {
			return projectDao.findByCreateUserIdAndName(user.getId(), projectName);
		});
	}

	// TODO: 添加数据库事务
	@Override
	public Project create(UserInfo user, Project project) {
		
		// 保存项目基本信息
		Project savedProject = projectDao.save(project);
		
		LocalDateTime createTime = project.getCreateTime();
		Integer createUserId = project.getCreateUserId();
		
		// 保存 APP 基本信息
		App app = new App();
		String appName = "@" + user.getLoginName() + "/" + project.getName();
		app.setAppName(appName);
		app.setProjectId(savedProject.getId());
		app.setCreateUserId(createUserId);
		app.setCreateTime(createTime);
		app.setRegistrationToken(IdGenerator.shortUuid());
		appDao.save(app);
		
		// 生成入口模块：Main 页面
		ProjectResource resource = createMainProgram(project.getId(), createTime, createUserId);
		// 此方法中实现了应用模板功能
		// TODO: 是否需要将应用模板逻辑，单独提取出来？
		projectResourceService.insert(resource);
		
		// 生成 README.md 文件
		String readMeContent = "# "+ project.getName() + "\r\n" + "\r\n" + "**TODO: 在这里添加项目介绍，帮助感兴趣的人快速了解您的项目。**";
		createReadmeFile(project.getId(), readMeContent, createTime, createUserId);
		
		// 创建 git 仓库
		propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).ifPresent(rootDir -> {
			ProjectContext context = new ProjectContext(user.getLoginName(), project.getName(), rootDir);
			try {
				ObjectMapper mapper = new ObjectMapper();
				ProgramModel programModel = new ProgramModel();
				
				String mainPageJsonString = "{}";
				try {
					mainPageJsonString = mapper.writeValueAsString(programModel);
				} catch (JsonProcessingException e) {
					logger.error("转换 json 失败", e);
				}
				
				GitUtils
					.beginInit(context.getGitRepositoryDirectory(), user.getLoginName(), user.getEmail())
					.addFile(ProjectResource.README_NAME, readMeContent)
					.addFile(ProjectResource.MAIN_KEY + ".ui.json", mainPageJsonString)
					.commit("First Commit");
			}catch (RuntimeException e) {
				logger.error(String.format("为项目 {} 初始创建 git 仓库失败", appName), e);
			}
		});
		
		return savedProject;
	}
	
	/**
	 * 生成默认模块: Main 页面
	 * @param project
	 * @param createTime
	 * @param createUserId
	 * @return
	 */
	private ProjectResource createMainProgram(Integer projectId, LocalDateTime createTime, Integer createUserId) {
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setKey(ProjectResource.MAIN_KEY);
		resource.setName(ProjectResource.MAIN_NAME);
		resource.setResourceType(ProjectResourceType.PROGRAM);
		resource.setAppType(AppType.WEB);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(1);
		resource.setCreateUserId(createUserId);
		resource.setCreateTime(createTime);
		
		// 因为是空模板，所以这里只引用模板，但没有应用模板
		// 但是因为没有保存，所以此行代码是多余的。
		// 注意，在项目资源表中，不需要保存 templateId
		// 如果不是空模板，则后面直接在页面中应用模板即可
		// TODO: 空模板，也可称为默认模板，空模板中也可能有内容，如只显示“Hello World”，
		// 因此，后续要添加应用模板功能，不要再注释掉此行代码
		// resource.setTempletId(projectResourceService.getEmptyTemplet().getId());
		return resource;
	}
	
	private void createReadmeFile(Integer projectId, String readMeContent, LocalDateTime createTime, Integer createUserId) {
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(projectId);
		resource.setKey(ProjectResource.README_KEY);
		resource.setName(ProjectResource.README_NAME);
		resource.setResourceType(ProjectResourceType.FILE);
		resource.setAppType(AppType.UNKNOWN);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(2); // 排在 Main 页面之后
		resource.setCreateUserId(createUserId);
		resource.setCreateTime(createTime);
		
		ProjectResource savedProjectResource = projectResourceService.insert(resource);
		
		ProjectFile readme = new ProjectFile();
		readme.setProjectResourceId(savedProjectResource.getId());
		readme.setFileType(FileType.MARKDOWN);
		readme.setContent(readMeContent);
		projectFileDao.save(readme);
	}



}
