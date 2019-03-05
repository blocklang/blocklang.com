package com.blocklang.develop.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialOperateFields;
import com.blocklang.develop.constant.DeployState;
import com.blocklang.develop.constant.converter.DeployStateConverter;

/**
 * 项目部署配置信息
 * 
 * 注意：此部署类放在 develop 包下，而不是在 deploy 包或 release 包下，
 * 因为此类中存的信息，可在开发阶段生成。
 * 
 * @author Zhengwei Jin
 *
 */
@Entity
@Table(name = "project_deploy", 
uniqueConstraints = @UniqueConstraint(columnNames = { "project_id", "user_id", "registration_token" }))
public class ProjectDeploy extends PartialOperateFields{

	private static final long serialVersionUID = 5478013789004188656L;

	@Column(name = "project_id", nullable = false)
	private Integer projectId;
	
	@Column(name = "user_id", nullable = false)
	private Integer userId;
	
	@Column(name = "registration_token", unique = true, length = 22)
	private String registrationToken;
	
	@Convert(converter = DeployStateConverter.class)
	@Column(name = "deploy_state", nullable = false, length = 2)
	private DeployState deployState;

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getRegistrationToken() {
		return registrationToken;
	}

	public void setRegistrationToken(String registrationToken) {
		this.registrationToken = registrationToken;
	}

	public DeployState getDeployState() {
		return deployState;
	}

	public void setDeployState(DeployState deployState) {
		this.deployState = deployState;
	}
	
}
