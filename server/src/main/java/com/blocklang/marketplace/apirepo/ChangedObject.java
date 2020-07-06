package com.blocklang.marketplace.apirepo;

/**
 * 每个调整都是在一个对象上的调整，我们将这个对象称为 {@link ChangedObject}， 
 * 而维护这个对象的每个操作数据，我们使用 {@link ChangeData} 标识， 
 * 同时 {@link ChangedObject} 本身也是一个 {@link ChangeData}。
 * 
 * @author Zhengwei Jin
 *
 */
public interface ChangedObject extends ChangeData {
	String getId();

	String getName();

	String getCode();
}
