package com.blocklang.marketplace.runner.action;

import com.blocklang.core.runner.common.ExecutionContext;
import com.blocklang.marketplace.apirepo.ApiObjectFactory;
import com.blocklang.marketplace.apirepo.RepoParser;
import com.blocklang.marketplace.apirepo.apiobject.widget.WidgetFactory;

/**
 * 解析 Widget api 仓库
 * 
 * <pre>
 * inputs: 无
 * outputs
 * 
 * </pre>
 * 
 * TODO: 输出新解析的 tag 列表
 * 
 * @author Zhengwei Jin
 *
 */
public class ParseWidgetApiRepoAction extends AbstractParseApiRepoAction{

	public ParseWidgetApiRepoAction(ExecutionContext context) {
		super(context);
	}

	@Override
	protected RepoParser createApiRepoParser() {
		ApiObjectFactory factory = new WidgetFactory();
		return new RepoParser(context, factory);
	}
	
}
