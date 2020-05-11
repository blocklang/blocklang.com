package com.blocklang.marketplace.task;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.Assert;

import com.blocklang.core.runner.CliContext;
import com.blocklang.core.runner.CliLogger;
import com.blocklang.core.runner.TaskLogger;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;

/**
 * 在这里统一存储和查询发布过程中的信息
 * 
 * @author Zhengwei Jin
 *
 */
public class MarketplacePublishContext implements CliContext<MarketplacePublishData>{

	private CliLogger logger;
	private MarketplacePublishData data;
	
	public MarketplacePublishContext(String dataRootPath, ComponentRepoPublishTask publishTask) {
		data = new MarketplacePublishData(dataRootPath, publishTask);
	}
	
	/**
	 * 创建日志记录对象。开启了发送 stomp 日志功能。
	 * 
	 * <p>在执行任务过程中共用同一个日志对象。</p>
	 * 
	 * @param messagingTemplate    发送 stomp 消息的模板对象
	 * @param desinationPrefix     发送地址前缀，以 / 开头，完整地址为 <code>desinationPrefix/{taskId}</code>
	 * @return 日志记录对象
	 */
	@Override
	public CliLogger newLogger(SimpMessagingTemplate messagingTemplate, String desinationPrefix) {
		Assert.isNull(this.logger, "日志记录对象已创建，不要重复创建");

		CliLogger taskLogger = new TaskLogger(this.data.getRepoPublishLogFile());
		// 设置 websocket 消息的参数，启动发送 stomp 消息功能
		taskLogger.enableSendStompMessage(this.data.getPublishTask().getId(), messagingTemplate, desinationPrefix);

		this.logger = taskLogger;
		return logger;
	}

	/**
	 * 获取日志记录对象，在调用此方法前，需要先调用 {@link #newLogger(SimpMessagingTemplate, String)} 创建日志记录对象。
	 * 
	 * @return 日志记录对象
	 */
	@Override
	public CliLogger getLogger() {
		Assert.notNull(this.logger, "日志记录对象未创建，请先调用 TaskLogger#newLogger");
		return this.logger;
	}

	@Override
	public MarketplacePublishData getData() {
		return this.data;
	}

}
