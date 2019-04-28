package com.blocklang.develop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.develop.constant.AccessLevel;
import com.blocklang.develop.dao.ProjectAuthorizationDao;
import com.blocklang.develop.model.ProjectAuthorization;
import com.blocklang.develop.service.ProjectAuthorizationService;

public class ProjectAuthorizationServiceImplTest extends AbstractServiceTest{

	@Autowired
	private ProjectAuthorizationService projectAuthorizationService;
	@Autowired
	private ProjectAuthorizationDao projectAuthorizationDao;
	
	@Test
	public void find_no_data() {
		assertThat(projectAuthorizationService.findAllByUserIdAndProjectId(1, 1)).isEmpty();
	}
	
	@Test
	public void find_success() {
		ProjectAuthorization auth = new ProjectAuthorization();
		auth.setUserId(1);
		auth.setProjectId(1);
		auth.setAccessLevel(AccessLevel.WRITE);
		auth.setCreateUserId(1);
		auth.setCreateTime(LocalDateTime.now());
		projectAuthorizationDao.save(auth);
		
		assertThat(projectAuthorizationService.findAllByUserIdAndProjectId(1, 1)).hasSize(1);
	}
}
