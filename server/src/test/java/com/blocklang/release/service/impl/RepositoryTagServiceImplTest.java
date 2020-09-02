package com.blocklang.release.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.release.dao.RepositoryTagDao;
import com.blocklang.release.model.RepositoryTag;
import com.blocklang.release.service.RepositoryTagService;

public class RepositoryTagServiceImplTest extends AbstractServiceTest {

	@Autowired
	private RepositoryTagService repositoryTagService;
	@Autowired
	private RepositoryTagDao repositoryTagDao;
	
	@Test
	public void find_no_data() {
		Optional<RepositoryTag> projectTagOption = repositoryTagService.find(1, "0.1.0");
		assertThat(projectTagOption).isEmpty();
	}
	
	@Test
	public void find_success() {
		RepositoryTag projectTag = new RepositoryTag();
		
		projectTag.setRepositoryId(1);
		projectTag.setVersion("0.1.0");
		projectTag.setGitTagId("i-am-a-git-tag");
		projectTag.setCreateUserId(2);
		projectTag.setCreateTime(LocalDateTime.now());
		
		repositoryTagDao.save(projectTag);
		Optional<RepositoryTag> projectTagOption = repositoryTagService.find(1, "0.1.0");
		assertThat(projectTagOption).isPresent();
	}
	
	@Test
	public void find_latest_no_data() {
		Optional<RepositoryTag> projectTagOption = repositoryTagService.findLatestTag(1);
		assertThat(projectTagOption).isEmpty();
	}
	
	@Test
	public void find_latest_success_one_project_two_version() {
		RepositoryTag projectTag = new RepositoryTag();
		
		projectTag.setRepositoryId(1);
		projectTag.setVersion("0.1.0");
		projectTag.setGitTagId("i-am-a-git-tag-1");
		projectTag.setCreateUserId(2);
		projectTag.setCreateTime(LocalDateTime.now());
		
		repositoryTagDao.save(projectTag);
		
		projectTag = new RepositoryTag();
		projectTag.setRepositoryId(1);
		projectTag.setVersion("0.1.1");
		projectTag.setGitTagId("i-am-a-git-tag-2");
		projectTag.setCreateUserId(2);
		projectTag.setCreateTime(LocalDateTime.now());
		
		repositoryTagDao.save(projectTag);
		
		Optional<RepositoryTag> projectTagOption = repositoryTagService.findLatestTag(1);
		assertThat(projectTagOption.get().getVersion()).isEqualTo("0.1.1");
	}
	
	@Test
	public void find_latest_success_two_project_one_version() {
		RepositoryTag projectTag = new RepositoryTag();
		
		projectTag.setRepositoryId(1);
		projectTag.setVersion("0.1.0");
		projectTag.setGitTagId("i-am-a-git-tag-1");
		projectTag.setCreateUserId(2);
		projectTag.setCreateTime(LocalDateTime.now());
		
		repositoryTagDao.save(projectTag);
		
		projectTag = new RepositoryTag();
		projectTag.setRepositoryId(2);
		projectTag.setVersion("0.1.1");
		projectTag.setGitTagId("i-am-a-git-tag-2");
		projectTag.setCreateUserId(2);
		projectTag.setCreateTime(LocalDateTime.now());
		
		repositoryTagDao.save(projectTag);
		
		Optional<RepositoryTag> projectTagOption = repositoryTagService.findLatestTag(1);
		assertThat(projectTagOption.get().getVersion()).isEqualTo("0.1.0");
	}
}
