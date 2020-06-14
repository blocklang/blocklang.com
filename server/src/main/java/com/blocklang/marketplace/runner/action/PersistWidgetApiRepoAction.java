package com.blocklang.marketplace.runner.action;

import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.marketplace.apirepo.ApiObject;
import com.blocklang.marketplace.apirepo.ApiRefPersisterFactory;
import com.blocklang.marketplace.apirepo.RepoPersister;
import com.blocklang.marketplace.apirepo.widget.WidgetApiRefPersisterFactory;
import com.blocklang.marketplace.apirepo.widget.data.WidgetData;

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
	protected RepoPersister<? extends ApiObject> createApiRepoPersister() {
		ApiRefPersisterFactory<WidgetData> factory = new WidgetApiRefPersisterFactory();
		return new RepoPersister<WidgetData>(context, factory);
	}


}
