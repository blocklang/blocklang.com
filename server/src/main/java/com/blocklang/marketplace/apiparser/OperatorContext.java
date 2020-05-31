package com.blocklang.marketplace.apiparser;

import java.util.ArrayList;
import java.util.List;

import com.blocklang.core.runner.common.CliLogger;
import com.blocklang.marketplace.task.CodeGenerator;

public class OperatorContext<T extends Codeable> {

	private CliLogger logger;
	private CodeGenerator codeGenerator;
	
	private List<T> components = new ArrayList<>();
	private T current;

	public List<T> getComponents() {
		return this.components;
	}

	public void addComponent(T component) {
		this.components.add(component);
		this.current = component;
	}

	public T getSelectedComponent() {
		return current;
	}

	public CodeGenerator getComponentCodeGenerator() {
		if(codeGenerator != null) {
			return codeGenerator;
		}
		String seed = components.isEmpty() ? null : components.get(components.size() - 1).getCode();
		return new CodeGenerator(seed);
	}
	
	public CliLogger getLogger() {
		return logger;
	}

	public void setLogger(CliLogger logger) {
		this.logger = logger;
	}
	
}
