package com.blocklang.develop.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "page_func_connection")
public class PageFunctionConnection implements Serializable{

	private static final long serialVersionUID = -3171174657720190996L;

	@Id
	@Column(name = "dbid", length = 32, updatable = false)
	private String id;
	
	@Column(name = "from_output_port_id", length = 32, nullable = false)
	private String fromOutputPortId;
	
	@Column(name = "to_input_port_id", length = 32, nullable = false)
	private String toInputPortId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFromOutputPortId() {
		return fromOutputPortId;
	}

	public void setFromOutputPortId(String fromOutputPortId) {
		this.fromOutputPortId = fromOutputPortId;
	}

	public String getToInputPortId() {
		return toInputPortId;
	}

	public void setToInputPortId(String toInputPortId) {
		this.toInputPortId = toInputPortId;
	}
	
}
