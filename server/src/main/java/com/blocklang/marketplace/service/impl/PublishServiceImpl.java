package com.blocklang.marketplace.service.impl;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.service.PublishService;

@Service
public class PublishServiceImpl implements PublishService {

	@Async
	@Override
	public void asyncPublish(ComponentRepoPublishTask publishTask) {
		this.publish(publishTask);

	}

	@Override
	public void publish(ComponentRepoPublishTask publishTask) {
		// 从源代码托管网站下载组件的源代码
		// 查找 git 仓库中的 tag
		// 找到最新的 tag
		// 从最新的 tag 中查找 package.json 文件
		// 校验 package.json 中的必填项等
		// 扫面项目中所有的 ui 部件，确认文件是否齐全
		// 扫描 changelog 文件夹，检测配置的是否准确
		// 当检测通过之后，才开始往数据库中存储 ui 部件的元数据
		
	}

}
