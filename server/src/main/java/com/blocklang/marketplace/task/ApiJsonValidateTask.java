package com.blocklang.marketplace.task;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.blocklang.marketplace.constant.MarketplaceConstant;
import com.blocklang.marketplace.data.ApiJson;
import com.blocklang.marketplace.data.ComponentJson;

import de.skuzzle.semantic.Version;

public class ApiJsonValidateTask extends AbstractRepoPublishTask {
	
	private ComponentJson componentJson;
	private ApiJson apiJson;

	public ApiJsonValidateTask(MarketplacePublishContext context) {
		super(context);
		
		this.componentJson = context.getComponentJson();
		this.apiJson = context.getApiJson();
	}

	/**
	 * 包含两种校验：
	 * 
	 * <ol>
	 * <li>对 api.json 本身的 schema 和值的校验
	 * <li>对 component.json 和 api.json 的比较校验，确保两者之间的配置信息有着合理的实现关系
	 * </ol>
	 */
	@Override
	public Optional<Boolean> run() {
		boolean success = true;
		
		// 一、自检
		// 在这里对 api.json 中的 schema 和内容进行校验
		// name
		boolean nameHasError = false;
		String name = apiJson.getName();
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
		
		// displayName
		boolean displayNameHasError = false;
		String displayName = apiJson.getDisplayName();
		if(StringUtils.isNotBlank(displayName) && com.blocklang.core.util.StringUtils.byteLength(displayName) > 60) {
			logger.error("displayName - 值的长度不能超过60个字节(一个汉字占两个字节)");
			displayNameHasError = false;
		}
		
		// version
		boolean versionHasError = false;
		String version = apiJson.getVersion();
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
		
		// category
		boolean categoryHasError = false;
		String category = apiJson.getCategory();
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
		
		// description
		boolean descriptionHasError = false;
		String description = apiJson.getDescription();
		if(com.blocklang.core.util.StringUtils.byteLength(description) > 500) {
			logger.error("description - 值的长度不能超过500个字节(一个汉字占两个字节)");
			descriptionHasError = true;
		}
		
		success = !(nameHasError || displayNameHasError || versionHasError || categoryHasError || descriptionHasError);
		
		// 二、对比校验
		// 然后比较 api.json 和 component.json 的值，看是否匹配
		
		// api.json 和 component.json 中的 category 要保持一致
		if(success) {
			logger.info("校验 {0} 中的 category 与 {1} 中的 category 是否相同", 
					MarketplaceConstant.FILE_NAME_API, 
					MarketplaceConstant.FILE_NAME_COMPONENT);
			if(componentJson.getCategory().equalsIgnoreCase(apiJson.getCategory())) {
				logger.info("两者相同");
			} else {
				logger.error("两者不同");
				success = false;
			}
		}
		
		String[] apiComponents = null;
		// 确定组件库实现了 api 库中的所有组件
		if(success) {
			logger.info("校验 API 库 {0} 的 components 中定义组件是否都在组件库中 {1} 的 components 中定义",
					MarketplaceConstant.FILE_NAME_API, MarketplaceConstant.FILE_NAME_COMPONENT);
			logger.info("注意：只判断路径中的组件名称是否相同");
			
			String[] components = componentJson.getComponents();
			apiComponents = apiJson.getComponents();
			
			String[] componentNames = Arrays.stream(components).map(component -> {
				String[] segments = component.split("/");
				return segments[segments.length - 1].toLowerCase();
			}).toArray(String[]::new);
			
			for(String apiComponent : apiComponents) {
				String[] apiSegments = apiComponent.split("/");
				String apiComponentName = apiSegments[apiSegments.length - 1];
				boolean matched = false;
				for(String componentName : componentNames) {
					// 存的是完整路径，完整路径肯定不相等，但最后一个 / 后的值可以约定相等
					if(apiComponentName.trim().equalsIgnoreCase(componentName.trim())) {
						matched = true;
						break;
					}
				}
				if(!matched) {
					logger.error("components - 组件库中未配置(实现) API 库定义的 {0} 组件", apiComponent);
					success = false;
				}
			}
		}
		
		if(success) {
			return Optional.of(true);
		}
		return Optional.empty();
	}

}
