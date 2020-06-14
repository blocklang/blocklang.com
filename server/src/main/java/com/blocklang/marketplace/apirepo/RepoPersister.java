package com.blocklang.marketplace.apirepo;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.marketplace.service.PersistApiRefService;

public class RepoPersister<T extends ApiObject> {

	private MarketplaceStore store;
	private ComponentRepoPublishTask publishTask;
	private CliLogger logger;
	private List<String> tags;
	private PersistApiRefService<T> persistApiRefService;
	private ApiRefPersisterFactory<T> factory;
	private RefReader<T> reader;
	
	public RepoPersister(ExecutionContext context, ApiRefPersisterFactory<T> factory) {
		this.store = context.getValue(ExecutionContext.MARKETPLACE_STORE, MarketplaceStore.class);
		this.publishTask = context.getValue(ExecutionContext.PUBLISH_TASK, ComponentRepoPublishTask.class);
		
		logger = context.getLogger();
		this.factory = factory;
	}
	
	/**
	 * 尝试保存 git 仓库中所有 tag 和 master 分支的 API 对象。
	 * 
	 * 注意，如果某一个 tag 保存失败，则需要回滚此 tag 中的所有数据；
	 * 但依然会尝试保存后续 tag 或 master 分支中的数据。
	 * 
	 * @return 如果全部保存成功就返回 true，否则返回 false。
	 */
	public boolean run() {
		setup();
		return tryPersistAllTagsAndMaster();
	}

	private void setup() {
		tags = GitUtils.getTags(store.getRepoSourceDirectory())
				.stream()
				.map(ref -> ref.getName())
				.collect(Collectors.toList());
		
		this.reader = factory.createRefReader(store, logger);
		this.persistApiRefService = factory.createPersistApiRefService();
	}
	
	private boolean tryPersistAllTagsAndMaster() {
		boolean success = true;
		
		if(tags.isEmpty()) {
			logger.info("git 仓库中没有标注 tag");
		} else {
			for(String tag : tags) {
				String shortRefName = GitUtils.getVersionFromRefName(tag).get();
				// 判断是否已发布过，如果已发布过，则不再发布
				if(persistApiRefService.isPublished(publishTask.getGitUrl(), publishTask.getCreateUserId(), shortRefName)) {
					logger.info("{0} 已发布过，不再发布", tag);
					continue;
				}
				
				reader.setup(publishTask.getGitUrl(), shortRefName, tag, publishTask.getCreateUserId());
				RefData<T> refData = reader.read();
				if(refData.isInvalidData()) {
					logger.error("{0} 读取 API 数据失败", tag);
					continue;
				}
				
				try {
					this.persistApiRefService.save(refData);
				}catch (DataAccessException e) {
					success = false;
					logger.error("{0} 中的 API 存储失败", tag);
				}
			}
		}
		
		// 存储 master 分支，每次发布时都要全部更新 master 分支
		try {
			reader.setup(publishTask.getGitUrl(), "master", "refs/heads/master", publishTask.getCreateUserId());
			RefData<T> refData = reader.read();
			
			if(refData.isInvalidData()) {
				logger.error("{0} 读取 API 数据失败", "refs/heads/master");
			} else {
				this.persistApiRefService.save(refData);
			}
		}catch (DataAccessException e) {
			success = false;
			logger.error("master 分支中的 API 存储失败");
		}
		
		return success;
	}

	
}
