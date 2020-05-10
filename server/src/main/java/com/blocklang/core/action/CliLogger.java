package com.blocklang.core.action;

public interface CliLogger {

	void log(String content);

	void error(Throwable e);

}
