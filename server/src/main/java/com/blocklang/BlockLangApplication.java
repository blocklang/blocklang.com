package com.blocklang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching // 启用缓存机制
@SpringBootApplication
public class BlockLangApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlockLangApplication.class, args);
	}
	
}
