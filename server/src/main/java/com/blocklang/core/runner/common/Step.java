package com.blocklang.core.runner.common;

import java.util.ArrayList;
import java.util.List;

public class Step {

	private String id;
	private String name;
	private Boolean ifResult = false; // 相当于 if
	private AbstractAction uses; // 没有 run 命令，所以将所有 cli 都封装为 action
	// 为 action 设置输入参数，其中的值只能传入字符串类型，
	// 如果需要引用对象的话，需传入表达式，如 ${{expr}}
	private List<StepWith> with = new ArrayList<StepWith>();

	public Step(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getIfResult() {
		return ifResult;
	}

	public void setIfResult(Boolean ifResult) {
		this.ifResult = ifResult;
	}

	public AbstractAction getUses() {
		return uses;
	}

	public void setUses(AbstractAction uses) {
		this.uses = uses;
	}

	public List<StepWith> getWith() {
		return with;
	}

	public void addWith(String key, String value) {
		with.add(new StepWith(key, value));
	}

}
