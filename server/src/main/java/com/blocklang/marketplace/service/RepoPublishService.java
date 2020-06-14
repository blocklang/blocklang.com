package com.blocklang.marketplace.service;

import com.blocklang.marketplace.model.ComponentRepoPublishTask;

/**
 * 发布组件库的服务接口
 * 
 * @author Zhengwei Jin
 *
 */
public interface RepoPublishService {

	void publish(ComponentRepoPublishTask publishTask);
	
}
