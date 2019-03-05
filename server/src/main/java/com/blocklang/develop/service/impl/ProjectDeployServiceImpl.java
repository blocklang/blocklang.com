package com.blocklang.develop.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.core.util.IdGenerator;
import com.blocklang.develop.constant.DeployState;
import com.blocklang.develop.dao.ProjectDeployDao;
import com.blocklang.develop.model.ProjectDeploy;
import com.blocklang.develop.service.ProjectDeployService;

@Service
public class ProjectDeployServiceImpl implements ProjectDeployService {

	@Autowired
	private ProjectDeployDao projectDeployDao;
	
	@Override
	public Optional<ProjectDeploy> findOrCreate(Integer projectId, Integer userId) {
		return projectDeployDao.findByProjectIdAndUserId(projectId, userId).or(() ->{
			ProjectDeploy deploy = new ProjectDeploy();
			deploy.setUserId(userId);
			deploy.setProjectId(projectId);
			deploy.setRegistrationToken(IdGenerator.shortUuid());
			deploy.setDeployState(DeployState.UNDEPLOY);
			deploy.setCreateTime(LocalDateTime.now());
			deploy.setCreateUserId(userId);
			return Optional.of(projectDeployDao.save(deploy));
		});
	}

	@Override
	public Optional<ProjectDeploy> findByRegistrationToken(String registrationToken) {
		return projectDeployDao.findByRegistrationToken(registrationToken);
	}

}
