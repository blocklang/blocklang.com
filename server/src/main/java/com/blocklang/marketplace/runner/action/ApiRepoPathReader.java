package com.blocklang.marketplace.runner.action;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * 用于读取和解析 api 仓库中的路径。
 * 
 * 约定 api 仓库中的路径遵循 {order}__{description} 命名规范，其中 order 为 yyyyMMddHHmm 时间戳且必填，description 非必填。
 * 
 * @author Zhengwei Jin
 *
 */
public class ApiRepoPathReader {
	
	private static final String SEPARATOR = "__";
	
	private String name;
	private ApiRepoPathInfo pathInfo;

	public ApiRepoPathInfo read(String name) {
		if(StringUtils.equalsIgnoreCase(this.name, name)) {
			return pathInfo;
		}
		this.name = null;
		if(validate(name).isEmpty()) {
			return this.pathInfo;
		}
		return null;
	}

	public List<String> validate(String name) {
		this.name = name;
		
		List<String> errors = new ArrayList<String>();
		
		String[] array = name.split(SEPARATOR);
		if(array.length == 1) {
			String order = array[0];
			try {
				LocalDateTime.parse(order, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
			}catch(DateTimeParseException e) {
				errors.add(order + "应该是格式为 yyyyMMddHHmm 的时间");
			}
			
			pathInfo = new ApiRepoPathInfo(array[0], null);
		}else if(array.length >=2 ) {
			String order = array[0];
			try {
				LocalDateTime.parse(order, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
			}catch(DateTimeParseException e) {
				errors.add(order + "应该是格式为 yyyyMMddHHmm 的时间");
			}
			String[] left = Arrays.copyOfRange(array, 1, array.length);
			pathInfo = new ApiRepoPathInfo(array[0], String.join(SEPARATOR, left));
		}
		return errors;
	}

}
