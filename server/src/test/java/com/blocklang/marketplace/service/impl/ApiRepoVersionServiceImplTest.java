package com.blocklang.marketplace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.service.ApiRepoVersionService;

public class ApiRepoVersionServiceImplTest extends AbstractServiceTest {

	@Autowired
	private ApiRepoVersionService apiRepoVersionService;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	
	@Test
	public void findById_no_data() {
		assertThat(apiRepoVersionService.findById(1)).isEmpty();
	}
	
	@Test
	public void findById_success() {
		ApiRepoVersion version = new ApiRepoVersion();
		version.setApiRepoId(1);
		version.setName("a api repo");
		version.setVersion("0.1.0");
		version.setGitTagName("v0.1.0");
		version.setCreateUserId(1);
		version.setCreateTime(LocalDateTime.now());
		ApiRepoVersion savedVersion = apiRepoVersionDao.save(version);
		
		assertThat(apiRepoVersionService.findById(savedVersion.getId())).isPresent();
	}
	
	@Test
	public void findLatestVersion_empty_versions() {
		assertThat(apiRepoVersionService.findLatestStableVersion(Integer.MAX_VALUE)).isEmpty();
	}
	
	@Test
	public void findLatestVersion_only_has_master() {
		Integer apiRepoId = Integer.MAX_VALUE;
		
		ApiRepoVersion master = new ApiRepoVersion();
		master.setApiRepoId(apiRepoId);
		master.setName("a api repo");
		master.setVersion("master");
		master.setGitTagName("refs/heads/master");
		master.setCreateUserId(1);
		master.setCreateTime(LocalDateTime.now());
		apiRepoVersionDao.save(master);
		
		assertThat(apiRepoVersionService.findLatestStableVersion(apiRepoId)).isEmpty();
	}
	
	@Test
	public void findLatestVersion_two_versions() {
		Integer apiRepoId = Integer.MAX_VALUE;
		
		ApiRepoVersion version1 = new ApiRepoVersion();
		version1.setApiRepoId(apiRepoId);
		version1.setName("a api repo");
		version1.setVersion("0.1.0");
		version1.setGitTagName("v0.1.0");
		version1.setCreateUserId(1);
		version1.setCreateTime(LocalDateTime.now());
		
		apiRepoVersionDao.save(version1);
		
		ApiRepoVersion version2 = new ApiRepoVersion();
		version2.setApiRepoId(apiRepoId);
		version2.setName("a api repo");
		version2.setVersion("0.2.0");
		version2.setGitTagName("v0.2.0");
		version2.setCreateUserId(1);
		version2.setCreateTime(LocalDateTime.now());
		apiRepoVersionDao.save(version2);
		
		ApiRepoVersion master = new ApiRepoVersion();
		master.setApiRepoId(apiRepoId);
		master.setName("a api repo");
		master.setVersion("master");
		master.setGitTagName("refs/heads/master");
		master.setCreateUserId(1);
		master.setCreateTime(LocalDateTime.now());
		apiRepoVersionDao.save(master);
		
		assertThat(apiRepoVersionService.findLatestStableVersion(apiRepoId).get().getVersion()).isEqualTo("0.2.0");
	}
}
