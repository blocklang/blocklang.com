package com.blocklang.core.runner.action;

import java.util.List;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.data.changelog.AddWidgetEvent;
import com.blocklang.marketplace.data.changelog.AddWidgetProperty;
import com.blocklang.marketplace.data.changelog.Change;
import com.blocklang.marketplace.data.changelog.Widget;
import com.blocklang.marketplace.data.changelog.WidgetEvent;
import com.blocklang.marketplace.data.changelog.WidgetProperty;
import com.blocklang.marketplace.task.CodeGenerator;

// 应用一个 changelog 文件中的所有 change
public class WidgetMerger {

	private List<Widget> allWidgets;
	private Widget widget;
	private CliLogger logger;
	
	public WidgetMerger(List<Widget> allWidgets, Widget widget, CliLogger logger) {
		this.allWidgets = allWidgets;
		this.widget = widget;
		this.logger = logger;
	}
	
	public boolean run(List<Change> changes, String widgetCode) {
		boolean anyOperatorsInvalid = false;
		
		for (Change change : changes) {
			if (change.getOperator().equals("createWidget")) {
				// 如果该 widget 已经存在，则显示错误信息
				Widget data = change.getData(Widget.class);

				// 校验 widget 名是否被占用
				if (allWidgets.stream().anyMatch(widget -> widget.getName().equalsIgnoreCase(data.getName()))) {
					logger.error("Widget.name {0} 已经被占用，请更换");
					anyOperatorsInvalid = true;
					break;
				}

				if (widget != null) {
					logger.error("{0} 已创建过，不能重复创建", data.getName());
					anyOperatorsInvalid = true;
					break;
				}

				if (widget == null) {
					data.setCode(widgetCode);
					// 为新建的属性设置 code
					CodeGenerator propertiesCodeGen = new CodeGenerator(null);
					data.getProperties().forEach(prop -> prop.setCode(propertiesCodeGen.next()));
					data.getEvents().forEach(event -> event.setCode(propertiesCodeGen.next()));

					widget = data;
				}
			} else if (change.getOperator().equals("addProperty")) {
				if (widget == null) {
					// 如果尚未创建 widget，则给出错误信息
					logger.error("无法执行 addProperty 操作，因为尚未创建 Widget");
					anyOperatorsInvalid = true;
					break;
				}
				// 需要先判断属性是否已经存在，如果已存在，则给出错误提示

				AddWidgetProperty addWidgetProperty = change.getData(AddWidgetProperty.class);
				// 约定 property 按照 code 排序，这样就可以取最后一个 property 的 code
				List<WidgetProperty> existProperties = widget.getProperties();

				List<WidgetProperty> addWidgetProperties = addWidgetProperty.getProperties();
				for (WidgetProperty added : addWidgetProperties) {
					// 如果属性名已被占用
					if (existProperties.stream().anyMatch(prop -> prop.getName().equals(added.getName()))) {
						logger.error("属性名 {0} 已存在", added.getName());
						anyOperatorsInvalid = true;
					}
				}

				// 新增的属性列表中，只要有一个属性名被占用，就不能应用变更
				if (!anyOperatorsInvalid) {
					// 要从 properties 和 events 中找出最大值
					String seed = getMaxPropertyCode(widget);
					CodeGenerator codeGen = new CodeGenerator(seed);
					addWidgetProperty.getProperties().forEach(prop -> prop.setCode(codeGen.next()));

					existProperties.addAll(addWidgetProperty.getProperties());
				}
			} else if (change.getOperator().equals("addEvent")) {
				if (widget == null) {
					// 如果尚未创建 widget，则给出错误信息
					logger.error("无法执行 addEvent 操作，因为尚未创建 Widget");
					anyOperatorsInvalid = true;
					break;
				}

				AddWidgetEvent addWidgetEvent = change.getData(AddWidgetEvent.class);
				List<WidgetEvent> existEvents = widget.getEvents();

				List<WidgetEvent> addWidgetEvents = addWidgetEvent.getEvents();
				for (WidgetEvent added : addWidgetEvents) {
					// 如果事件名已被占用
					if (existEvents.stream().anyMatch(event -> event.getName().equals(added.getName()))) {
						logger.error("事件名 {0} 已存在", added.getName());
						anyOperatorsInvalid = true;
					}
				}

				if (!anyOperatorsInvalid) {
					String seed = getMaxPropertyCode(widget);
					CodeGenerator codeGen = new CodeGenerator(seed);
					addWidgetEvent.getEvents().forEach(event -> event.setCode(codeGen.next()));
					widget.getEvents().addAll(addWidgetEvent.getEvents());
				}
			}
		}
		
		return !anyOperatorsInvalid;
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

		String propertyMaxSeed = properties.isEmpty() ? "0001" : properties.get(properties.size() - 1).getCode();
		String eventMaxSeed = events.isEmpty() ? "0001" : events.get(events.size() - 1).getCode();
		return propertyMaxSeed.compareTo(eventMaxSeed) >= 0 ? propertyMaxSeed : eventMaxSeed;
	}
	
	public Widget getResult() {
		return widget;
	}
	
}
