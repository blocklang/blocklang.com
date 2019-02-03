package com.blocklang.release.service.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.service.AbstractServiceTest;
import com.blocklang.release.dao.ProjectTagDao;
import com.blocklang.release.model.ProjectTag;
import com.blocklang.release.service.ProjectTagService;

public class ProjectTagServiceImplTest extends AbstractServiceTest {

	@Autowired
	private ProjectTagService projectTagService;
	@Autowired
	private ProjectTagDao projectTagDao;
	
	@Test
	public void find_no_data() {
		Optional<ProjectTag> projectTagOption = projectTagService.find(1, "0.1.0");
		assertThat(projectTagOption.isEmpty(), is(true));
	}
	
	@Test
	public void find_success() {
		ProjectTag projectTag = new ProjectTag();
		
		projectTag.setProjectId(1);
		projectTag.setVersion("0.1.0");
		projectTag.setGitTagId("i-am-a-git-tag");
		projectTag.setCreateUserId(2);
		projectTag.setCreateTime(LocalDateTime.now());
		
		projectTagDao.save(projectTag);
		Optional<ProjectTag> projectTagOption = projectTagService.find(1, "0.1.0");
		assertThat(projectTagOption.isPresent(), is(true));
	}
	
	@Test
	public void find_latest_no_data() {
		Optional<ProjectTag> projectTagOption = projectTagService.findLatestTag(1);
		assertThat(projectTagOption.isEmpty(), is(true));
	}
	
	@Test
	public void find_latest_success_one_project_two_version() {
		ProjectTag projectTag = new ProjectTag();
		
		projectTag.setProjectId(1);
		projectTag.setVersion("0.1.0");
		projectTag.setGitTagId("i-am-a-git-tag-1");
		projectTag.setCreateUserId(2);
		projectTag.setCreateTime(LocalDateTime.now());
		
		projectTagDao.save(projectTag);
		
		projectTag = new ProjectTag();
		projectTag.setProjectId(1);
		projectTag.setVersion("0.1.1");
		projectTag.setGitTagId("i-am-a-git-tag-2");
		projectTag.setCreateUserId(2);
		projectTag.setCreateTime(LocalDateTime.now());
		
		projectTagDao.save(projectTag);
		
		Optional<ProjectTag> projectTagOption = projectTagService.findLatestTag(1);
		assertThat(projectTagOption.get().getVersion(), equalTo("0.1.1"));
	}
	
	@Test
	public void find_latest_success_two_project_one_version() {
		ProjectTag projectTag = new ProjectTag();
		
		projectTag.setProjectId(1);
		projectTag.setVersion("0.1.0");
		projectTag.setGitTagId("i-am-a-git-tag-1");
		projectTag.setCreateUserId(2);
		projectTag.setCreateTime(LocalDateTime.now());
		
		projectTagDao.save(projectTag);
		
		projectTag = new ProjectTag();
		projectTag.setProjectId(2);
		projectTag.setVersion("0.1.1");
		projectTag.setGitTagId("i-am-a-git-tag-2");
		projectTag.setCreateUserId(2);
		projectTag.setCreateTime(LocalDateTime.now());
		
		projectTagDao.save(projectTag);
		
		Optional<ProjectTag> projectTagOption = projectTagService.findLatestTag(1);
		assertThat(projectTagOption.get().getVersion(), equalTo("0.1.0"));
	}
}
