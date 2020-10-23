package com.blocklang.release.runner.action;

import java.nio.file.Path;

import com.blocklang.core.runner.common.AbstractAction;
import com.blocklang.core.runner.common.CliCommand;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.release.data.ProjectStore;

/**
 * 生成微信小程序源代码
 * 
 * @author jinzw
 *
 */
public class GenerateWeappSourceAction extends AbstractAction{
	
	private ProjectStore store;

	public GenerateWeappSourceAction(ExecutionContext context) {
		super(context);
		this.store = context.getValue(ExecutionContext.STORE, ProjectStore.class);
	}

	/**
	 * 在仓库中小程序项目的根目录下执行命令：
	 *
	 * <p>
	 * <code>mp --type weapp --model-dir ./your/model/root/dir</code>
	 * </p>
	 * 
	 * 不设置 <code>--model-dir</code> 时，说明存放模型的根目录就是在当前工作目录下。
	 * 
	 * @return 执行成功，则返回 <code>true</code>；否则返回 <code>false</code>
	 */
	@Override
	public boolean run() {
		logger.info("开始生成微信小程序源码");
		Path workingDirectory = store.getProjectDirectory();
		CliCommand cli = new CliCommand(logger);
		
		String[] commands = {"mp", "--type", "weapp"};
		return cli.run(workingDirectory, commands);
	}
}
