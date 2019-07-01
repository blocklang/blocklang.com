package com.blocklang.marketplace.task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.Ref;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.git.exception.GitTagFailedException;
import com.blocklang.marketplace.constant.Language;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.data.ApiJson;
import com.blocklang.marketplace.data.ComponentJson;
import com.blocklang.marketplace.data.changelog.Change;
import com.blocklang.marketplace.data.changelog.ChangeLog;
import com.blocklang.marketplace.data.changelog.ComponentChangeLogs;
import com.blocklang.marketplace.data.changelog.NewWidgetChange;
import com.blocklang.marketplace.model.ApiComponent;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.model.ComponentRepoVersion;

import de.skuzzle.semantic.Version;

/**
 * 发布的内容为：
 * 
 * <p>
 * <ol>
 * <li> 组件库的最新版
 * <li> API 库，从组件库引用的版本到前面所有未安装版本
 * </ol>
 * 
 * 如果 API 的前面版本已发布，则跳过。
 * 
 * 逐个安装，如果某个文件安装失败了，则记录下来，并开始安装下一个组件，
 * 在最后再删除已安装的内容，只有都安装成功后，才在 api_changelog 中登记
 * </p>
 * 
 * @author Zhengwei Jin
 *
 */
public class ApiChangeLogsSetupGroupTask extends AbstractRepoPublishTask {

	private ComponentRepoPublishTask publishTask;
	private ComponentJson componentJson;
	private ApiJson apiJson;
	
	private ComponentRepoDao componentRepoDao;
	private ComponentRepoVersionDao componentRepoVersionDao;
	private ApiRepoDao apiRepoDao;
	private ApiRepoVersionDao apiRepoVersionDao;
	
	public ApiChangeLogsSetupGroupTask(
			MarketplacePublishContext marketplacePublishContext, 
			ComponentRepoDao componentRepoDao,
			ComponentRepoVersionDao componentRepoVersionDao,
			ApiRepoDao apiRepoDao,
			ApiRepoVersionDao apiRepoVersionDao) {
		super(marketplacePublishContext);
		
		this.publishTask = context.getPublishTask();
		this.componentJson = context.getComponentJson();
		this.apiJson = context.getApiJson();
		
		this.componentRepoDao = componentRepoDao;
		this.componentRepoVersionDao = componentRepoVersionDao;
		this.apiRepoDao = apiRepoDao;
		this.apiRepoVersionDao = apiRepoVersionDao;
	}

	@Override
	public Optional<Boolean> run() {
		boolean success = true;
		
		// 保存组件库基本信息和最新版本信息
		logger.info("开始保存组件库基本信息");
		ComponentRepo savedCompRepo = saveComponentRepo();
		logger.info("保存成功");
		
		logger.info("开始保存组件库的 {0} 版本信息", context.getComponentRepoLatestVersion());
		ComponentRepoVersion savedCompRepoVersion = saveComponentRepoVersion(savedCompRepo.getId());
		logger.info("保存成功");
		
		logger.info("开始保存 API 库基本信息");
		ApiRepo savedApiRepo = null;
		Optional<ApiRepo> apiRepoOption = apiRepoDao.findByNameAndCreateUserId(apiJson.getName(), publishTask.getCreateUserId());
		if(apiRepoOption.isPresent()) {
			savedApiRepo = apiRepoOption.get();
			logger.info("已存在");
		} else {
			savedApiRepo = saveApiRepo();
			logger.info("保存成功"); 
		}
		
		// 保存 API 版本信息，API 是逐个版本累加的，所以要发布引用版本，就要发布之前的版本
		logger.info("开始保存 API 库的版本信息");
		List<ApiRepoVersion> savedApiRepoVersions = null;
		Optional<ApiRepoVersion> apiRepoVersionOption = apiRepoVersionDao.findByApiRepoIdAndVersion(savedApiRepo.getId(), apiJson.getVersion());
		if(apiRepoVersionOption.isPresent()) {
			logger.info("已存在");
			savedApiRepoVersions = Collections.emptyList();
		} else {
			List<String> apiVersions = new ArrayList<String>();
			try {
				List<Ref> tags = GitUtils.getTags(context.getLocalApiRepoPath().getRepoSourceDirectory());
				for(Ref ref : tags) {
					Optional<String> versionOption = GitUtils.getVersionFromRefName(ref.getName());
					apiVersions.add(versionOption.get());
				}
			} catch (GitTagFailedException e) {
				logger.error(e);
			}
			
			List<ApiRepoVersion> setupedRepoVersions = apiRepoVersionDao.findAllByApiRepoId(savedApiRepo.getId());
			// 删除已安装的版本
			for(ApiRepoVersion apiRepoVersion : setupedRepoVersions) {
				apiVersions.remove(apiRepoVersion.getVersion());
			}
			// 删除比指定的版本号更大的版本
			Version currentApiRepoVersion = Version.parseVersion(apiJson.getVersion());
			apiVersions.removeIf(apiVersion -> {
				Version version = Version.parseVersion(apiVersion);
				return version.isGreaterThan(currentApiRepoVersion);
			});
			Integer savedApiRepoId = savedApiRepo.getId();
			savedApiRepoVersions = apiVersions
					.stream()
					.map(apiVersion -> this.saveApiRepoVersion(savedApiRepoId, apiVersion))
					.collect(Collectors.toList());
		}

		// 增量安装 API 变更
		// 先循环组件，再嵌套循环版本
		List<ComponentChangeLogs> allChangeLogs = context.getChangeLogs();
		for(ComponentChangeLogs component : allChangeLogs) {
			String latestPublishVersion = component.getLatestPublishVersion();
			if(latestPublishVersion == null) {
				// 第一次发布
				for(ChangeLog changeLog : component.getChangeLogs()) {
					for(Change change : changeLog.getChanges()) {
						if(NewWidgetChange.class.isAssignableFrom(change.getClass())) {
							NewWidgetChange newWidgetChange = (NewWidgetChange)change;
							// TODO: 
							Integer apiRepoVersionId = null;
							this.saveWidget(apiRepoVersionId, newWidgetChange);
						} else {
							logger.error("不是有效的变更操作");
						}
					}
					
					latestPublishVersion = changeLog.getVersion();
				}
			} else {
				// 需要在上一个版本的基础上增量发布
				throw new UnsupportedOperationException();
			}
		}
		
		
		
		
		if(success) {
			return Optional.of(true);
		}
		return Optional.empty();
	}

	private void saveWidget(Integer apiRepoVersionId, NewWidgetChange newWidgetChange) {
		// code
		// 获取下一个有效的 code
		ApiComponent component = new ApiComponent();
		
	}

	private ApiRepoVersion saveApiRepoVersion(Integer savedApiRepoId, String apiVersion) {
		ApiRepoVersion apiRepoVersion = new ApiRepoVersion();
		apiRepoVersion.setApiRepoId(savedApiRepoId);
		apiRepoVersion.setVersion(apiVersion);
		apiRepoVersion.setCreateUserId(publishTask.getCreateUserId());
		apiRepoVersion.setCreateTime(LocalDateTime.now());
		return apiRepoVersionDao.save(apiRepoVersion);
	}

	private ApiRepo saveApiRepo() {
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
		return apiRepoDao.save(apiRepo);
	}

	private ComponentRepoVersion saveComponentRepoVersion(Integer compRepoId) {
		ComponentRepoVersion compRepoVersion = new ComponentRepoVersion();
		compRepoVersion.setComponentRepoId(compRepoId);
		// 确认 context.getComponentRepoLatestVersion() 的值与 componentJson.getVersion() 的值相同
		compRepoVersion.setVersion(componentJson.getVersion().trim());
		compRepoVersion.setCreateUserId(publishTask.getCreateUserId());
		compRepoVersion.setCreateTime(LocalDateTime.now());
		return componentRepoVersionDao.save(compRepoVersion);
	}

	private ComponentRepo saveComponentRepo() {
		ComponentRepo repo = new ComponentRepo();
		repo.setGitRepoUrl(publishTask.getGitUrl());
		repo.setGitRepoWebsite(context.getLocalComponentRepoPath().getWebsite());
		repo.setGitRepoOwner(context.getLocalComponentRepoPath().getOwner());
		repo.setGitRepoName(context.getLocalComponentRepoPath().getRepoName());
		repo.setName(componentJson.getName().trim()); // name 必填
		repo.setVersion(componentJson.getVersion().trim()); // version 必填
		repo.setLabel(componentJson.getDisplayName());
		repo.setDescription(componentJson.getDescription());
		repo.setCategory(RepoCategory.fromValue(componentJson.getCategory().trim()));
		repo.setLanguage(Language.fromValue(componentJson.getLanguage().trim()));
		if(StringUtils.isNotBlank(componentJson.getIcon())) {
			repo.setLogoPath(componentJson.getIcon());
		}
		repo.setCreateUserId(publishTask.getCreateUserId());
		repo.setCreateTime(LocalDateTime.now());
		
		return componentRepoDao.save(repo);
	}

}
