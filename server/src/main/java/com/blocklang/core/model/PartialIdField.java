package com.blocklang.core.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * 数据库代理主键字段
 * 
 * @author Zhengwei Jin
 *
 */
@MappedSuperclass
public abstract class PartialIdField implements Serializable{

	private static final long serialVersionUID = -4718246714776411995L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "dbid", updatable = false)
	private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}
