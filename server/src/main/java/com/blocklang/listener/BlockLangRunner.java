package com.blocklang.listener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.service.PropertyService;

@Component
public class BlockLangRunner implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(BlockLangRunner.class);
	
	@Autowired
	private PropertyService propertyService;
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		// 全都校验一遍后，再打印错误信息。
		List<String> errors = new ArrayList<>();
		
		// 校验系统参数表中配置的数据是否有效
		// 1. 校验 blocklang.root.path
		String dataRootPathString = propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH, "");
		if(dataRootPathString.isBlank()) {
			errors.add("参数 " + CmPropKey.BLOCKLANG_ROOT_PATH + " 的值不能为空。");
		}else {
			Path dataRootPath = Path.of(dataRootPathString);
			if(Files.notExists(dataRootPath)) {
				try {
					// 初始创建此路径
					Files.createDirectories(dataRootPath);
				} catch (IOException e) {
					errors.add("参数 " + CmPropKey.BLOCKLANG_ROOT_PATH + " 的值 " + dataRootPathString + " 在文件系统中无效。");
					errors.add(ExceptionUtils.getStackTrace(e));
				}
			}
		}
		
		// 2. 校验 maven.root.path
		String mavenRootPathString = propertyService.findStringValue(CmPropKey.MAVEN_ROOT_PATH, "");
		if(mavenRootPathString.isBlank()) {
			errors.add("参数 " + CmPropKey.MAVEN_ROOT_PATH + " 的值不能为空。");
		} else {
			Path mavenRootPath = Path.of(mavenRootPathString);
			if(Files.notExists(mavenRootPath)) {
				errors.add("参数 " + CmPropKey.MAVEN_ROOT_PATH + " 的值 " + mavenRootPathString + " 对应的路径不存在。");
			}
		}
		
		if(!errors.isEmpty()) {
			errors.forEach(error -> logger.error(error));
			throw new Exception("存在无效的参数");
		}
	}

}