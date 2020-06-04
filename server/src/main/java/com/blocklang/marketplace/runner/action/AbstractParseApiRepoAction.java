package com.blocklang.marketplace.runner.action;

import java.util.List;

import com.blocklang.core.runner.common.AbstractAction;
import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.marketplace.apirepo.RepoParser;

/**
 * 
 * 逐个版本解析 Widget 的 API 仓库，并将解析后的结果存储在文件系统中。
 * 
 * <ul>
 * <li> 准备要解析的 tag 和分支：获取所有 tag 和 master 分支信息
 * <li> 切换到一个版本后，获取所有 change-set json 文件
 * <li> 获取 Widget 列表
 * <li> 逐个 Widget 的解析 change-set json 文件
 * <li> 校验 json schema
 * <li> 保存解析结果
 * </ul>
 * 
 * 注意：此类用于解析所有类型的 API，然后在 run 方法中执行不同的解析操作
 * 
 * <pre>
 * inputs
 * outputs
 *     parsedTags    - 成功解析的 tag 列表
 * </pre>
 *
 */
public abstract class AbstractParseApiRepoAction extends AbstractAction{
	private List<String> parsedTags;
	
	public AbstractParseApiRepoAction(ExecutionContext context) {
		super(context);
	}

	@Override
	public boolean run() {
		RepoParser parser = createApiRepoParser();
		
		var success = parser.run();
		if(success) {
			parsedTags = parser.getParsedTags();
		}
		return success;
	}
	
	protected abstract RepoParser createApiRepoParser();

	@Override
	protected Object getOutput(String paramKey) {
		if("parsedTags".equals(paramKey)) {
			return parsedTags;
		}
		return super.getOutput(paramKey);
	}

}
