package com.blocklang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching // 启用缓存机制
@SpringBootApplication
@ServletComponentScan("com.blocklang.release.controller")
public class BlockLangApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlockLangApplication.class, args);
		
		onStartUpSuccess();
	}

	private static void onStartUpSuccess() {
		System.out.println();
		System.out.println("Block Lang 启动成功！");
		System.out.println();
	}
	
}
