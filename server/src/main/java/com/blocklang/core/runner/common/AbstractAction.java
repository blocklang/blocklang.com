package com.blocklang.core.runner.common;

import java.util.Optional;

/**
 * 
 * FIXME: 在此类中增加 getInputs 和 setOutputs 方法？这样就可以传递变量。
 * 
 * @author Zhengwei Jin
 *
 */
public abstract class AbstractAction {

	protected CliLogger logger;
	protected ExecutionContext context;
	
	public AbstractAction(ExecutionContext context) {
		this.context = context;
		this.logger = context.getLogger();
	}
	
	// FIXME: 既然不通过 Optional 传返回的结果，是否可直接返回 boolean 类型。
	public abstract Optional<?> run();
}
