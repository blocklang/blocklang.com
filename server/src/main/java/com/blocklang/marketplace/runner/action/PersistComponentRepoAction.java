package com.blocklang.marketplace.runner.action;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.AbstractAction;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.core.util.JsonUtil;
import com.blocklang.core.util.SpringUtils;
import com.blocklang.marketplace.componentrepo.RefData;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.data.RepoConfigJson;
import com.blocklang.marketplace.model.GitRepoPublishTask;
import com.blocklang.marketplace.service.PersistComponentRepoService;
import com.fasterxml.jackson.core.JsonProcessingException;

public class PersistComponentRepoAction extends AbstractAction{

	private String defaultBranch;
	private GitRepoPublishTask publishTask;
	private MarketplaceStore store;
	
	public PersistComponentRepoAction(ExecutionContext context) {
		super(context);
		this.publishTask = context.getValue(ExecutionContext.PUBLISH_TASK, GitRepoPublishTask.class);
		this.store = context.getValue(ExecutionContext.MARKETPLACE_STORE, MarketplaceStore.class);
		try {
			this.defaultBranch = GitUtils.getDefaultBranch(store.getRepoSourceDirectory());
		} catch (IOException e) {
			
		}
	}

	@Override
	public boolean run() {
		// 1. 获取 git tags
		List<RefData> refDatas = GitUtils.getTags(store.getRepoSourceDirectory())
				.stream()
				.map(ref -> ref.getName())
				// 2. 从 每个 tag 和 master 中读取 blocklang.json 文件内容
				.map(fullRefName -> {
					RefData data = new RefData();
					data.setFullRefName(fullRefName);
					data.setShortRefName(GitUtils.getVersionFromRefName(fullRefName).get());
					data.setCreateUserId(publishTask.getCreateUserId());
					data.setGitUrl(publishTask.getGitUrl());
					
					RepoConfigJson repoConfig;
					try {
						String configContent = GitUtils.getBlob(store.getRepoSourceDirectory(), fullRefName, "blocklang.json")
								.get().getContent();
						repoConfig = JsonUtil.fromJsonObject(configContent, RepoConfigJson.class);
						data.setRepoConfig(repoConfig);
					} catch (JsonProcessingException e) {
						data.readFailed();
					}
					
					return data;
				})
				.collect(Collectors.toList());
		
		if(this.defaultBranch == null) {
			logger.error("在 git 仓库中没有找到 master/main 分支");
			return false;
		}
		// 读取 master 分支
		RefData data = new RefData();
		data.setFullRefName("refs/heads/" + this.defaultBranch);
		data.setShortRefName(this.defaultBranch);
		data.setCreateUserId(publishTask.getCreateUserId());
		data.setGitUrl(publishTask.getGitUrl());
		
		RepoConfigJson repoConfig;
		try {
			String configContent = GitUtils.getBlob(store.getRepoSourceDirectory(), "refs/heads/" + this.defaultBranch, "blocklang.json")
					.get().getContent();
			repoConfig = JsonUtil.fromJsonObject(configContent, RepoConfigJson.class);
			data.setRepoConfig(repoConfig);
		} catch (JsonProcessingException e) {
			data.readFailed();
		}
		refDatas.add(data);
		
		// 3. 逐个存储 blocklang.json 内容，tag 存过之后不再更新，master 分支每次都要更新
		boolean success = true;
		try {
			getPersistComponentRepoService().save(refDatas);
		}catch(DataAccessException e) {
			success = false;
		}
		return success;
	}
	
	private PersistComponentRepoService getPersistComponentRepoService() {
		return SpringUtils.getBean(PersistComponentRepoService.class);
	}

}
