package com.blocklang.marketplace.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.marketplace.apirepo.RefData;
import com.blocklang.marketplace.apirepo.RefReader;
import com.blocklang.marketplace.apirepo.apiobject.ApiObject;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.GitRepoPublishTask;

public interface PersistApiRepoService {

	/**
	 * 尝试保存 git 仓库中所有 tag 和 master 分支的 API 对象。
	 * 
	 * 注意，如果某一个 tag 保存失败，则需要回滚此 tag 中的所有数据；
	 * 但依然会尝试保存后续 tag 和 master 分支中的数据。
	 * 
	 * @param context
	 * @return 如果全部保存成功就返回 true，否则返回 false。
	 */
	default boolean save(ExecutionContext context) {
		MarketplaceStore store = context.getValue(ExecutionContext.MARKETPLACE_STORE, MarketplaceStore.class);
		GitRepoPublishTask publishTask = context.getValue(ExecutionContext.PUBLISH_TASK, GitRepoPublishTask.class);
		// gitUrl 要优先获取从 context 中获取 GIT_URL，取不到，再从 publishTask 中获取
		// 只所以这样，是因为会出现一个 context 跨先发布组件库，再发布 API 库，这是就需要先存组件库 Url，然后再存 Api 库的 Url。
		String gitUrl = context.getStringValue(ExecutionContext.GIT_URL);
		if(gitUrl == null) {
			gitUrl = publishTask.getGitUrl();
		}
		CliLogger logger = context.getLogger();
		
		RefReader<? extends ApiObject> reader = new RefReader<>(store, logger);
		
		List<String> tags = GitUtils.getTags(store.getRepoSourceDirectory())
				.stream()
				.map(ref -> ref.getName())
				.collect(Collectors.toList());
		
		boolean success = true;
		if (tags.isEmpty()) {
			logger.info("git 仓库中没有标注 tag");
		} else {
			for (String tag : tags) {
				String shortRefName = GitUtils.getVersionFromRefName(tag).orElse(null);
				// FIXME: 只支持稳定的版本号
				if(shortRefName == null) { // 如果不是有效的语义版本号，则忽略此分支
					logger.info("根据 {0} 无法获取到有效的语义版本号", tag);
					continue;
				}
				
				// 判断是否已发布过，如果已发布过，则不重复发布
				if(this.getPersistApiRefService().isPublished(gitUrl, 
						publishTask.getCreateUserId(), shortRefName)) {
					logger.info("{0} 已发布过，不重复发布", tag);
					continue;
				}
				
				reader.setup(gitUrl, shortRefName, tag, 
						publishTask.getCreateUserId());
				RefData<? extends ApiObject> refData = reader.read();
				if(refData.isInvalidData()) {
					logger.error("{0} 读取 API 数据失败", tag);
					continue;
				}
				
				try {
					ApiRepo apiRepo = saveApoRepo(refData);
					this.getPersistApiRefService().save(apiRepo.getId(), refData);
				} catch (DataAccessException e) {
					success = false;
					logger.error(e);
					logger.error("{0} 中的 API 存储失败", tag);
				}
			}
		}
		
		// 存储 master 分支，每次发布时都要全部更新 master 分支
		try {
			reader.setup(gitUrl, "master", "refs/heads/master", 
					publishTask.getCreateUserId());
			RefData<? extends ApiObject> refData = reader.read();
			if (refData.isInvalidData()) {
				logger.error("{0} 读取 API 数据失败", "refs/heads/master");
				success = false;
			} else {
				ApiRepo apiRepo = saveApoRepo(refData);
				this.getPersistApiRefService().save(apiRepo.getId(), refData);
			}
		} catch (DataAccessException e) {
			success = false;
			logger.error(e);
			logger.error("master 分支中的 API 存储失败");
		}
		
		return success;
		
	}
	
	PersistApiRefService getPersistApiRefService();
	
	<T extends ApiObject> ApiRepo saveApoRepo(RefData<T> refData);
	
}
