package com.blocklang.develop.model;

import java.time.LocalDateTime;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;

import com.blocklang.core.constant.Constant;
import com.blocklang.core.constant.GitFileStatus;
import com.blocklang.core.model.PartialOperateFields;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.DeviceType;
import com.blocklang.develop.constant.IconClass;
import com.blocklang.develop.constant.RepositoryResourceType;
import com.blocklang.develop.constant.converter.AppTypeConverter;
import com.blocklang.develop.constant.converter.DeviceTypeConverter;
import com.blocklang.develop.constant.converter.RepositoryResourceTypeConverter;
import com.nimbusds.oauth2.sdk.util.StringUtils;

@Entity
@Table(name = "repository_resource", 
	uniqueConstraints = {
			@UniqueConstraint(columnNames = { "repository_id", "parent_id", "resource_type", "app_type", "resource_key" }),
			@UniqueConstraint(columnNames = { "repository_id", "parent_id", "resource_type", "app_type", "resource_name" })
	})
public class RepositoryResource extends PartialOperateFields{

	private static final long serialVersionUID = -7591405398132869438L;
	
	/**
	 * 首页
	 */
	public static final String MAIN_KEY = "main";
	public static final String MAIN_NAME = "首页";
	
	public static final String APP_KEY = "app";
	public static final String APP_NAME = "app";
	
	// 约定：系统自带的文件名，一律使用大写字母
	public static final String README_KEY = "README";
	public static final String README_NAME = "README.md";
	public static final Integer README_SEQ = 1;
	
	public static final String BUILD_KEY = "BUILD";
	public static final String BUILD_NAME = "BUILD.json";
	public static final Integer BUILD_SEQ = 2;
	
	public static final String DEPENDENCY_KEY = "DEPENDENCY";
	public static final String DEPENDENCY_NAME = "DEPENDENCY.json";
	
	public static final String PROJECT_JSON_KEY = "PROJECT";
	public static final String PROJECT_JSON_NAME = "PROJECT.json";

	@Column(name = "repository_id", nullable = false)
	private Integer repositoryId;
	
	@Column(name = "resource_key", nullable = false, length = 32)
	private String key;
	
	// name 是显示名，如果没有设置，则取 key 的值
	@Column(name = "resource_name", length = 32)
	private String name;
	
	@Column(name = "resource_desc", length = 64)
	private String description;

	@Convert(converter = RepositoryResourceTypeConverter.class)
	@Column(name = "resource_type", nullable = false, length = 2)
	private RepositoryResourceType resourceType;
	
	@Convert(converter = AppTypeConverter.class)
	@Column(name = "app_type", nullable = false, length = 2)
	private AppType appType;
	
	@Convert(converter = DeviceTypeConverter.class)
	@Column(name = "device_type", nullable = false, length = 2)
	private DeviceType deviceType;
	
	@Column(name = "parent_id", nullable = false)
	private Integer parentId = Constant.TREE_ROOT_ID;

	@Column(name = "seq", nullable = false)
	private Integer seq;
	
	@Transient
	private String latestCommitId;
	@Transient
	private String latestShortMessage;
	@Transient
	private String latestFullMessage;
	@Transient
	private LocalDateTime latestCommitTime;
	@Transient 
	private GitFileStatus gitStatus;

	public Integer getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(Integer repositoryId) {
		this.repositoryId = repositoryId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public RepositoryResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(RepositoryResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public AppType getAppType() {
		return appType;
	}

	public void setAppType(AppType appType) {
		this.appType = appType;
	}

	public DeviceType getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public String getLatestCommitId() {
		return latestCommitId;
	}

	public void setLatestCommitId(String latestCommitId) {
		this.latestCommitId = latestCommitId;
	}

	public String getLatestShortMessage() {
		return latestShortMessage;
	}

	public void setLatestShortMessage(String latestShortMessage) {
		this.latestShortMessage = latestShortMessage;
	}

	public String getLatestFullMessage() {
		return latestFullMessage;
	}

	public void setLatestFullMessage(String latestFullMessage) {
		this.latestFullMessage = latestFullMessage;
	}

	public LocalDateTime getLatestCommitTime() {
		return latestCommitTime;
	}

	public void setLatestCommitTime(LocalDateTime latestCommitTime) {
		this.latestCommitTime = latestCommitTime;
	}

	/**
	 * 是否项目入口
	 * 
	 * @return
	 */
	public Boolean isMain() {
		if(!isPage()) {
			return false;
		}
		
		if(appType == AppType.WEB && MAIN_KEY.equals(this.key)) {
			return true;
		}
		
		if(appType == AppType.MINI_PROGRAM && APP_KEY.equals(this.key)) {
			return true;
		}
		
		return false;
	}
	
	public Boolean isProject() {
		return RepositoryResourceType.PROJECT.equals(this.resourceType);
	}

	public Boolean isTemplet() {
		return RepositoryResourceType.PAGE_TEMPLET.equals(this.resourceType);
	}

	public Boolean isGroup() {
		return RepositoryResourceType.GROUP.equals(this.resourceType);
	}

	public Boolean isPage() {
		return RepositoryResourceType.PAGE.equals(this.resourceType);
	}

	public Boolean isReadme() {
		return isFile() && README_KEY.equals(this.key);
	}

	public Boolean isService() {
		return RepositoryResourceType.SERVICE.equals(this.resourceType);
	}
	
	public Boolean isFile() {
		return RepositoryResourceType.FILE.equals(this.resourceType);
	}
	
	public Boolean isDependence() {
		return RepositoryResourceType.DEPENDENCE.equals(this.resourceType);
	}
	
	public Boolean isBuildConfig() {
		return RepositoryResourceType.BUILD.equals(this.resourceType);
	}

	public String getIcon() {
		if(isPage()) {
			if(isMain()) {
				return IconClass.HOME;
			}
			return this.appType.getIcon();
		}
		
		if(isProject()) {
			return IconClass.PROJECT;
		}
		
		if(isGroup()) {
			return IconClass.GROUP;
		}
		if(isTemplet()) {
			return IconClass.TEMPLET;
		}
		if(isReadme()) {
			return IconClass.README;
		}
		if(isService()) {
			return IconClass.SERVICE;
		}
		if(isDependence()) {
			return IconClass.DEPENDENCE;
		}
		if(isBuildConfig()) {
			return IconClass.BUILD;
		}
		return null;

	}

	@Transient
	private MessageSource messageSource;
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	public String getTitle() {
		Assert.notNull(this.messageSource, "不能为空");

		String i18nKey = "";
		if(isPage()) {
			if(isMain()) {
				i18nKey = "resourceTitle.main";
			}else {
				i18nKey = "resourceTitle.program";
			}
		} else if(isGroup()) {
			i18nKey = "resourceTitle.function";
		} else if(isProject()) {
			if(appType.equals(AppType.MINI_PROGRAM)) {
				i18nKey = "resourceTitle.project.miniProgram";
			} else if(appType.equals(AppType.WEB)) {
				i18nKey = "resourceTitle.project.web";
			}
		} else if(isTemplet()) {
			i18nKey = "resourceTitle.templet";
		} else if(isReadme()) {
			i18nKey = "resourceTitle.readme";
		}else if(isService()) {
			i18nKey = "resourceTitle.service";
		}else if(isBuildConfig()) {
			i18nKey = "resourceTitle.build";
		}
		
		if(StringUtils.isBlank(i18nKey)) {
			return "";
		}
		
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(i18nKey, null, locale);
	}

	/**
	 * 获取文件名，该文件名是在 git 仓库中存储时，使用的文件名。
	 * 
	 * @return
	 */
	public String getFileName() {
		// 所有资源的名字使用统一后缀 json，因为页面类型信息已经存在模型文件中了。
//		if(isPage()) {
//			return this.key + ".page." + this.appType.getValue() + ".json";
//		}
//		if(isTemplet()) {
//			return this.key + ".page.tmpl.json";
//		}
//		if(isFile() || isDependence() || isBuildConfig()) {
//			return this.name;
//		}
//		if(isService()) {
//			return this.key + ".api.json";
//		}
//
//		return "";
		
		if(isFile()) {
			return this.getName();
		}
		return this.key + ".json";
	}

	public GitFileStatus getGitStatus() {
		return gitStatus;
	}

	public void setGitStatus(GitFileStatus gitStatus) {
		this.gitStatus = gitStatus;
	}
	
}
