package com.blocklang.marketplace.apiparser;

import java.util.List;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.data.changelog.Widget;
import com.blocklang.marketplace.data.changelog.WidgetEvent;
import com.blocklang.marketplace.data.changelog.WidgetProperty;
import com.blocklang.marketplace.task.CodeGenerator;

public class AddWidgetProperty implements WidgetOperator<List<WidgetProperty>>{

	@Override
	public boolean apply(WidgetOperatorContext context, List<WidgetProperty> data) {
		if(!validate(context, data)) {
			return false;
		}
		
		Widget widget = context.getSelectedWidget();
		
		String seed = getMaxPropertyCode(widget);
		CodeGenerator codeGen = new CodeGenerator(seed);
		
		data.forEach(prop -> prop.setCode(codeGen.next()));
		widget.getProperties().addAll(data);
		return true;
	}

	private boolean validate(WidgetOperatorContext context, List<WidgetProperty> data) {
		CliLogger logger = context.getLogger();
		Widget widget = context.getSelectedWidget();
		if(widget == null) {
			context.getLogger().error("无法执行 addProperty 操作，因为尚未创建 Widget");
			return false;
		}
		
		var existProperties = widget.getProperties();
		return data.stream().allMatch(addedProperty -> {
			var propNameUsed = existProperties
					.stream()
					.anyMatch(existProperty -> existProperty.getName().equals(addedProperty.getName()));
			if(propNameUsed) {
				logger.error("属性名 {0} 已存在", addedProperty.getName());
				return false;
			}
			return true;
		});
	}
	
	/**
	 * Widget 中的属性和事件使用相同的编码序列，将事件看作一种特殊的属性
	 * 
	 * @param widget Widget
	 * @return 下一个属性编码
	 */
	private String getMaxPropertyCode(Widget widget) {
		List<WidgetProperty> properties = widget.getProperties();
		List<WidgetEvent> events = widget.getEvents();

		// 默认值为 0，能简化后续的比较操作，如果为 null，需要加入 null check
		String propertyMaxSeed = "0"; 
		if(!properties.isEmpty()) {
			propertyMaxSeed = properties.get(properties.size() - 1).getCode();
		}
		
		String eventMaxSeed = "0";
		if(!events.isEmpty()) {
			eventMaxSeed = events.get(events.size() - 1).getCode();
		}

		return propertyMaxSeed.compareTo(eventMaxSeed) >= 0 ? propertyMaxSeed : eventMaxSeed;
	}

}
