package com.blocklang.marketplace.task;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.blocklang.core.git.GitUtils;
import com.blocklang.develop.constant.AppType;
import com.blocklang.marketplace.constant.ChangelogExecuteResult;
import com.blocklang.marketplace.constant.ComponentAttrValueType;
import com.blocklang.marketplace.constant.Language;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.dao.ApiChangeLogDao;
import com.blocklang.marketplace.dao.ApiComponentAttrDao;
import com.blocklang.marketplace.dao.ApiComponentAttrFunArgDao;
import com.blocklang.marketplace.dao.ApiComponentAttrValOptDao;
import com.blocklang.marketplace.dao.ApiComponentDao;
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
import com.blocklang.marketplace.data.changelog.WidgetEvent;
import com.blocklang.marketplace.data.changelog.WidgetEventArgument;
import com.blocklang.marketplace.data.changelog.WidgetProperty;
import com.blocklang.marketplace.data.changelog.WidgetPropertyOption;
import com.blocklang.marketplace.model.ApiChangeLog;
import com.blocklang.marketplace.model.ApiComponent;
import com.blocklang.marketplace.model.ApiComponentAttr;
import com.blocklang.marketplace.model.ApiComponentAttrFunArg;
import com.blocklang.marketplace.model.ApiComponentAttrValOpt;
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
	private ApiComponentDao apiComponentDao;
	private ApiComponentAttrDao apiComponentAttrDao;
	private ApiComponentAttrValOptDao apiComponentAttrValOptDao;
	private ApiComponentAttrFunArgDao apiComponentAttrFunArgDao;
	private ApiChangeLogDao apiChangeLogDao;
	
	public ApiChangeLogsSetupGroupTask(
			MarketplacePublishContext marketplacePublishContext, 
			ComponentRepoDao componentRepoDao,
			ComponentRepoVersionDao componentRepoVersionDao,
			ApiRepoDao apiRepoDao,
			ApiRepoVersionDao apiRepoVersionDao,
			ApiComponentDao apiComponentDao,
			ApiComponentAttrDao apiComponentAttrDao,
			ApiComponentAttrValOptDao apiComponentAttrValOptDao,
			ApiComponentAttrFunArgDao apiComponentAttrFunArgDao,
			ApiChangeLogDao apiChangeLogDao) {
		super(marketplacePublishContext);
		
		this.publishTask = context.getPublishTask();
		this.componentJson = context.getComponentJson();
		this.apiJson = context.getApiJson();
		
		this.componentRepoDao = componentRepoDao;
		this.componentRepoVersionDao = componentRepoVersionDao;
		this.apiRepoDao = apiRepoDao;
		this.apiRepoVersionDao = apiRepoVersionDao;
		this.apiComponentDao = apiComponentDao;
		this.apiComponentAttrDao = apiComponentAttrDao;
		this.apiComponentAttrValOptDao = apiComponentAttrValOptDao;
		this.apiComponentAttrFunArgDao = apiComponentAttrFunArgDao;
		this.apiChangeLogDao = apiChangeLogDao;
	}

	/**
	 * 因为组件库依赖于 API 库，所以先保存 API 库，再保存组件库
	 */
	@Override
	public Optional<Boolean> run() {
		boolean success = true;
		
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
		Integer currentApiRepoVersionId = null;
		Optional<ApiRepoVersion> apiRepoVersionOption = apiRepoVersionDao.findByApiRepoIdAndVersion(savedApiRepo.getId(), apiJson.getVersion());
		if(apiRepoVersionOption.isPresent()) {
			logger.info("已存在");
			savedApiRepoVersions = Collections.emptyList();
			currentApiRepoVersionId = apiRepoVersionOption.get().getId();
		} else {
			List<String> apiRepoVersions = context.getApiRepoVersions();
			
			List<ApiRepoVersion> setupedRepoVersions = apiRepoVersionDao.findAllByApiRepoId(savedApiRepo.getId());
			// 删除已安装的版本
			for(ApiRepoVersion apiRepoVersion : setupedRepoVersions) {
				apiRepoVersions.remove(apiRepoVersion.getVersion());
			}
			// 删除比指定的版本号更大的版本
			Version currentApiRepoVersion = Version.parseVersion(apiJson.getVersion());
			apiRepoVersions.removeIf(apiVersion -> {
				Version version = Version.parseVersion(apiVersion);
				return version.isGreaterThan(currentApiRepoVersion);
			});
			Integer savedApiRepoId = savedApiRepo.getId();
			savedApiRepoVersions = apiRepoVersions
					.stream()
					.map(apiVersion -> this.saveApiRepoVersion(savedApiRepoId, apiVersion))
					.collect(Collectors.toList());
			
			// 最后一个，必须是当前引用的 API 版本信息
			if(savedApiRepoVersions.size() > 0) {
				currentApiRepoVersionId = savedApiRepoVersions.get(savedApiRepoVersions.size() - 1).getId();
			} else {
				logger.error("");
				success = false;
			}
			
		}
		
		// 保存组件库基本信息和最新版本信息
		logger.info("开始保存组件库基本信息");
		ComponentRepo savedCompRepo = saveComponentRepo(savedApiRepo.getId());
		logger.info("保存成功");
		
		logger.info("开始保存组件库的 {0} 版本信息", context.getComponentRepoLatestVersion());
		saveComponentRepoVersion(savedCompRepo.getId(), currentApiRepoVersionId);
		logger.info("保存成功");

		// 增量安装 API 变更
		// 先循环组件，再嵌套循环版本
		List<ComponentChangeLogs> allChangeLogs = context.getChangeLogs();
		for(ComponentChangeLogs component : allChangeLogs) {
			String componentCodeSeed = null;

			if(component.isFirstSetup()) {
				CodeGenerator componentCodeGenerator = new CodeGenerator(componentCodeSeed);
				for(ChangeLog changeLog : component.getChangeLogs()) {
					Optional<ApiRepoVersion> currentApiRepoVersionOption = apiRepoVersionDao.findByApiRepoIdAndVersion(savedApiRepo.getId(), changeLog.getVersion());
					if(currentApiRepoVersionOption.isPresent()) {
						Integer apiRepoVersionId = currentApiRepoVersionOption.get().getId();
						for(Change change : changeLog.getChanges()) {
							if(NewWidgetChange.class.isAssignableFrom(change.getClass())) {
								NewWidgetChange newWidgetChange = (NewWidgetChange)change;
								this.newWidget(apiRepoVersionId, newWidgetChange, componentCodeGenerator);
							} else {
								logger.error("不是有效的变更操作");
							}
						}
					} else {
						logger.error("在数据库中未能找到 apiRepoId = {0} 和 version = {1} 的记录", savedApiRepo.getId(), changeLog.getVersion());
						success = false;
					}
				}
			} else if(component.hasNewVersion()) {
				// 如果之前发布过，需要在上一个版本的基础上增量发布
				throw new UnsupportedOperationException();
			} else {
				// 此部件没有新版本，此处什么也不做
			}
		}
		
		// 当所有 change log 都安装完后，再保存安装记录
		int executeOrder = 1;
		for(ComponentChangeLogs component : allChangeLogs) {
			for(ChangeLog changeLog : component.getChangeLogs()) {
				Optional<ApiRepoVersion> currentApiRepoVersionOption = apiRepoVersionDao.findByApiRepoIdAndVersion(savedApiRepo.getId(), changeLog.getVersion());
				
				ApiChangeLog apiChangeLog = new ApiChangeLog();
				apiChangeLog.setApiRepoId(savedApiRepo.getId());
				apiChangeLog.setChangelogId(changeLog.getId());
				apiChangeLog.setChangelogAuthor(changeLog.getAuthor());
				apiChangeLog.setChangelogFileName(component.getComponentName() + "/changelog/" + changeLog.getFileName());
				apiChangeLog.setExecuteTime(LocalDateTime.now());
				apiChangeLog.setExecuteOrder(executeOrder);
				apiChangeLog.setExecuteResult(ChangelogExecuteResult.SUCCESS);
				apiChangeLog.setMd5Sum(changeLog.getMd5Sum());
				apiChangeLog.setDeploymentId(currentApiRepoVersionOption.get().getId());
				apiChangeLog.setCreateUserId(publishTask.getCreateUserId());
				apiChangeLog.setCreateTime(LocalDateTime.now());
				
				apiChangeLogDao.save(apiChangeLog);
				
				executeOrder++;
			}
		}
		
		
		if(success) {
			return Optional.of(true);
		}
		return Optional.empty();
	}

	// 注意，方法名要与操作名相同
	private void newWidget(Integer apiRepoVersionId, NewWidgetChange newWidgetChange, CodeGenerator componentCodeGenerator) {
		// code
		// 获取下一个有效的 code
		ApiComponent savedApiComponent = saveApiComponent(apiRepoVersionId, newWidgetChange, componentCodeGenerator);
		
		CodeGenerator componentAttrCodeGenerator = new CodeGenerator(null);
		for(WidgetProperty property : newWidgetChange.getProperties()) {
			Integer apiComponentId = savedApiComponent.getId();
			ApiComponentAttr savedApiComponentAttr = saveComponentAttr(apiComponentId, property, componentAttrCodeGenerator);
		
			CodeGenerator componentAttrOptCodeGenerator = new CodeGenerator(null);
			for(WidgetPropertyOption option : property.getOptions()) {
				Integer apiComponentAttrId = savedApiComponentAttr.getId();
				saveApiComponentAttrOpt(apiComponentAttrId, option, componentAttrOptCodeGenerator);
			}
		}
		
		for(WidgetEvent event : newWidgetChange.getEvents()) {
			Integer apiComponentId = savedApiComponent.getId();
			ApiComponentAttr savedApiComponentAttr = saveComponentAttr(apiComponentId, event, componentAttrCodeGenerator);
		
			CodeGenerator componentEventArgCodeGenerator = new CodeGenerator(null);
			for(WidgetEventArgument argument : event.getArguments()) {
				Integer apiComponentAttrId = savedApiComponentAttr.getId();
				saveApiComponentFunArg(apiComponentAttrId, argument, componentEventArgCodeGenerator);
			}
		}
	}

	private ApiComponent saveApiComponent(Integer apiRepoVersionId, NewWidgetChange newWidgetChange, CodeGenerator componentCodeGenerator) {
		ApiComponent apiComponent = new ApiComponent();
		apiComponent.setApiRepoVersionId(apiRepoVersionId);
		apiComponent.setCode(componentCodeGenerator.next());
		apiComponent.setName(newWidgetChange.getName());
		apiComponent.setLabel(newWidgetChange.getLabel());
		// appType 不要放在 API 仓库上
		apiComponent.setDescription(newWidgetChange.getDescription());
		apiComponent.setCreateTime(LocalDateTime.now());
		apiComponent.setCreateUserId(publishTask.getCreateUserId());
		
		ApiComponent savedApiComponent = apiComponentDao.save(apiComponent);
		return savedApiComponent;
	}

	private void saveApiComponentFunArg(Integer apiComponentAttrId, WidgetEventArgument argument, CodeGenerator componentEventArgCodeGenerator) {
		ApiComponentAttrFunArg arg = new ApiComponentAttrFunArg();
		arg.setApiComponentAttrId(apiComponentAttrId);
		arg.setCode(componentEventArgCodeGenerator.next());
		arg.setName(argument.getName());
		arg.setLabel(argument.getLabel());
		arg.setValueType(ComponentAttrValueType.fromKey(argument.getValueType()));
		arg.setDefaultValue(argument.getDefaultValue().toString());
		arg.setDescription(argument.getDescription());

		this.apiComponentAttrFunArgDao.save(arg);
	}

	private void saveApiComponentAttrOpt(Integer apiComponentAttrId, WidgetPropertyOption option, CodeGenerator componentAttrOptCodeGenerator) {
		ApiComponentAttrValOpt opt = new ApiComponentAttrValOpt();
		opt.setApiComponentAttrId(apiComponentAttrId);
		opt.setCode(componentAttrOptCodeGenerator.next());
		opt.setValue(option.getValue());
		opt.setLabel(option.getLabel());
		opt.setDescription(option.getDescription());
		this.apiComponentAttrValOptDao.save(opt);
	}

	private ApiComponentAttr saveComponentAttr(Integer apiComponentId, WidgetProperty property, CodeGenerator componentAttrCodeGenerator) {
		ApiComponentAttr attr = new ApiComponentAttr();
		attr.setApiComponentId(apiComponentId);
		attr.setCode(componentAttrCodeGenerator.next());
		attr.setName(property.getName());
		attr.setLabel(property.getLabel());
		attr.setDescription(property.getDescription());
		attr.setValueType(ComponentAttrValueType.fromKey(property.getValueType()));
		attr.setDefaultValue(property.getDefaultValue().toString());
		
		return apiComponentAttrDao.save(attr);
	}
	
	private ApiComponentAttr saveComponentAttr(Integer apiComponentId, WidgetEvent event, CodeGenerator componentAttrCodeGenerator) {
		ApiComponentAttr attr = new ApiComponentAttr();
		attr.setApiComponentId(apiComponentId);
		attr.setCode(componentAttrCodeGenerator.next());
		attr.setName(event.getName());
		attr.setLabel(event.getLabel());
		attr.setValueType(ComponentAttrValueType.fromKey(event.getValueType()));
		attr.setDescription(event.getDescription());
		
		return apiComponentAttrDao.save(attr);
	}

	private ApiRepoVersion saveApiRepoVersion(Integer savedApiRepoId, String apiVersion) {
		ApiRepoVersion apiRepoVersion = new ApiRepoVersion();
		apiRepoVersion.setApiRepoId(savedApiRepoId);
		apiRepoVersion.setVersion(apiVersion);
		apiRepoVersion.setGitTagName(GitUtils.getTagName(context.getApiRepoRefName()).orElse(null));
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

	private ComponentRepoVersion saveComponentRepoVersion(Integer compRepoId, Integer apiRepoVersionId) {
		ComponentRepoVersion compRepoVersion = new ComponentRepoVersion();
		compRepoVersion.setComponentRepoId(compRepoId);
		compRepoVersion.setApiRepoVersionId(apiRepoVersionId);
		// TODO: 确认 context.getComponentRepoLatestVersion() 的值与 componentJson.getVersion() 的值相同
		compRepoVersion.setVersion(componentJson.getVersion().trim());
		compRepoVersion.setGitTagName(context.getComponentRepoLatestTagName());
		compRepoVersion.setCreateUserId(publishTask.getCreateUserId());
		compRepoVersion.setCreateTime(LocalDateTime.now());
		return componentRepoVersionDao.save(compRepoVersion);
	}

	private ComponentRepo saveComponentRepo(Integer apiRepoId) {
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(apiRepoId);
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
		repo.setIsIdeExtension(componentJson.isDev());
		repo.setStd(componentJson.isStd());
		repo.setAppType(AppType.fromValue(componentJson.getAppType()));
		repo.setCreateUserId(publishTask.getCreateUserId());
		repo.setCreateTime(LocalDateTime.now());
		
		// 在这里已保存最近发布时间，不需要在最后再保存
		repo.setLastPublishTime(LocalDateTime.now());
		
		return componentRepoDao.save(repo);
	}

}
