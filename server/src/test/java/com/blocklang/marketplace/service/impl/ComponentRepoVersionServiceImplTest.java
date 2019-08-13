package com.blocklang.marketplace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ComponentRepoVersion;
import com.blocklang.marketplace.service.ComponentRepoVersionService;

public class ComponentRepoVersionServiceImplTest extends AbstractServiceTest {
	
	@Autowired
	private ComponentRepoVersionService componentRepoVersionService;
	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	
	@Test
	public void find_by_id_no_data() {
		assertThat(componentRepoVersionService.findById(1)).isEmpty();
	}
	
	@Test
	public void find_by_id_success() {
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setComponentRepoId(1);
		version.setVersion("0.1.0");
		version.setApiRepoVersionId(1);
		version.setCreateUserId(1);
		version.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion savedVersion = componentRepoVersionDao.save(version);
		
		assertThat(componentRepoVersionService.findById(savedVersion.getId())).isPresent();
	}
	
	@Test
	public void find_by_component_repo_id_no_data() {
		assertThat(componentRepoVersionService.findByComponentRepoId(1)).isEmpty();
	}
	
	@Test
	public void find_by_component_repo_id_successno_data() {
		Integer componentRepoId = 1;
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setComponentRepoId(componentRepoId);
		version.setVersion("0.1.0");
		version.setApiRepoVersionId(1);
		version.setCreateUserId(1);
		version.setCreateTime(LocalDateTime.now());
		componentRepoVersionDao.save(version);
		
		assertThat(componentRepoVersionService.findByComponentRepoId(componentRepoId)).hasSize(1);
	}
}
