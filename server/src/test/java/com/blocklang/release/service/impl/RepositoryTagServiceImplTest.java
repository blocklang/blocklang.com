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
		Optional<RepositoryTag> repositoryTagOption = repositoryTagService.find(1, "0.1.0");
		assertThat(repositoryTagOption).isEmpty();
	}
	
	@Test
	public void find_success() {
		RepositoryTag repositoryTag = new RepositoryTag();
		
		repositoryTag.setRepositoryId(1);
		repositoryTag.setVersion("0.1.0");
		repositoryTag.setGitTagId("i-am-a-git-tag");
		repositoryTag.setCreateUserId(2);
		repositoryTag.setCreateTime(LocalDateTime.now());
		
		repositoryTagDao.save(repositoryTag);
		Optional<RepositoryTag> repositoryTagOption = repositoryTagService.find(1, "0.1.0");
		assertThat(repositoryTagOption).isPresent();
	}
	
	@Test
	public void find_latest_no_data() {
		Optional<RepositoryTag> repositoryTagOption = repositoryTagService.findLatestTag(1);
		assertThat(repositoryTagOption).isEmpty();
	}
	
	@Test
	public void find_latest_success_one_project_two_version() {
		RepositoryTag repositoryTag = new RepositoryTag();
		
		repositoryTag.setRepositoryId(1);
		repositoryTag.setVersion("0.1.0");
		repositoryTag.setGitTagId("i-am-a-git-tag-1");
		repositoryTag.setCreateUserId(2);
		repositoryTag.setCreateTime(LocalDateTime.now());
		
		repositoryTagDao.save(repositoryTag);
		
		repositoryTag = new RepositoryTag();
		repositoryTag.setRepositoryId(1);
		repositoryTag.setVersion("0.1.1");
		repositoryTag.setGitTagId("i-am-a-git-tag-2");
		repositoryTag.setCreateUserId(2);
		repositoryTag.setCreateTime(LocalDateTime.now());
		
		repositoryTagDao.save(repositoryTag);
		
		Optional<RepositoryTag> repositoryTagOption = repositoryTagService.findLatestTag(1);
		assertThat(repositoryTagOption.get().getVersion()).isEqualTo("0.1.1");
	}
	
	@Test
	public void find_latest_success_two_project_one_version() {
		RepositoryTag repositoryTag = new RepositoryTag();
		
		repositoryTag.setRepositoryId(1);
		repositoryTag.setVersion("0.1.0");
		repositoryTag.setGitTagId("i-am-a-git-tag-1");
		repositoryTag.setCreateUserId(2);
		repositoryTag.setCreateTime(LocalDateTime.now());
		
		repositoryTagDao.save(repositoryTag);
		
		repositoryTag = new RepositoryTag();
		repositoryTag.setRepositoryId(2);
		repositoryTag.setVersion("0.1.1");
		repositoryTag.setGitTagId("i-am-a-git-tag-2");
		repositoryTag.setCreateUserId(2);
		repositoryTag.setCreateTime(LocalDateTime.now());
		
		repositoryTagDao.save(repositoryTag);
		
		Optional<RepositoryTag> repositoryTagOption = repositoryTagService.findLatestTag(1);
		assertThat(repositoryTagOption.get().getVersion()).isEqualTo("0.1.0");
	}
}
