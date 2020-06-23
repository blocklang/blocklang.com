package com.blocklang.marketplace.service.impl;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.marketplace.model.GitRepoPublishTask;
import com.blocklang.marketplace.service.GitRepoPublishTaskService;
import com.blocklang.marketplace.service.RepoPublishService;
import com.blocklang.release.constant.ReleaseResult;

public class PublishServiceImplTest extends AbstractServiceTest{

	@Autowired
	private GitRepoPublishTaskService gitRepoPublishTaskService;
	@Autowired
	private RepoPublishService publishService;
	
	// 如果要运行此测试用例，则需要 mock propertyService 并添加断言
	@Disabled
	@Test
	public void publish() {
		GitRepoPublishTask task = new GitRepoPublishTask();
		task.setGitUrl("https://github.com/blocklang/widgets-bootstrap.git");
		task.setCreateUserId(1);
		task.setPublishResult(ReleaseResult.STARTED);
		task.setCreateTime(LocalDateTime.now());
		task.setStartTime(LocalDateTime.now());
		
		GitRepoPublishTask savedTask = gitRepoPublishTaskService.save(task);
		publishService.publish(savedTask);
	}
}
