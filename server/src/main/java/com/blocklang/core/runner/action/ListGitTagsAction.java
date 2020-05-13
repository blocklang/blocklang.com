package com.blocklang.core.runner.action;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.git.exception.GitTagFailedException;
import com.blocklang.core.runner.common.AbstractAction;
import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.ExecutionContext;

/**
 * 获取 git 仓库中的所有 tag
 * 
 * <pre>
 * inputs
 *     localSourceDirectory - Path(required)，仓库的存储路径
 * outputs
 *     gitTags              - String[]，git tag 列表
 * </pre>
 * 
 * @author Zhengwei Jin
 *
 */
public class ListGitTagsAction extends AbstractAction{

	// TODO: 考虑是否需要将此参数提升为 context 级变量
	// Action 级的变量，都要加上 action 前缀，其中还需要包含 step id，这样才能绑定到实例上，而不是变量上。
	// 所以 id 是必填项
	public static final String INPUT_LOCAL_SOURCE_DIRECTORY = ListGitTagsAction.class.getSimpleName() + ".localSourceDirectory";
	public static final String OUTPUT_GIT_TAGS = "gitTags";
	
	// TODO: 优化设计，参数链很重要
	// 难点是如何设置 Action 级的变量，在 step 配置中设置，然后 action 从 step 配置中获取
	private Path localSourceDirectory;
	
	public ListGitTagsAction(ExecutionContext context) {
		super(context);
		
		this.localSourceDirectory = context.getValue(INPUT_LOCAL_SOURCE_DIRECTORY, Path.class);
		Assert.notNull(this.localSourceDirectory, "必须要设置 " + INPUT_LOCAL_SOURCE_DIRECTORY + " 参数！");
	}

	/**
	 * 即使在仓库中没有找到 git tags，也不要返回 false，因为此 action 仅仅是收集信息。
	 */
	@Override
	public Optional<Boolean> run() {
		logger.info("获取仓库的 tag 列表");
		
		// 判断是不是有效的 git 仓库
		Assert.isTrue(GitUtils.isGitRepo(this.localSourceDirectory), this.localSourceDirectory.toString() + " 不是有效的 git 仓库。");
		
		try {
			List<String> tags = GitUtils.getTags(this.localSourceDirectory)
					.stream()
					.map(ref -> ref.getName())
					.collect(Collectors.toList());
			context.putValue(OUTPUT_GIT_TAGS, tags);
			
			// TODO: 让 TaskLogger 支持一次写入多行日志
			logger.info("{0} 共有 {1} 个 git tags：", CliLogger.ANSWER, tags.size());
			tags.forEach(tag -> logger.info("{0} {1}", CliLogger.ANSWER, tag));
			
			return Optional.of(true);
		} catch(GitTagFailedException e) {
			logger.error(e);
			return Optional.empty();
		}
	}

}
