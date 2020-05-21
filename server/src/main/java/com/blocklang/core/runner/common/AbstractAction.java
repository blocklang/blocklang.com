package com.blocklang.core.runner.common;

import java.util.List;

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
	
	public abstract boolean run();

	protected void setInputs(List<StepWith> inputs) { }

	protected Object getOutput(String paramKey) {
		return null;
	}

}
