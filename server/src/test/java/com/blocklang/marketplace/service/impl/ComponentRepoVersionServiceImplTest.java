package com.blocklang.marketplace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.develop.constant.AppType;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ComponentRepoVersion;
import com.blocklang.marketplace.service.ComponentRepoVersionService;

public class ComponentRepoVersionServiceImplTest extends AbstractServiceTest {
	
	@Autowired
	private ComponentRepoVersionService componentRepoVersionService;
	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	
	@Test
	public void findById_no_data() {
		assertThat(componentRepoVersionService.findById(1)).isEmpty();
	}
	
	@Test
	public void findById_success() {
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setApiRepoVersionId(1);
		version.setComponentRepoId(1);
		version.setName("a component repo");
		version.setVersion("0.1.0");
		version.setGitTagName("v0.1.0");
		version.setAppType(AppType.WEB);
		version.setBuild("dojo");
		version.setCreateUserId(1);
		version.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion savedVersion = componentRepoVersionDao.save(version);
		
		assertThat(componentRepoVersionService.findById(savedVersion.getId())).isPresent();
	}
	
	@Test
	public void findAllByComponentRepoId_no_data() {
		Integer componentRepoId = 1;
		assertThat(componentRepoVersionService.findAllByComponentRepoId(componentRepoId)).isEmpty();
	}
	
	@Test
	public void findAllByComponentRepoId_success() {
		Integer componentRepoId = 1;
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setApiRepoVersionId(1);
		version.setComponentRepoId(componentRepoId);
		version.setName("component repo");
		version.setVersion("0.1.0");
		version.setGitTagName("v0.1.0");
		version.setAppType(AppType.WEB);
		version.setBuild("dojo");
		version.setCreateUserId(1);
		version.setCreateTime(LocalDateTime.now());
		componentRepoVersionDao.save(version);
		
		assertThat(componentRepoVersionService.findAllByComponentRepoId(componentRepoId)).hasSize(1);
	}
	
	@Test
	public void findLatestVersion_empty_versions() {
		assertThat(componentRepoVersionService.findLatestVersion(Integer.MAX_VALUE)).isEmpty();
	}
	
	@Test
	public void findLatestVersion_two_versions() {
		Integer componentRepoId = Integer.MAX_VALUE;
		
		ComponentRepoVersion version1 = new ComponentRepoVersion();
		version1.setApiRepoVersionId(1);
		version1.setComponentRepoId(componentRepoId);
		version1.setName("component repo");
		version1.setVersion("0.1.0");
		version1.setGitTagName("v0.1.0");
		version1.setAppType(AppType.WEB);
		version1.setBuild("dojo");
		version1.setCreateUserId(1);
		version1.setCreateTime(LocalDateTime.now());
		componentRepoVersionDao.save(version1);
		
		ComponentRepoVersion version2 = new ComponentRepoVersion();
		version2.setApiRepoVersionId(1);
		version2.setComponentRepoId(componentRepoId);
		version2.setName("component repo");
		version2.setVersion("0.2.0");
		version2.setGitTagName("v0.2.0");
		version2.setAppType(AppType.WEB);
		version2.setBuild("dojo");
		version2.setCreateUserId(1);
		version2.setCreateTime(LocalDateTime.now());
		componentRepoVersionDao.save(version2);
		
		ComponentRepoVersion master = new ComponentRepoVersion();
		master.setApiRepoVersionId(1);
		master.setComponentRepoId(componentRepoId);
		master.setName("component repo");
		master.setVersion("master");
		master.setGitTagName("refs/heads/master");
		master.setAppType(AppType.WEB);
		master.setBuild("dojo");
		master.setCreateUserId(1);
		master.setCreateTime(LocalDateTime.now());
		componentRepoVersionDao.save(master);
		
		assertThat(componentRepoVersionService.findLatestVersion(componentRepoId).get().getVersion()).isEqualTo("0.2.0");
	}
}
