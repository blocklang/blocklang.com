package com.blocklang.marketplace.runner.action;

import com.blocklang.core.runner.common.AbstractAction;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.marketplace.apirepo.ApiObject;
import com.blocklang.marketplace.apirepo.RepoPersister;

public abstract class AbstractPersistApiRepoAction extends AbstractAction {

	public AbstractPersistApiRepoAction(ExecutionContext context) {
		super(context);
	}

	@Override
	public boolean run() {
		RepoPersister<? extends ApiObject> persister = createApiRepoPersister();
		return persister.run();
	}

	protected abstract RepoPersister<? extends ApiObject> createApiRepoPersister();

}
