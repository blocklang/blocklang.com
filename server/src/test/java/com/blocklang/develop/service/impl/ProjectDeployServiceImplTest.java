package com.blocklang.develop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.develop.constant.DeployState;
import com.blocklang.develop.dao.ProjectDeployDao;
import com.blocklang.develop.model.ProjectDeploy;
import com.blocklang.develop.service.ProjectDeployService;

public class ProjectDeployServiceImplTest extends AbstractServiceTest {

	@Autowired
	private ProjectDeployService projectDeployService;
	@Autowired
	private ProjectDeployDao projectDeployDao;
	
	@Test
	public void find_or_create_is_not_exist_then_create_it() {
		assertThat(projectDeployService.findOrCreate(1, 1)).isPresent();
	}
	
	@Test
	public void find_or_create_success() {
		ProjectDeploy deploy = new ProjectDeploy();
		deploy.setUserId(1);
		deploy.setProjectId(1);
		deploy.setRegistrationToken("token");
		deploy.setDeployState(DeployState.UNDEPLOY);
		deploy.setCreateTime(LocalDateTime.now());
		deploy.setCreateUserId(1);
		
		projectDeployDao.save(deploy);
		
		assertThat(projectDeployService.findOrCreate(1, 1)).isPresent();
	}
	

	@Test
	public void find_by_registration_token_no_data() {
		assertThat(projectDeployService.findByRegistrationToken("not-exist-registration-token")).isEmpty();
		
		ProjectDeploy deploy = new ProjectDeploy();
		deploy.setUserId(1);
		deploy.setProjectId(1);
		deploy.setRegistrationToken("registration-token");
		deploy.setDeployState(DeployState.UNDEPLOY);
		deploy.setCreateTime(LocalDateTime.now());
		deploy.setCreateUserId(1);
		
		projectDeployDao.save(deploy);
		
		assertThat(projectDeployService.findByRegistrationToken("not-exist-registration-token")).isEmpty();
	}
	
	@Test
	public void find_by_registration_token_success() {
		ProjectDeploy deploy = new ProjectDeploy();
		deploy.setRegistrationToken("registration-token");
		deploy.setUserId(1);
		deploy.setProjectId(2);
		deploy.setCreateTime(LocalDateTime.now());
		deploy.setCreateUserId(1);
		deploy.setDeployState(DeployState.UNDEPLOY);
		
		projectDeployDao.save(deploy);
		
		Optional<ProjectDeploy> deployOption = projectDeployService.findByRegistrationToken("registration-token");
		assertThat(deployOption).isPresent();
	}

}
