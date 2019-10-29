package com.blocklang.marketplace.task;

public interface CliLogger {

	void log(String content);

	void error(Throwable e);

}
