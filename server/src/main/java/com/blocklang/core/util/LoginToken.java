package com.blocklang.core.util;

import java.util.Base64;

/**
 * 自动登录的 token 的结构为 token:provider, 如 xxxx:qq
 * 
 * 这里只是做简单的加密和解密
 * 
 * @author Zhengwei Jin
 *
 */
public class LoginToken {
	private String provider;
	private String token;

	public String encode(String provider, String token) {
		String data = provider + ":" + token;
		return new String(Base64.getEncoder().encode(data.getBytes()));
	}

	public void decode(String encoded) {
		String plainText = new String(Base64.getDecoder().decode(encoded.getBytes()));
		String[] splited = plainText.split(":");
		provider = splited[0];
		token = splited[1];
	}

	public String getProvider() {
		return this.provider;
	}

	public String getToken() {
		return this.token;
	}

}