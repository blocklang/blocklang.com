package com.blocklang.develop.data;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.blocklang.core.data.group.First;
import com.blocklang.core.data.group.Second;

/**
 * 校验项目名称
 * 
 * @author Zhengwei Jin
 *
 */
@GroupSequence({CheckProjectNameParam.class, First.class, Second.class})
public class CheckProjectNameParam {
	private String owner;

	@NotBlank(message = "{NotBlank.projectName}", groups = {First.class})
	@Pattern(regexp = "^[a-zA-Z0-9\\-\\w\\.]+$", message = "{NotValid.projectName}", groups = {Second.class})
	private String name;

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
