package com.blocklang.release.constant;

public class Bot {
	
	/**
	 * 由 Installer 直接访问 API，所以没有用户信息，但在设计数据库时，约定创建人信息不能为空，
	 * 所以将所有通过 API 访问的创建用户标识的值都设置为此。
	 */
	public static final Integer ID = -99;
}
