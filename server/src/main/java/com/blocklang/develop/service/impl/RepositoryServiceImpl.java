package com.blocklang.develop.service.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;
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
import com.blocklang.core.util.DateUtil;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.FileType;
import com.blocklang.develop.constant.RepositoryResourceType;
import com.blocklang.develop.dao.RepositoryAuthorizationDao;
import com.blocklang.develop.dao.RepositoryCommitDao;
import com.blocklang.develop.dao.RepositoryDao;
import com.blocklang.develop.dao.RepositoryFileDao;
import com.blocklang.develop.dao.RepositoryResourceDao;
import com.blocklang.develop.data.GitCommitInfo;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryAuthorization;
import com.blocklang.develop.model.RepositoryCommit;
import com.blocklang.develop.model.RepositoryContext;
import com.blocklang.develop.model.RepositoryFile;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.RepositoryService;

@Service
public class RepositoryServiceImpl implements RepositoryService {
	
	private static final Logger logger = LoggerFactory.getLogger(RepositoryServiceImpl.class);

	@Autowired
	private RepositoryDao repositoryDao;
	@Autowired
	private RepositoryFileDao repositoryFileDao;
	@Autowired
	private RepositoryAuthorizationDao repositoryAuthorizationDao;
	@Autowired
	private RepositoryCommitDao repositoryCommitDao;
	@Autowired
	private PropertyService propertyService;
	@Autowired
	private RepositoryResourceDao repositoryResourceDao;
	
	// 如果需要缓存时，使用 service，不要使用 dao
	// 因为只会为 service 添加缓存
	@Autowired
	private UserService userService;
	
	@Override
	public Optional<Repository> find(String userName, String repositoryName) {
		return userService.findByLoginName(userName).flatMap(user -> {
			return repositoryDao.findByCreateUserIdAndName(user.getId(), repositoryName);
		}).map(project -> {
			project.setCreateUserName(userName);
			return project;
		});
	}

	// TODO: 添加数据库事务
	@Override
	public Repository createRepository(UserInfo user, Repository project) {
		Repository savedRepository = saveRepository(project);
		// 为用户设置访问仓库的权限：仓库创建者具有管理员权限
		authAdminPermission(user, savedRepository);
		Pair<String, String> readme = createRepositoryReadmeFile(savedRepository);
		Pair<String, String> buildConfig = createRepositoryBuildConfig(savedRepository);
		
		Map<String, String> files = new HashMap<String, String>();
		files.put(readme.getKey(), readme.getValue());
		files.put(buildConfig.getKey(), buildConfig.getValue());
		createGitRepository(user, savedRepository, files);
		return savedRepository;
	}

	private Repository saveRepository(Repository repository) {
		return repositoryDao.save(repository);
	}

	private void authAdminPermission(UserInfo user, Repository repository) {
		RepositoryAuthorization auth = new RepositoryAuthorization();
		auth.setRepositoryId(repository.getId());
		auth.setUserId(user.getId());
		auth.setAccessLevel(AccessLevel.ADMIN);
		auth.setCreateTime(LocalDateTime.now());
		auth.setCreateUserId(repository.getCreateUserId());
		repositoryAuthorizationDao.save(auth);
	}
	
	/**
	 * @return 返回 README 的文件名和内容
	 */
	private Pair<String, String> createRepositoryReadmeFile(Repository repository) {
		RepositoryResource resource = new RepositoryResource();
		resource.setRepositoryId(repository.getId());
		resource.setKey(RepositoryResource.README_KEY);
		resource.setName(RepositoryResource.README_NAME);
		resource.setResourceType(RepositoryResourceType.FILE);
		resource.setAppType(AppType.UNKNOWN);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(RepositoryResource.README_SEQ);
		resource.setCreateUserId(repository.getCreateUserId());
		resource.setCreateTime(LocalDateTime.now());
		RepositoryResource savedProjectResource = repositoryResourceDao.save(resource);
		
		RepositoryFile readme = new RepositoryFile();
		readme.setRepositoryResourceId(savedProjectResource.getId());
		readme.setFileType(FileType.MARKDOWN);
		String content = getDefaultReadmeContent(repository.getName());
		readme.setContent(content);
		repositoryFileDao.save(readme);
		
		return Pair.of(resource.getFileName(), content);
	}

	private String getDefaultReadmeContent(String repositoryName) {
		return "# "+ repositoryName + "\r\n" + "\r\n" + "**TODO: 在这里添加详细介绍，帮助感兴趣的人快速了解您的仓库。**";
	}
	
	private Pair<String, String> createRepositoryBuildConfig(Repository repository) {
		RepositoryResource resource = new RepositoryResource();
		resource.setRepositoryId(repository.getId());
		resource.setKey(RepositoryResource.BUILD_KEY);
		resource.setName(RepositoryResource.BUILD_NAME);
		resource.setResourceType(RepositoryResourceType.BUILD);
		resource.setAppType(AppType.UNKNOWN);
		resource.setParentId(Constant.TREE_ROOT_ID);
		resource.setSeq(RepositoryResource.BUILD_SEQ);
		resource.setCreateUserId(repository.getCreateUserId());
		resource.setCreateTime(LocalDateTime.now());
		RepositoryResource savedProjectResource = repositoryResourceDao.save(resource);
		
		// TODO: 是否需要设计 BUILD_CONFIG 表？
		return Pair.of(savedProjectResource.getFileName(), "{}");
	}
	
	// 在 git 仓库中
	// 1. 创建一个 README.md 文件
	// 2. 创建一个 BUILD.json 文件
	private void createGitRepository(UserInfo user, Repository repository, Map<String, String> files) {
		propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).ifPresent(rootDir -> {
			RepositoryContext context = new RepositoryContext(user.getLoginName(), repository.getName(), rootDir);
			try {
				String commitMessage = "First Commit";
				String commitId = GitUtils
					.beginInit(context.getGitRepositoryDirectory(), user.getLoginName(), user.getEmail())
					.addFiles(files)
					.commit(commitMessage);
				
				RepositoryCommit commit = new RepositoryCommit();
				commit.setCommitId(commitId);
				commit.setCommitUserId(user.getId());
				commit.setCommitTime(LocalDateTime.now());
				commit.setRepositoryId(repository.getId());
				commit.setBranch(Constants.MASTER);
				commit.setShortMessage(commitMessage);
				commit.setCreateUserId(user.getId());
				commit.setCreateTime(LocalDateTime.now());
				
				repositoryCommitDao.save(commit);
			}catch (RuntimeException e) {
				String appName = "@" + user.getLoginName() + "/" + repository.getName();
				logger.error(String.format("为项目 %s 初始创建 git 仓库失败", appName), e);
			}
		});
	}
	
//	// 保存项目基本信息
//	Project savedProject = projectDao.save(project);
//	Integer projectId = savedProject.getId();
//	
//	LocalDateTime createTime = project.getCreateTime();
//	Integer createUserId = project.getCreateUserId();
//	
//	ProjectAuthorization auth = new ProjectAuthorization();
//	auth.setProjectId(savedProject.getId());
//	auth.setUserId(user.getId());
//	auth.setAccessLevel(AccessLevel.ADMIN); // 项目创建者具有管理员权限
//	auth.setCreateTime(LocalDateTime.now());
//	auth.setCreateUserId(createUserId);
//	projectAuthorizationDao.save(auth);
//	
//	// 保存 APP 基本信息
//	App app = new App();
//	String appName = "@" + user.getLoginName() + "/" + project.getName();
//	app.setAppName(appName);
//	app.setProjectId(savedProject.getId());
//	app.setCreateUserId(createUserId);
//	app.setCreateTime(createTime);
//	appDao.save(app);
//	
//	// 生成入口模块：Main 页面
//	ProjectResource mainPage = createMainPage(project.getId(), createTime, createUserId);
//	// 创建空页面，默认为空页面添加根节点，包括 Page 部件及其属性。
//	PageModel pageModel = projectResourceService.createPageModelWithStdPage(mainPage.getId());
//					
//	// 生成 README.md 文件
//	String readmeContent = "# "+ project.getName() + "\r\n" + "\r\n" + "**TODO: 在这里添加项目介绍，帮助感兴趣的人快速了解您的项目。**";
//	ProjectResource readme = createReadmeFile(project.getId(), readmeContent, createTime, createUserId);
//	// 生成 DEPENDENCE.json 文件
//	ProjectResource dependence = createDependeceFile(project.getId(), createTime, createUserId);
//	
//	// 创建 git 仓库
//	propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).ifPresent(rootDir -> {
//		ProjectContext context = new ProjectContext(user.getLoginName(), project.getName(), rootDir);
//		try {
//			ObjectMapper mapper = new ObjectMapper();
//			String mainPageJsonString = "{}";
//			try {
//				mainPageJsonString = mapper.writeValueAsString(pageModel);
//			} catch (JsonProcessingException e) {
//				logger.error("转换 json 失败", e);
//			}
//			
//			String commitMessage = "First Commit";
//			String commitId = GitUtils
//				.beginInit(context.getGitRepositoryDirectory(), user.getLoginName(), user.getEmail())
//				.addFile(readme.getFileName(), readmeContent)
//				.addFile(mainPage.getFileName(), mainPageJsonString)
//				.addFile(dependence.getFileName(), "{ }") // 默认为空的 json 对象，做美化排版
//				.commit(commitMessage);
//			
//			ProjectCommit commit = new ProjectCommit();
//			commit.setCommitId(commitId);
//			commit.setCommitUserId(createUserId);
//			commit.setCommitTime(LocalDateTime.now());
//			commit.setProjectId(projectId);
//			commit.setBranch(Constants.MASTER);
//			commit.setShortMessage(commitMessage);
//			commit.setCreateUserId(createUserId);
//			commit.setCreateTime(LocalDateTime.now());
//			
//			projectCommitDao.save(commit);
//		}catch (RuntimeException e) {
//			logger.error(String.format("为项目 %s 初始创建 git 仓库失败", appName), e);
//		}
//	});
//	
//	return savedProject;

	/**
	 * 生成默认模块: Main 页面
	 * @param project
	 * @param createTime
	 * @param createUserId
	 * @return
	 */
	private RepositoryResource createMainPage(Integer projectId, LocalDateTime createTime, Integer createUserId) {
		RepositoryResource resource = new RepositoryResource();
		resource.setRepositoryId(projectId);
		resource.setKey(RepositoryResource.MAIN_KEY);
		resource.setName(RepositoryResource.MAIN_NAME);
		resource.setResourceType(RepositoryResourceType.PAGE);
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
		
		// 此方法中实现了应用模板功能
		// TODO: 是否需要将应用模板逻辑，单独提取出来？
		return repositoryResourceDao.save(resource);
	}

	@Override
	public List<Repository> findCanAccessRepositoriesByUserId(Integer userId) {
		List<Repository> projects = repositoryAuthorizationDao.findAllByUserId(userId).stream().flatMap(projectAuthoriation -> {
			return repositoryDao.findById(projectAuthoriation.getRepositoryId()).stream();
		}).sorted(Comparator.comparing(Repository::getLastActiveTime).reversed()).collect(Collectors.toList());
		
		projects.forEach(each -> {
			String loginName = userService.findById(each.getCreateUserId()).map(UserInfo::getLoginName).orElse(null);
			each.setCreateUserName(loginName);
		});
		
		return projects;
	}

	@Override
	public Optional<GitCommitInfo> findLatestCommitInfo(Repository repository, String relativeFilePath) {
		Optional<GitCommitInfo> commitOption = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH).map(rootDir -> {
			RepositoryContext context = new RepositoryContext(repository.getCreateUserName(), repository.getName(), rootDir);
			RevCommit commit = GitUtils.getLatestCommit(context.getGitRepositoryDirectory(), Objects.toString(relativeFilePath, ""));
			if(commit == null) {
				return null;
			}
			
			String commitId = commit.getName();
			
			GitCommitInfo commitInfo = new GitCommitInfo();
			commitInfo.setCommitTime(DateUtil.ofSecond(commit.getCommitTime()));
			commitInfo.setShortMessage(commit.getShortMessage());
			commitInfo.setFullMessage(commit.getFullMessage());
			commitInfo.setId(commitId);
			return commitInfo;
		});
		
		commitOption.ifPresent(commitInfo -> {
			repositoryCommitDao.findByRepositoryIdAndBranchAndCommitId(repository.getId(), Constants.MASTER, commitInfo.getId()).flatMap(projectCommit -> {
				return userService.findById(projectCommit.getCommitUserId());
			}).ifPresent(user -> {
				commitInfo.setUserName(user.getLoginName());
				commitInfo.setAvatarUrl(user.getAvatarUrl());
			});
		});
		
		return commitOption;
	}

	
	@Override
	public Optional<Repository> findById(Integer repositoryId) {
		return repositoryDao.findById(repositoryId);
	}
}
