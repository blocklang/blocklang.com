package com.blocklang.marketplace.service;

import com.blocklang.marketplace.model.ComponentRepoPublishTask;

/**
 * 发布组件库服务接口
 * 
 * @author Zhengwei Jin
 *
 */
public interface PublishService {

	void publish(ComponentRepoPublishTask publishTask, String dataRootPath);
	
	void asyncPublish(ComponentRepoPublishTask publishTask, String dataRootPath);
}
