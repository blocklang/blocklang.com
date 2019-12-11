package com.blocklang.marketplace.service.impl;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.service.ComponentRepoPublishTaskService;
import com.blocklang.marketplace.service.PublishService;
import com.blocklang.release.constant.ReleaseResult;

public class PublishServiceImplTest extends AbstractServiceTest{

	@Autowired
	private ComponentRepoPublishTaskService componentRepoPublishTaskService;
	@Autowired
	private PublishService publishService;
	
	@Test
	public void publish() {
		ComponentRepoPublishTask task = new ComponentRepoPublishTask();
		task.setGitUrl("https://github.com/blocklang/widgets-bootstrap.git");
		task.setCreateUserId(1);
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setStartTime(LocalDateTime.now());
		
		ComponentRepoPublishTask savedTask = componentRepoPublishTaskService.save(task);
		publishService.publish(savedTask);
	}
}
