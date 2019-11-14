package com.blocklang.core.util;

import java.io.UnsupportedEncodingException;

public class StringUtil {

	/**
	 * 判断字符串的字节长度，一个英文字母占一个字节，一个汉字占两个字节。
	 * 
	 * @param cs
	 * @return
	 */
	public static int byteLength(String str) {
		if(str == null) {
			return 0;
		}
		
		String newString = "";
		try {
			newString = new String(str.getBytes("GB2312"), "ISO-8859-1");
			return newString.length();
		} catch (UnsupportedEncodingException e) {
			return 0;
		}
	}

}
