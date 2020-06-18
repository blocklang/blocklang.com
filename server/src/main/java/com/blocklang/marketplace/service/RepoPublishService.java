package com.blocklang.marketplace.service;

import com.blocklang.marketplace.model.GitRepoPublishTask;

/**
 * 发布组件库的服务接口
 * 
 * @author Zhengwei Jin
 *
 */
public interface RepoPublishService {

	void publish(GitRepoPublishTask publishTask);
	
	void asyncPublish(GitRepoPublishTask publishTask);
}
