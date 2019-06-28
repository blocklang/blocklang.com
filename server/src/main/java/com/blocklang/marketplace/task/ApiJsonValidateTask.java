package com.blocklang.marketplace.task;

import java.util.Arrays;
import java.util.Optional;

import com.blocklang.marketplace.constant.MarketplaceConstant;
import com.blocklang.marketplace.data.ApiJson;
import com.blocklang.marketplace.data.ComponentJson;

public class ApiJsonValidateTask extends AbstractRepoPublishTask {
	
	private ComponentJson componentJson;
	private ApiJson apiJson;

	public ApiJsonValidateTask(MarketplacePublishContext context) {
		super(context);
		
		this.componentJson = context.getComponentJson();
		this.apiJson = context.getApiJson();
	}

	@Override
	public Optional<Boolean> run() {
		boolean success = true;
		
		// 校验 api.json 中的字段
		
		// TODO: 在这里对 api.json 中的 schema 和内容进行校验
		
		// 然后比较 api.json 和 component.json 的值，看是否匹配
		
		// 对比校验
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
			
			if(success) {
				logger.info("校验通过");
			}
		}
		
		if(success) {
			return Optional.of(true);
		}
		return Optional.empty();
	}

}
