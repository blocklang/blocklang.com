package com.blocklang.marketplace.task;

import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.transport.URIish;

import com.blocklang.core.git.GitUtils;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.data.ComponentJson;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;

import de.skuzzle.semantic.Version;

/**
 * 校验 component.json 中的字段
 * 
 * <p>
 * <h4>name</h4>
 * <ol>
 * <li>不能为空
 * <li>长度不能超过64个字节
 * <li>只能包含字母、数字、中划线和下划线
 * <li>同一个发布者没有发不过此名称的组件库
 * </ol>
 *
 * <h4>displayName</h4>
 * <ol>
 * <li>长度不能超过64个字节
 * </ol>
 *
 * <h4>version</h4>
 * <ol>
 * <li>不能为空
 * <li>必须是有效的语义化版本
 * <li>必须大于最新的版本号
 * </ol>
 *
 * <h4>category</h4>
 * <ol>
 * <li>不能为空
 * <li>只能是 “Widget”（不区分大小写）
 * </ol>
 *
 * <h4>language</h4>
 * <ol>
 * <li>不能为空
 * <li>只能是“Typescript”、“Java”（不区分大小写）
 * </ol>
 *
 * <h4>description</h4>
 * <ol>
 * <li>长度不能超过512个字节
 * </ol>
 *
 * <h4>icon</h4>
 * <ol>
 * <li>长度不能超过64个字节
 * </ol>
 *
 * <h4>api.git</h4>
 * <ol>
 * <li>不能为空
 * <li>有效的 https 协议的 git 远程仓库地址
 * <li>根据此地址能找到远程仓库
 * </ol>
 * 
 * <h4>api.version</h4>
 * <ol>
 * <li>不能为空
 * <li>有效的语义化版本号
 * </ol>
 * </p>
 * 
 * 
 * @author Zhengwei Jin
 *
 */
public class ComponentJsonValidateTask extends AbstractRepoPublishTask {

	private ComponentJson componentJson;
	
	private ComponentRepoDao componentRepoDao;
	private ComponentRepoPublishTask publishTask;
	private boolean isFirstPublish;
	
	public ComponentJsonValidateTask(MarketplacePublishContext context, 
			ComponentRepoDao componentRepoDao) {
		super(context);
		this.componentJson = context.getComponentJson();
		this.isFirstPublish = context.isFirstPublish();
		this.componentRepoDao = componentRepoDao;
	}

	@Override
	public Optional<Boolean> run() {
		// name
		boolean nameHasError = false;
		String name = componentJson.getName();
		if(StringUtils.isBlank(name)) {
			logger.error("name - 值不能为空");
			nameHasError = true;
		}
		String trimedName = Objects.toString(name, "").trim();
		if(!nameHasError) {
			// 之所以取 60 而并不是 64，因为对于用户来说， 60 好记
			if(com.blocklang.core.util.StringUtils.byteLength(trimedName) > 60) {
				logger.error("name - 值的长度不能超过60个字节(一个汉字占两个字节)");
				nameHasError = true;
			}
		}
		if(!nameHasError) {
			// 校验：只支持英文字母、数字、中划线(-)、下划线(_)、点(.)
			String regEx = "^[a-zA-Z0-9\\-\\w]+$";
			Pattern pattern = Pattern.compile(regEx);
			Matcher matcher = pattern.matcher(trimedName);
			if(!matcher.matches()) {
				logger.error("name - 值只支持英文字母、数字、中划线(-)、下划线(_)、点(.)，‘{0}’中包含非法字符", trimedName);
				nameHasError = true;
			}
		}
		if(!nameHasError) {
			if(isFirstPublish && componentRepoDao.findByNameAndCreateUserId(trimedName, publishTask.getCreateUserId()).isPresent()) {
				logger.error("name - {0} 下已注册名为 {1} 的组件库，请更换名字", publishTask.getCreateUserName(), trimedName);
				nameHasError = true;
			}
		}
		
		// displayName
		boolean displayNameHasError = false;
		String displayName = componentJson.getDisplayName();
		if(StringUtils.isNotBlank(displayName) && com.blocklang.core.util.StringUtils.byteLength(displayName) > 60) {
			logger.error("displayName - 值的长度不能超过60个字节(一个汉字占两个字节)");
			displayNameHasError = false;
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
			if(!isFirstPublish) {
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
		
		// components
		boolean componentsHasError = false;
		String[] components = componentJson.getComponents();
		if(components.length == 0) {
			logger.error("components - 共发现 0 个组件，请先配置可发布的组件");
			componentsHasError = true;
		}

		boolean success = !(nameHasError || displayNameHasError || versionHasError || categoryHasError || languageHasError
				|| descriptionHasError || iconHasError || apiGitHasError || apiVersionHasError || componentsHasError);
		
		if(success) {
			return Optional.of(true);
		}
		return Optional.empty();
	}

}
