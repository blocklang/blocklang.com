package com.blocklang.core.runner.action;

import java.util.Optional;

import org.springframework.util.Assert;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.runner.common.AbstractAction;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.ExecutionContext;

/**
 * 校验 git url 是否指向一个有效的 git 仓库
 * 
 * <pre>
 * inputs
 *     gitUrl  - string(required)，git 仓库地址
 * outputs
 * </pre>
 * 
 * @author Zhengwei Jin
 *
 */
public class CheckGitUrlAction extends AbstractAction{

	public static final String INPUT_GIT_URL = "gitUrl";
	
	private String gitUrl;
	
	public CheckGitUrlAction(ExecutionContext context) {
		super(context);
		
		this.gitUrl = context.getStringValue(INPUT_GIT_URL);
		Assert.hasText(this.gitUrl, "必须要设置 " + INPUT_GIT_URL + " 参数!");
	}

	@Override
	public Optional<Boolean> run() {
		logger.info("检查 {0} 仓库是否存在", this.gitUrl);
		if(GitUtils.isValidRemoteRepository(this.gitUrl)) {
			logger.info("{0} 存在", CliLogger.ANSWER);
			return Optional.of(true);
		}
		logger.error("{0} 不存在", CliLogger.ANSWER);
		return Optional.empty();
	}

}
