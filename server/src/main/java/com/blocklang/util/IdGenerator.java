package com.blocklang.util;

import java.util.UUID;

public class IdGenerator {

	/**
	 * 生成字符数不多于22个的 UUID
	 * 
	 * @return 字符数不多于22个的 UUID
	 */
	public static final String shortUuid() {
		ShortUuid.Builder builder = new ShortUuid.Builder();
		ShortUuid shortUuid = builder.build(UUID.randomUUID());

		return shortUuid.toString();
	}

	public static String uuid() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
}
