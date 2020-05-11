package com.blocklang.core.runner;

import org.springframework.messaging.simp.SimpMessagingTemplate;

public interface CliContext<T> {

	CliLogger newLogger(SimpMessagingTemplate messagingTemplate, String desinationPrefix);
	
	CliLogger getLogger();
	
	T getData();
}
