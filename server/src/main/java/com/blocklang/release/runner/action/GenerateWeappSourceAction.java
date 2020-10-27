package com.blocklang.release.runner.action;

import java.nio.file.Path;

import com.blocklang.core.runner.common.AbstractAction;
import com.blocklang.core.runner.common.CliCommand;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.release.data.MiniProgramStore;

/**
 * 生成微信小程序源代码
 * 
 * @author jinzw
 *
 */
public class GenerateWeappSourceAction extends AbstractAction{
	
	private MiniProgramStore store;

	public GenerateWeappSourceAction(ExecutionContext context) {
		super(context);
		this.store = context.getValue(ExecutionContext.STORE, MiniProgramStore.class);
	}

	/**
	 * 在仓库中小程序项目的根目录下执行命令：
	 *
	 * <p>
	 * <code>mp --type weapp --model-dir ./your/model/root/dir</code>
	 * </p>
	 * 
	 * 此命令有两个目录：
	 * 
	 * <ol>
	 * <li>在当前工作目录生成源代码
	 * <li>从模型目录读取模型数据
	 * </ol>
	 * 
	 * @return 执行成功，则返回 <code>true</code>；否则返回 <code>false</code>
	 */
	@Override
	public boolean run() {
		logger.info("开始生成微信小程序源码");
		Path sourceDirectory = store.getProjectSourceDirectory();
		CliCommand cli = new CliCommand(logger);

		String modelPath = store.getProjectModelDirectory().toString();
		String[] commands = {"mp", "--type", "weapp", "--model-dir", modelPath};
		return cli.run(sourceDirectory, commands);
	}
}
