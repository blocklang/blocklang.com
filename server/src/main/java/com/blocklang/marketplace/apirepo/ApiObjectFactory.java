package com.blocklang.marketplace.apirepo;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.core.runner.common.JsonSchemaValidator;
import com.blocklang.marketplace.apirepo.apiobject.ApiObjectContext;
import com.blocklang.marketplace.data.MarketplaceStore;

/**
 * 创建 ApiObjectParser 需要用到的对象。
 * 
 * 抽象工厂模式
 * 
 * @author Zhengwei Jin
 *
 */
public abstract class ApiObjectFactory {
	
	/**
	 * 创建 changelog 文件的 json schema 校验器。
	 * 
	 * @return json schema 校验器
	 */
	public abstract JsonSchemaValidator createSchemaValidator();

	/**
	 * 创建解析 api object 的上下文对象。
	 * 在上下文对象中存储从某一个 git tag 或 master 分支中解析出的 apiObject 列表
	 * 
	 * @return 解析 api object 的上下文对象
	 */
	public abstract ApiObjectContext createApiObjectContext(MarketplaceStore store, CliLogger logger);
	
	/**
	 * 创建解析 Change 的工厂类。
	 * 该工厂类中解析所有类型的 Change 操作，并返回 Change 列表。
	 * 
	 * @param logger 日志类
	 * @return 解析和应用 Change 的工厂类。
	 */
	public abstract ChangeParserFactory createChangeParserFactory(CliLogger logger);
	
}
