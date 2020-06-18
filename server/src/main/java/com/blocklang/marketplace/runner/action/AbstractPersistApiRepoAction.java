package com.blocklang.marketplace.runner.action;

import com.blocklang.core.runner.common.AbstractAction;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.marketplace.service.PersistApiRepoService;

public abstract class AbstractPersistApiRepoAction extends AbstractAction {

	public AbstractPersistApiRepoAction(ExecutionContext context) {
		super(context);
	}

	@Override
	public boolean run() {
		return getPersistApiRepoService().save(context);
	}

	protected abstract PersistApiRepoService getPersistApiRepoService();

}
