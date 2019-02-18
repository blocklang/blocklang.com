package com.blocklang.develop.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NewProjectParam extends CheckProjectNameParam{
	
	private String description;
	
	@JsonProperty("public")
	private Boolean isPublic;
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}
	
}
