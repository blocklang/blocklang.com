package com.blocklang.marketplace.runner.action;

import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.core.util.SpringUtils;
import com.blocklang.marketplace.service.PersistApiRepoService;
import com.blocklang.marketplace.service.PersistWidgetApiRepoService;

/**
 * 往数据库中存储仓库中所有 tag 和 master 分支中的 API 信息。
 * 
 * 
 * @author Zhengwei Jin
 *
 */
public class PersistWidgetApiRepoAction extends AbstractPersistApiRepoAction{

	public PersistWidgetApiRepoAction(ExecutionContext context) {
		super(context);
	}
	
	@Override
	protected PersistApiRepoService getPersistApiRepoService() {
		return SpringUtils.getBean(PersistWidgetApiRepoService.class);
	}

}
