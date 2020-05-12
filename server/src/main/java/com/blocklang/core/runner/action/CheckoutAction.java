package com.blocklang.core.runner.action;

import java.nio.file.Path;
import java.util.Optional;

import org.springframework.util.Assert;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.AbstractAction;
import com.blocklang.core.runner.common.ExecutionContext;

/**
 * 从远程 git 仓库 clone 或 pull 源码。
 * 
 * 需要从上下文中读取 repository 变量，该变量表示 git 仓库的远程地址。
 * 
 * <pre>
 * inputs:
 *     repository             - string(required), 远程仓库地址
 *     localSourceDirectory   - Path(required), 仓库在主机上的存储路径
 * outputs:
 * </pre>
 * 
 * @author Zhengwei Jin
 *
 */
public class CheckoutAction extends AbstractAction{

	public static final String INPUT_REPOSITORY = "repository";
	public static final String INPUT_LOCAL_SOURCE_DIRECTORY = "localSourceDirectory";
	
	private String repository;
	private Path localSourceDirectory;
	
	public CheckoutAction(ExecutionContext context) {
		super(context);
		
		this.repository = context.getStringValue(INPUT_REPOSITORY);
		Assert.hasText(this.repository, "必须要设置 " + INPUT_REPOSITORY + " 参数!");
		
		this.localSourceDirectory = context.getValue(INPUT_LOCAL_SOURCE_DIRECTORY, Path.class);
		Assert.notNull(this.localSourceDirectory, "必须要设置 localSourceDirectory 参数!");
	}

	@Override
	public Optional<Boolean> run() {
		logger.info("开始同步 git 仓库 {0}", this.repository);
		
		try {
			GitUtils.syncRepository(repository, localSourceDirectory);
			return Optional.of(true);
		} catch (RuntimeException e) {
			logger.error(e);
			return Optional.empty();
		}
	}
	
}
