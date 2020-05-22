package com.blocklang.core.runner.action;

import java.nio.file.Path;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.AbstractAction;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.marketplace.data.MarketplaceStore;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;

/**
 * 从远程 git 仓库 clone 或 pull 源码。
 * 
 * 需要从上下文中读取 repository 变量，该变量表示 git 仓库的远程地址。
 * 
 * <pre>
 * inputs: 无
 * outputs: 无
 * </pre>
 * 
 * 注意：
 * <ul>
 * <li>gitUrl                  - string(required), git 仓库地址，取自 publishTask.gitUrl</li>
 * <li>localSourceDirectory    - Path(required), 仓库在主机上的存储路径，取自 marketplaceStore.getRepoSourceDirectory()</li>
 * </ul>
 * 
 * @author Zhengwei Jin
 *
 */
public class CheckoutAction extends AbstractAction{

	private String gitUrl;
	private Path localSourceDirectory;
	
	public CheckoutAction(ExecutionContext context) {
		super(context);
		
		var task = context.getValue(ExecutionContext.PUBLISH_TASK, ComponentRepoPublishTask.class);
		var store = context.getValue(ExecutionContext.MARKETPLACE_STORE, MarketplaceStore.class);
		
		this.gitUrl = task.getGitUrl();
		this.localSourceDirectory = store.getRepoSourceDirectory();
	}

	@Override
	public boolean run() {
		logger.info("检查 {0} 仓库是否存在", this.gitUrl);
		if(!isValidRemoteRepository()) {
			logger.error("{0} 不存在", CliLogger.ANSWER);
			return false;
		} else {
			logger.info("{0} 存在", CliLogger.ANSWER);
		}
		
		logger.info("开始同步 git 仓库 {0}", this.gitUrl);
		try {
			syncRepository();
			logger.info("{0} 同步完成", CliLogger.ANSWER);
			return true;
		} catch (RuntimeException e) {
			logger.error("{0} 同步失败", CliLogger.ANSWER);
			logger.error(e);
			return false;
		}
	}
	
	protected boolean isValidRemoteRepository() {
		return GitUtils.isValidRemoteRepository(this.gitUrl);
	}
	
	// 提取出来，方便 mock，添加 final 实现内联
	// 但是此处没有添加 final，因为 mockito 默认不能 mock final 方法
	protected void syncRepository() {
		GitUtils.syncRepository(gitUrl, localSourceDirectory);
	}
}
