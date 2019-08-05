package com.blocklang.marketplace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;
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
	public void find_by_id_no_data() {
		assertThat(apiRepoVersionService.findById(1)).isEmpty();
	}
	
	@Test
	public void find_by_id_no_success() {
		ApiRepoVersion version = new ApiRepoVersion();
		version.setApiRepoId(1);
		version.setVersion("0.1.0");
		version.setCreateUserId(1);
		version.setCreateTime(LocalDateTime.now());
		ApiRepoVersion savedVersion = apiRepoVersionDao.save(version);
		
		assertThat(apiRepoVersionService.findById(savedVersion.getId())).isPresent();
	}
	
}
